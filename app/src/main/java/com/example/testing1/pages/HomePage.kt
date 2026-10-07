package com.example.testing1.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testing1.model.MonthlyGoalModel
import com.example.testing1.model.ExpenseModel
import com.example.testing1.viewmodel.HomeBadge
import com.example.testing1.viewmodel.HomeViewModel
import com.example.testing1.viewmodel.SpendingStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
private val White70       = Color(0xB3FFFFFF)
private val White60       = Color(0x99FFFFFF)
private val CardShape     = RoundedCornerShape(16.dp)
private val PillShape     = RoundedCornerShape(50.dp)
private val ChipShape     = RoundedCornerShape(8.dp)

private val categoryEmoji = mapOf(
    "food" to "🍔", "transport" to "🚗", "shopping" to "🛍️",
    "bills" to "📄", "health" to "💊", "fun" to "🎮",
    "education" to "📚", "other" to "📦"
)

data class Tip(val icon: String, val title: String, val body: String)
data class Achievement(
    val icon: String, val title: String,
    val description: String, val xp: Int, val unlocked: Boolean
)

private val sampleTips = listOf(
    Tip("☕", "The Latte Factor",     "Small daily purchases add up. A $5 daily coffee = $1,825/year."),
    Tip("📊", "50/30/20 Rule",        "Spend 50% on needs, 30% on wants, and save at least 20%."),
    Tip("🔄", "Pay Yourself First",   "Move savings the moment your pay lands."),
    Tip("🎯", "Zero-Based Budgeting", "Give every dollar a job so income minus expenses equals zero."),
)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomePage( modifier: Modifier = Modifier,
    onNavigateToAdd: () -> Unit = {},
    vm: HomeViewModel = viewModel()
) {
    val recentExpenses   by vm.recentExpenses.collectAsState()
    val goal             by vm.monthlyGoal.collectAsState()
    val totalSpent       by vm.totalSpentThisMonth.collectAsState()
    val remainingBalance by vm.remainingBalance.collectAsState()
    val spendingStatus   by vm.spendingStatus.collectAsState()
    val xp               by vm.xpThisMonth.collectAsState()
    val badges           by vm.badges.collectAsState()

    // Achievements derived from live data
    val achievements = remember(goal, recentExpenses) {
        buildAchievements(goal as MonthlyGoalModel?, recentExpenses)
    }

    // Dialog state
    var selectedTip         by remember { mutableStateOf<Tip?>(null) }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    var showAllTips         by remember { mutableStateOf(false) }
    var showAllAchievements by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 100.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        HeaderRow(level = xp / 100 + 1)

        // ── XP bar ────────────────────────────────────────────────────────────
        XpBar(xpCurrent = xp % 100, xpMax = 100)

        // ── Balance card ──────────────────────────────────────────────────────
        BalanceCard(
            balance       = remainingBalance,
            income        = goal?.incomeMaxGoal ?: 0.0,
            spent         = totalSpent,
            spendingStatus = spendingStatus,
            goal          = goal as MonthlyGoalModel?
        )

        // ── Spending goal progress bar ────────────────────────────────────────
        goal?.let { g ->
            if (g.spendingMaxGoal > 0) {
                SpendingGoalBar(spent = totalSpent, goal = g)
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Badges ────────────────────────────────────────────────────────────
        BadgesSection(badges)

        // ── Tips ──────────────────────────────────────────────────────────────
        SectionCard(
            title       = "💡 Financial Tips",
            actionLabel = if (showAllTips) "Show less" else "See all",
            onAction    = { showAllTips = !showAllTips }
        ) {
            val visible = if (showAllTips) sampleTips else sampleTips.take(2)
            visible.forEach { tip ->
                TipRow(tip = tip, onClick = { selectedTip = tip })
                if (tip != visible.last()) Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Achievements ──────────────────────────────────────────────────────
        SectionCard(
            title       = "🏆 Achievements",
            actionLabel = if (showAllAchievements) "Show less" else "See all",
            onAction    = { showAllAchievements = !showAllAchievements }
        ) {
            val visible = if (showAllAchievements) achievements else achievements.take(3)
            visible.forEach { a ->
                AchievementRow(a, onClick = { selectedAchievement = a })
                if (a != visible.last()) Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Recent transactions ───────────────────────────────────────────────
        RecentTransactions(
            expenses = recentExpenses,
            onAddExpense = onNavigateToAdd
        )
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    selectedTip?.let         { TipDialog(it)         { selectedTip = null } }
    selectedAchievement?.let { AchievementDialog(it) { selectedAchievement = null } }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderRow(level: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Welcome back 👋", fontSize = 12.sp, color = TextSecondary)
            Text("Penny Wise ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Surface, PillShape)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("⚡", fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text("Lv $level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// XP bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun XpBar(xpCurrent: Int, xpMax: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text("Experience", fontSize = 11.sp, color = TextSecondary,
                modifier = Modifier.weight(1f))
            Text("$xpCurrent / $xpMax XP", fontSize = 11.sp, color = AccentGold)
        }
        Box(modifier = Modifier.fillMaxWidth().height(10.dp)
            .background(Surface, RoundedCornerShape(50))) {
            Box(modifier = Modifier
                .fillMaxWidth((xpCurrent / xpMax.toFloat()).coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(listOf(Primary, AccentGold)),
                    RoundedCornerShape(50)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Balance card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BalanceCard(
    balance: Double,
    income: Double,
    spent: Double,
    spendingStatus: SpendingStatus,
    goal: MonthlyGoalModel?
) {
    val statusColor = when (spendingStatus) {
        SpendingStatus.ON_TRACK  -> AccentGreen
        SpendingStatus.OVER_MAX  -> AccentRed
        SpendingStatus.UNDER_MIN -> AccentGold
        SpendingStatus.NO_GOAL   -> White70
    }
    val statusText = when (spendingStatus) {
        SpendingStatus.ON_TRACK  -> "✅ On track"
        SpendingStatus.OVER_MAX  -> "🔴 Over limit"
        SpendingStatus.UNDER_MIN -> "⚠️ Under target"
        SpendingStatus.NO_GOAL   -> "Set a goal to track progress"
    }

    Card(
        shape  = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(Primary, Color(0xFF9C27B0))),
                    CardShape
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Remaining Balance", fontSize = 12.sp, color = White70)
                Text(
                    "$${"%,.2f".format(balance)}",
                    fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Status badge
                Surface(
                    shape = PillShape,
                    color = statusColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        statusText, fontSize = 11.sp, color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Income / Spent / Goal row
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    StatChip(label = "Income",  value = "$${"%,.0f".format(income)}")
                    Spacer(Modifier.width(16.dp))
                    StatChip(label = "Spent",   value = "$${"%,.0f".format(spent)}")
                    goal?.let {
                        Spacer(Modifier.width(16.dp))
                        StatChip(
                            label = "Limit",
                            value = "$${"%,.0f".format(it.spendingMaxGoal)}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = White60)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Spending goal progress bar (min / max markers)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpendingGoalBar(spent: Double, goal: MonthlyGoalModel) {
    val pct       = (spent / goal.spendingMaxGoal).coerceIn(0.0, 1.0).toFloat()
    val minMarker = (goal.spendingMinGoal / goal.spendingMaxGoal).coerceIn(0.0, 1.0).toFloat()
    val barColor  = when {
        spent > goal.spendingMaxGoal -> AccentRed
        spent < goal.spendingMinGoal -> AccentGold
        else                          -> AccentGreen
    }

    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Monthly Spending", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val w = maxWidth
                Box(modifier = Modifier.fillMaxWidth().height(12.dp)
                    .background(SurfaceLight, RoundedCornerShape(50))) {
                    // Fill
                    Box(modifier = Modifier.fillMaxWidth(pct).fillMaxHeight()
                        .background(barColor, RoundedCornerShape(50)))
                    // Min marker line
                    Box(modifier = Modifier
                        .offset(x = w * minMarker - 1.dp)
                        .width(2.dp).fillMaxHeight()
                        .background(AccentGold))
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Min $${"%,.0f".format(goal.spendingMinGoal)}",
                    fontSize = 10.sp, color = AccentGold)
                Text("$${"%,.2f".format(spent)} spent",
                    fontSize = 10.sp, color = barColor, fontWeight = FontWeight.Bold)
                Text("Max $${"%,.0f".format(goal.spendingMaxGoal)}",
                    fontSize = 10.sp, color = AccentRed)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Badges row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BadgesSection(badges: List<HomeBadge>) {
    val scrollState = rememberScrollState()

    Text("BADGES EARNED", fontSize = 12.sp, fontWeight = FontWeight.Bold,
        color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))

    if (badges.isEmpty()) {
        Text("Log expenses and set goals to earn badges 🎯",
            fontSize = 12.sp, color = TextMuted,
            modifier = Modifier.padding(bottom = 16.dp))
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).horizontalScroll(scrollState)
        ) {
            badges.forEach { badge ->
                Surface(shape = PillShape, color = Primary.copy(alpha = 0.2f)) {
                    Text("${badge.emoji} ${badge.label}", fontSize = 11.sp, color = Primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recent transactions
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RecentTransactions(
    expenses: List<ExpenseModel>,
    onAddExpense: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text("RECENT TRANSACTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = TextSecondary, modifier = Modifier.weight(1f))
        Text("+ Add", fontSize = 12.sp, color = Primary,
            modifier = Modifier.clickable(onClick = onAddExpense))
    }

    if (expenses.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
                .background(Surface, CardShape)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💸", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text("No transactions yet", fontSize = 14.sp,
                    color = TextSecondary, fontWeight = FontWeight.Bold)
                Text("Tap '+ Add' to log your first expense",
                    fontSize = 12.sp, color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    } else {
        val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
        expenses.forEach { expense ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Surface, CardShape)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Category icon in a circle
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp)
                        .background(Primary.copy(alpha = 0.15f), CircleShape)) {
                    Text(categoryEmoji[expense.category] ?: "📦", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        expense.note?.ifBlank { null }
                            ?: expense.category.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    Text(fmt.format(Date(expense.dateMillis)),
                        fontSize = 11.sp, color = TextSecondary)
                }
                Text(
                    "-$${"%,.2f".format(expense.amount)}",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentRed
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section card wrapper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, modifier = Modifier.weight(1f))
                Text(actionLabel, fontSize = 11.sp, color = Primary,
                    modifier = Modifier.clickable(onClick = onAction))
            }
            HorizontalDivider(color = SurfaceLight)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tip row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TipRow(tip: Tip, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
            .background(SurfaceLight, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(12.dp)) {
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp)
                .background(Primary.copy(alpha = 0.15f), CircleShape)) {
            Text(tip.icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tip.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(tip.body, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Achievement row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementRow(achievement: Achievement, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(
                if (achievement.unlocked) Primary.copy(alpha = 0.1f) else SurfaceLight,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick).padding(12.dp)) {
        Text(achievement.icon, fontSize = 26.sp,
            color = TextPrimary.copy(alpha = if (achievement.unlocked) 1f else 0.4f))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                achievement.title + if (achievement.unlocked) " ✓" else "",
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = if (achievement.unlocked) TextPrimary else TextSecondary
            )
            Text(achievement.description, fontSize = 11.sp, color = TextSecondary)
        }
        Surface(shape = ChipShape, color = AccentGold.copy(alpha = 0.15f)) {
            Text("+${achievement.xp} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = AccentGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tip dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TipDialog(tip: Tip, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp)
                        .background(Primary.copy(alpha = 0.15f), CircleShape)) {
                    Text(tip.icon, fontSize = 36.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(tip.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SurfaceLight)
                Spacer(Modifier.height(12.dp))
                Text(tip.body, fontSize = 14.sp, color = TextSecondary,
                    lineHeight = 22.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onDismiss, shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Got it!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Achievement dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementDialog(achievement: Achievement, onDismiss: () -> Unit) {
    val statusColor = if (achievement.unlocked) AccentGreen else TextMuted
    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp).background(
                        if (achievement.unlocked) AccentGold.copy(alpha = 0.15f)
                        else SurfaceLight, CircleShape)) {
                    Text(achievement.icon, fontSize = 40.sp,
                        color = TextPrimary.copy(alpha = if (achievement.unlocked) 1f else 0.4f))
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = PillShape, color = statusColor.copy(alpha = 0.15f)) {
                    Text(if (achievement.unlocked) "✓ Unlocked" else "🔒 Locked",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(achievement.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = TextPrimary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(achievement.description, fontSize = 13.sp, color = TextSecondary,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = SurfaceLight)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text("⚡", fontSize = 18.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Reward: +${achievement.xp} XP", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = AccentGold)
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onDismiss, shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (achievement.unlocked) AccentGreen else Primary),
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (achievement.unlocked) "Awesome! 🎉" else "Keep going!",
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Build achievements from live data
// ─────────────────────────────────────────────────────────────────────────────
private fun buildAchievements(
    goal: MonthlyGoalModel?,
    expenses: List<ExpenseModel>
): List<Achievement> = listOf(
    Achievement("🥾", "First Step",   "Log your first expense",             20,
        expenses.isNotEmpty()),
    Achievement("🔥", "On a Streak",  "Log 7 or more expenses this month",  50,
        expenses.size >= 7),
    Achievement("💰", "Saver",        "Stay under your spending max",       100,
        goal != null && goal.spendingMaxGoal > 0 &&
                expenses.sumOf { it.amount } <= goal.spendingMaxGoal),
    Achievement("📈", "Investor",     "Set an investing goal",              30,
        goal != null && goal.investingMinGoal > 0),
    Achievement("🛡️", "Safety Net",  "Set an emergency fund goal",         30,
        goal != null && goal.emergencyMinGoal > 0),
    Achievement("🎯", "Goal Setter",  "Set all four goal categories",       50,
        goal != null && goal.incomeMinGoal > 0 && goal.spendingMinGoal > 0
                && goal.investingMinGoal > 0 && goal.emergencyMinGoal > 0),
)