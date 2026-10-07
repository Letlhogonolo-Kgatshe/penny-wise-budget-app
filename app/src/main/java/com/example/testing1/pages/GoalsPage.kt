package com.example.testing1.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testing1.viewmodel.GoalsViewModel

// ── Colours ───────────────────────────────────────────────────────────────────
private val BgDark        = Color(0xFF121212)
private val Surface       = Color(0xFF1E1E1E)
private val SurfaceLight  = Color(0xFF2A2A2A)
private val Primary       = Color(0xFF6C63FF)
private val AccentGold    = Color(0xFFFFD700)
private val AccentGreen   = Color(0xFF4CAF50)
private val AccentRed     = Color(0xFFCF6679)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)
private val TextMuted     = Color(0xFF666666)
private val GoalShape     = RoundedCornerShape(16.dp)
private val FieldShape    = RoundedCornerShape(12.dp)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GoalsPage( modifier: Modifier = Modifier,vm: GoalsViewModel = viewModel()) {
    val goal by vm.currentGoal.collectAsState()

    // ── Per-card edit state ───────────────────────────────────────────────────
    var incomeEditing    by remember { mutableStateOf(false) }
    var spendingEditing  by remember { mutableStateOf(false) }
    var investingEditing by remember { mutableStateOf(false) }
    var emergencyEditing by remember { mutableStateOf(false) }

    // ── Draft inputs — prefilled from Room when goal loads ────────────────────
    var incomeMin    by remember { mutableStateOf("") }
    var incomeMax    by remember { mutableStateOf("") }
    var spendingMin  by remember { mutableStateOf("") }
    var spendingMax  by remember { mutableStateOf("") }
    var investingMin by remember { mutableStateOf("") }
    var investingMax by remember { mutableStateOf("") }
    var emergencyMin by remember { mutableStateOf("") }
    var emergencyMax by remember { mutableStateOf("") }

    // Validation error messages per card
    var incomeError    by remember { mutableStateOf<String?>(null) }
    var spendingError  by remember { mutableStateOf<String?>(null) }
    var investingError by remember { mutableStateOf<String?>(null) }
    var emergencyError by remember { mutableStateOf<String?>(null) }

    // Prefill drafts whenever Room emits a new goal row
    LaunchedEffect(goal) {
        goal?.let { g ->
            incomeMin    = fmt(g.incomeMinGoal)
            incomeMax    = fmt(g.incomeMaxGoal)
            spendingMin  = fmt(g.spendingMinGoal)
            spendingMax  = fmt(g.spendingMaxGoal)
            investingMin = fmt(g.investingMinGoal)
            investingMax = fmt(g.investingMaxGoal)
            emergencyMin = fmt(g.emergencyMinGoal)
            emergencyMax = fmt(g.emergencyMaxGoal)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 100.dp)
    ) {
        Text(
            "Goals & Targets",
            fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Set min & max targets for ${monthName(vm.currentMonth)} ${vm.currentYear}",
            fontSize = 12.sp, color = TextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // ── Income ────────────────────────────────────────────────────────────
        GoalCard(
            emoji        = "💰",
            title        = "Monthly Income",
            minValue     = goal?.incomeMinGoal,
            maxValue     = goal?.incomeMaxGoal,
            minLabel     = "Min target",
            maxLabel     = "Max expected",
            accentColor  = AccentGreen,
            isEditing    = incomeEditing,
            minInput     = incomeMin,
            maxInput     = incomeMax,
            error        = incomeError,
            onMinChange  = { incomeMin = it; incomeError = null },
            onMaxChange  = { incomeMax = it; incomeError = null },
            onEditClick  = { incomeEditing = !incomeEditing; incomeError = null },
            onSave       = {
                incomeError = validate(incomeMin, incomeMax)
                if (incomeError == null) {
                    saveAll(vm, incomeMin, incomeMax, spendingMin, spendingMax,
                        investingMin, investingMax, emergencyMin, emergencyMax)
                    incomeEditing = false
                }
            }
        )

        // ── Spending ──────────────────────────────────────────────────────────
        GoalCard(
            emoji        = "🎯",
            title        = "Monthly Spending",
            minValue     = goal?.spendingMinGoal,
            maxValue     = goal?.spendingMaxGoal,
            minLabel     = "Min budget",
            maxLabel     = "Max limit",
            accentColor  = AccentRed,
            isEditing    = spendingEditing,
            minInput     = spendingMin,
            maxInput     = spendingMax,
            error        = spendingError,
            onMinChange  = { spendingMin = it; spendingError = null },
            onMaxChange  = { spendingMax = it; spendingError = null },
            onEditClick  = { spendingEditing = !spendingEditing; spendingError = null },
            onSave       = {
                spendingError = validate(spendingMin, spendingMax)
                if (spendingError == null) {
                    saveAll(vm, incomeMin, incomeMax, spendingMin, spendingMax,
                        investingMin, investingMax, emergencyMin, emergencyMax)
                    spendingEditing = false
                }
            }
        )

        // ── Investing ─────────────────────────────────────────────────────────
        GoalCard(
            emoji        = "📈",
            title        = "Monthly Investing",
            minValue     = goal?.investingMinGoal,
            maxValue     = goal?.investingMaxGoal,
            minLabel     = "Min contribution",
            maxLabel     = "Max allocation",
            accentColor  = Primary,
            isEditing    = investingEditing,
            minInput     = investingMin,
            maxInput     = investingMax,
            error        = investingError,
            onMinChange  = { investingMin = it; investingError = null },
            onMaxChange  = { investingMax = it; investingError = null },
            onEditClick  = { investingEditing = !investingEditing; investingError = null },
            onSave       = {
                investingError = validate(investingMin, investingMax)
                if (investingError == null) {
                    saveAll(vm, incomeMin, incomeMax, spendingMin, spendingMax,
                        investingMin, investingMax, emergencyMin, emergencyMax)
                    investingEditing = false
                }
            }
        )

        // ── Emergency Fund ────────────────────────────────────────────────────
        GoalCard(
            emoji        = "🛡️",
            title        = "Emergency Fund",
            minValue     = goal?.emergencyMinGoal,
            maxValue     = goal?.emergencyMaxGoal,
            minLabel     = "Min reserve",
            maxLabel     = "Max target",
            accentColor  = AccentGold,
            isEditing    = emergencyEditing,
            minInput     = emergencyMin,
            maxInput     = emergencyMax,
            error        = emergencyError,
            onMinChange  = { emergencyMin = it; emergencyError = null },
            onMaxChange  = { emergencyMax = it; emergencyError = null },
            onEditClick  = { emergencyEditing = !emergencyEditing; emergencyError = null },
            onSave       = {
                emergencyError = validate(emergencyMin, emergencyMax)
                if (emergencyError == null) {
                    saveAll(vm, incomeMin, incomeMax, spendingMin, spendingMax,
                        investingMin, investingMax, emergencyMin, emergencyMax)
                    emergencyEditing = false
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable goal card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GoalCard(
    emoji: String,
    title: String,
    minValue: Double?,
    maxValue: Double?,
    minLabel: String,
    maxLabel: String,
    accentColor: Color,
    isEditing: Boolean,
    minInput: String,
    maxInput: String,
    error: String?,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSave: () -> Unit,
) {
    Card(
        shape  = GoalShape,
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                Text(
                    title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onEditClick,
                    shape  = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (isEditing) "Done" else "Edit",
                        fontSize = 11.sp, color = accentColor
                    )
                }
            }

            // ── Display values ────────────────────────────────────────────────
            if (minValue != null && maxValue != null && (minValue > 0 || maxValue > 0)) {
                MinMaxDisplay(
                    minValue    = minValue,
                    maxValue    = maxValue,
                    minLabel    = minLabel,
                    maxLabel    = maxLabel,
                    accentColor = accentColor
                )
            } else {
                Text("Not set yet", fontSize = 13.sp, color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp))
            }

            // ── Edit form ─────────────────────────────────────────────────────
            AnimatedVisibility(visible = isEditing) {
                Column(modifier = Modifier.padding(top = 14.dp)) {

                    // Min row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoalField(
                            label       = "Min ($)",
                            value       = minInput,
                            onChange    = onMinChange,
                            accentColor = accentColor,
                            modifier    = Modifier.weight(1f)
                        )
                        GoalField(
                            label       = "Max ($)",
                            value       = maxInput,
                            onChange    = onMaxChange,
                            accentColor = accentColor,
                            modifier    = Modifier.weight(1f)
                        )
                    }

                    // Validation error
                    error?.let {
                        Text(
                            it, fontSize = 11.sp, color = AccentRed,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = onSave,
                        shape  = FieldShape,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = Color.White)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Min / Max display row with colour-coded range bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MinMaxDisplay(
    minValue: Double,
    maxValue: Double,
    minLabel: String,
    maxLabel: String,
    accentColor: Color,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Min pill
            Column(horizontalAlignment = Alignment.Start) {
                Text(minLabel, fontSize = 10.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▲ ", fontSize = 10.sp, color = AccentGreen)
                    Text(
                        "$${fmtDisplay(minValue)}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }
            }
            // Max pill
            Column(horizontalAlignment = Alignment.End) {
                Text(maxLabel, fontSize = 10.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▼ ", fontSize = 10.sp, color = AccentRed)
                    Text(
                        "$${fmtDisplay(maxValue)}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Range bar — just a gradient strip showing the spread
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(AccentGreen.copy(alpha = 0.6f), accentColor, AccentRed.copy(alpha = 0.6f))
                    ),
                    RoundedCornerShape(50)
                )
        )

        Text(
            "Range: $${fmtDisplay(minValue)} – $${fmtDisplay(maxValue)}",
            fontSize = 10.sp, color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single labelled input field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GoalField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value          = value,
            onValueChange  = onChange,
            placeholder    = { Text("0.00", color = TextMuted, fontSize = 13.sp) },
            singleLine     = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle      = androidx.compose.ui.text.TextStyle(
                color = TextPrimary, fontSize = 13.sp),
            colors         = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = accentColor,
                unfocusedBorderColor    = SurfaceLight,
                cursorColor             = accentColor,
                focusedContainerColor   = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
            ),
            shape          = FieldShape,
            modifier       = Modifier.fillMaxWidth().height(52.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Validates that both fields are numbers and min < max. Returns error string or null. */
private fun validate(min: String, max: String): String? {
    val minD = min.toDoubleOrNull()
    val maxD = max.toDoubleOrNull()
    return when {
        minD == null || maxD == null -> "Please enter valid numbers"
        minD < 0 || maxD < 0        -> "Values must be positive"
        minD >= maxD                 -> "Min must be less than max"
        else                         -> null
    }
}

/** Saves all eight fields at once so the single Room row stays consistent. */
private fun saveAll(
    vm: GoalsViewModel,
    incomeMin: String,  incomeMax: String,
    spendingMin: String, spendingMax: String,
    investingMin: String, investingMax: String,
    emergencyMin: String, emergencyMax: String,
) = vm.saveGoal(
    incomeMin, incomeMax,
    spendingMin, spendingMax,
    investingMin, investingMax,
    emergencyMin, emergencyMax,
)

private fun fmt(d: Double) =
    if (d == 0.0) "" else d.toBigDecimal().stripTrailingZeros().toPlainString()

private fun fmtDisplay(d: Double) = "%,.2f".format(d)

private fun monthName(month: Int) = java.text.DateFormatSymbols().months[month - 1]