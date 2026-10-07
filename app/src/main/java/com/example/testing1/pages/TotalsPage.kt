// ── TotalsScreen.kt ───────────────────────────────────────────────────────────
package com.example.testing1.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testing1.model.MonthlyGoalModel
import com.example.testing1.viewmodel.TotalsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BgDark        = Color(0xFF121212)
private val Surface       = Color(0xFF1E1E1E)
private val Primary       = Color(0xFF6C63FF)
private val AccentGold    = Color(0xFFFFD700)
private val AccentGreen   = Color(0xFF4CAF50)
private val AccentRed     = Color(0xFFCF6679)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)
private val TextMuted     = Color(0xFF666666)

private val SliceColours = listOf(
    Color(0xFF6C63FF), Color(0xFFFFD700), Color(0xFF4CAF50),
    Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63),
    Color(0xFF00BCD4), Color(0xFFFF9800)
)
private val categoryEmoji = mapOf(
    "food" to "🍔", "transport" to "🚗", "shopping" to "🛍️",
    "bills" to "📄", "health" to "💊", "fun" to "🎮",
    "education" to "📚", "other" to "📦"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalsPage( modifier: Modifier = Modifier,vm: TotalsViewModel = viewModel()) {
    val categories   by vm.categoryTotals.collectAsState()
    val totalSpent   by vm.totalSpent.collectAsState()
    val goal         by vm.currentGoal.collectAsState()
    val fromMillis   by vm.fromMillis.collectAsState()
    val toMillis     by vm.toMillis.collectAsState()

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker   by remember { mutableStateOf(false) }
    val fromState = rememberDatePickerState(initialSelectedDateMillis = fromMillis)
    val toState   = rememberDatePickerState(initialSelectedDateMillis = toMillis)
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().background(BgDark)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 100.dp)) {
        Spacer(modifier = Modifier.height(30.dp))

        Text("Spending Breakdown", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp))

        // ── Date range selector ───────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateRangeButton("From: ${fmt.format(Date(fromMillis))}", Modifier.weight(1f)) {
                showFromPicker = true }
            DateRangeButton("To: ${fmt.format(Date(toMillis))}", Modifier.weight(1f)) {
                showToPicker = true }
        }

        if (showFromPicker) DateRangePickerDialog(fromState, { showFromPicker = false }) {
            fromState.selectedDateMillis?.let { vm.setRange(it, toMillis) }
            showFromPicker = false
        }
        if (showToPicker) DateRangePickerDialog(toState, { showToPicker = false }) {
            toState.selectedDateMillis?.let { vm.setRange(fromMillis, it) }
            showToPicker = false
        }

        // ── Donut chart ───────────────────────────────────────────────────
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            DonutChart(categories = categories, modifier = Modifier.size(220.dp))
        }

        // ── Total spent label ─────────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text("$${"%,.2f".format(totalSpent)}", fontSize = 26.sp,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Total Spent", fontSize = 11.sp, color = TextSecondary)
        }

        // ── Goal progress bar (min / max) ─────────────────────────────────
        goal?.let { GoalProgressSection(totalSpent = totalSpent, goal = it) }

        Spacer(Modifier.height(20.dp))

        // ── Bar chart per category ────────────────────────────────────────
        if (categories.isNotEmpty()) {
            Text("BY CATEGORY", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
            CategoryBarChart(categories = categories, goal = goal as MonthlyGoalModel?)
            Spacer(Modifier.height(20.dp))
        }

        // ── Category list ─────────────────────────────────────────────────
        if (categories.isEmpty()) {
            Text("No data for this period", fontSize = 13.sp, color = TextMuted)
        } else {
            categories.forEachIndexed { i, cat ->
                CategoryRow(cat, SliceColours[i % SliceColours.size], totalSpent)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Goal progress bar showing min/max markers ─────────────────────────────────
@Composable
private fun GoalProgressSection(totalSpent: Double, goal: MonthlyGoalModel) {
    val pct = if (goal.spendingMaxGoal > 0) (totalSpent / goal.spendingMaxGoal).coerceIn(0.0, 1.0) else 0.0
    val barColor = when {
        totalSpent < goal.spendingMinGoal -> AccentGold
        totalSpent > goal.spendingMaxGoal -> AccentRed
        else -> AccentGreen
    }
    val minMarker = if (goal.spendingMaxGoal > 0) (goal.spendingMinGoal / goal.spendingMaxGoal).toFloat() else 0f

    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spending Goal", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary)
                Text("$${"%,.2f".format(totalSpent)}", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = barColor)
            }
            Spacer(Modifier.height(8.dp))
            // Track with min and max markers
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val trackWidth = maxWidth
                Box(modifier = Modifier.fillMaxWidth().height(12.dp)
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(50))) {
                    // Fill
                    Box(modifier = Modifier
                        .fillMaxWidth(pct.toFloat()).fillMaxHeight()
                        .background(barColor, RoundedCornerShape(50)))
                    // Min marker
                    Box(modifier = Modifier
                        .offset(x = trackWidth * minMarker - 1.dp)
                        .width(2.dp).fillMaxHeight()
                        .background(AccentGold))
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Min $${"%,.0f".format(goal.spendingMinGoal)}", fontSize = 10.sp, color = AccentGold)
                Text("Max $${"%,.0f".format(goal.spendingMaxGoal)}", fontSize = 10.sp, color = AccentRed)
            }
            val statusMsg = when {
                totalSpent < goal.spendingMinGoal -> "⚠️ Under minimum target"
                totalSpent > goal.spendingMaxGoal -> "🔴 Over maximum limit!"
                else                      -> "✅ On track"
            }
            Text(statusMsg, fontSize = 12.sp, color = barColor,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

// ── Custom bar chart with min/max goal lines ──────────────────────────────────
@Composable
private fun CategoryBarChart(categories: List<com.example.testing1.data.CategoryTotal>, goal: MonthlyGoalModel?) {
    val maxValue = maxOf(
        categories.maxOfOrNull { it.total } ?: 0.0,
        goal?.spendingMaxGoal ?: 0.0
    )

    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp)) {
            if (maxValue == 0.0) return@Canvas

            val chartH = size.height - 24.dp.toPx()   // leave room for labels
            val barW   = (size.width / (categories.size * 2f)).coerceAtMost(48.dp.toPx())
            val gap    = (size.width - barW * categories.size) / (categories.size + 1)

            categories.forEachIndexed { i, cat ->
                val barH   = (cat.total / maxValue * chartH).toFloat()
                val x      = gap + i * (barW + gap)
                val colour = SliceColours[i % SliceColours.size]

                // Bar
                drawRoundRect(
                    color  = colour,
                    topLeft = Offset(x, chartH - barH),
                    size   = Size(barW, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
                // Amount label above bar
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        textSize  = 10.dp.toPx()
                        color     = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(
                        "$${"%,.0f".format(cat.total)}",
                        x + barW / 2, chartH - barH - 4.dp.toPx(), paint
                    )
                }
            }

            // Min goal line (gold dashed)
            goal?.let {
                val minY = (chartH - (it.spendingMinGoal / maxValue * chartH)).toFloat()
                drawLine(color = AccentGold, start = Offset(0f, minY),
                    end = Offset(size.width, minY), strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 8f)))
                // Max goal line (red dashed)
                val maxY = (chartH - (it.spendingMaxGoal / maxValue * chartH)).toFloat()
                drawLine(color = AccentRed, start = Offset(0f, maxY),
                    end = Offset(size.width, maxY), strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 8f)))
            }
        }

        // Legend for goal lines
        goal?.let {
            Row(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(AccentGold, "Min goal")
                LegendDot(AccentRed, "Max goal")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

// ── Donut chart ───────────────────────────────────────────────────────────────
@Composable
private fun DonutChart(categories: List<com.example.testing1.data.CategoryTotal>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 52f, cap = StrokeCap.Butt)
        val d      = size.minDimension - stroke.width
        val tl     = Offset(stroke.width / 2f, stroke.width / 2f)
        val sz     = Size(d, d)
        if (categories.isEmpty()) {
            drawArc(Color(0xFF2A2A2A), 0f, 360f, false, tl, sz, style = stroke); return@Canvas
        }
        val total = categories.sumOf { it.total }.takeIf { it > 0 } ?: return@Canvas
        var angle = -90f
        categories.forEachIndexed { i, cat ->
            val sweep = (cat.total / total * 360f).toFloat()
            drawArc(SliceColours[i % SliceColours.size], angle, sweep - 2f,
                false, tl, sz, style = stroke)
            angle += sweep
        }
    }
}

// ── Category list row ─────────────────────────────────────────────────────────
@Composable
private fun CategoryRow(cat: com.example.testing1.data.CategoryTotal, colour: Color, grandTotal: Double) {
    val pct = if (grandTotal > 0) (cat.total / grandTotal * 100).toInt() else 0
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)) {
        Box(modifier = Modifier.size(10.dp).background(colour, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(categoryEmoji[cat.category] ?: "📦", fontSize = 18.sp,
            modifier = Modifier.padding(end = 8.dp))
        Text(cat.category.replaceFirstChar { it.uppercase() }, fontSize = 13.sp,
            fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
        Text("$pct%", fontSize = 11.sp, color = TextSecondary,
            modifier = Modifier.padding(end = 8.dp))
        Text("$${"%,.2f".format(cat.total)}", fontSize = 13.sp,
            fontWeight = FontWeight.Bold, color = colour)
    }
}