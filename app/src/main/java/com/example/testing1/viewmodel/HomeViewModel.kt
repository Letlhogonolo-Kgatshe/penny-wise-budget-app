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

    // XP and the Streak badge count every expense this month. recentExpenses
    // is capped at 5 for the Home list, so it can't be used for these.
    private val expenseCountThisMonth: StateFlow<Int> =
        repo.getExpensesInRange(startOfCurrentMonth(), endOfCurrentMonth())
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val spendingStatus: StateFlow<SpendingStatus> =
        combine(monthlyGoal, totalSpentThisMonth, ::spendingStatusFor)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingStatus.NO_GOAL)

    val xpThisMonth: StateFlow<Int> =
        expenseCountThisMonth.map(::xpFor)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val badges: StateFlow<List<HomeBadge>> =
        combine(monthlyGoal, totalSpentThisMonth, expenseCountThisMonth, ::badgesFor)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}