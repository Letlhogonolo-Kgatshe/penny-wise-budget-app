package com.example.testing1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.BudgetRepository
import com.example.testing1.model.ExpenseModel
import com.example.testing1.model.MonthlyGoalModel
import kotlinx.coroutines.flow.*
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val repo = BudgetRepository()
    private val now   = Calendar.getInstance()
    private val year  = now.get(Calendar.YEAR)
    private val month = now.get(Calendar.MONTH) + 1

    val recentExpenses: StateFlow<List<ExpenseModel>> =
        repo.getRecentExpenses()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyGoal: StateFlow<MonthlyGoalModel?> =
        repo.getGoalForMonth(year, month)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalSpentThisMonth: StateFlow<Double> =
        repo.getTotalSpentInRange(startOfCurrentMonth(), endOfCurrentMonth())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBalance: StateFlow<Double> =
        combine(monthlyGoal, totalSpentThisMonth) { goal, spent ->
            (goal?.incomeMaxGoal ?: 0.0) - spent
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendingStatus: StateFlow<SpendingStatus> =
        combine(monthlyGoal, totalSpentThisMonth) { goal, spent ->
            when {
                goal == null || goal.spendingMaxGoal == 0.0 -> SpendingStatus.NO_GOAL
                spent > goal.spendingMaxGoal                -> SpendingStatus.OVER_MAX
                spent < goal.spendingMinGoal                -> SpendingStatus.UNDER_MIN
                else                                         -> SpendingStatus.ON_TRACK
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStatus.NO_GOAL)

    val xpThisMonth: StateFlow<Int> =
        recentExpenses.map { it.size * 10 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val badges: StateFlow<List<HomeBadge>> =
        combine(monthlyGoal, totalSpentThisMonth, recentExpenses) { goal, spent, expenses ->
            buildList {
                if (expenses.isNotEmpty())        add(HomeBadge("🥾", "First Step"))
                if (goal != null && spent <= goal.spendingMaxGoal && goal.spendingMaxGoal > 0)
                    add(HomeBadge("🎯", "On Budget"))
                if (goal != null && spent < goal.spendingMinGoal && goal.spendingMinGoal > 0)
                    add(HomeBadge("💎", "Under Spend"))
                if (expenses.size >= 7)           add(HomeBadge("🔥", "Streak"))
                if (goal != null && goal.investingMinGoal > 0) add(HomeBadge("📈", "Investor"))
                if (goal != null && goal.emergencyMinGoal > 0) add(HomeBadge("🛡️", "Safety Net"))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}