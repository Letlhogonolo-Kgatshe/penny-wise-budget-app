package com.example.testing1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.BudgetRepository
import com.example.testing1.data.CategoryTotal
import com.example.testing1.model.MonthlyGoalModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class TotalsViewModel : ViewModel() {

    private val repo = BudgetRepository()
    private val now  = Calendar.getInstance()

    private val _fromMillis = MutableStateFlow(startOfCurrentMonth())
    private val _toMillis   = MutableStateFlow(endOfCurrentMonth())

    val fromMillis = _fromMillis.asStateFlow()
    val toMillis   = _toMillis.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryTotals: StateFlow<List<CategoryTotal>> =
        combine(_fromMillis, _toMillis) { f, t -> f to t }
            .flatMapLatest { (f, t) -> repo.getCategoryTotalsInRange(f, t) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent: StateFlow<Double> =
        combine(_fromMillis, _toMillis) { f, t -> f to t }
            .flatMapLatest { (f, t) -> repo.getTotalSpentInRange(f, t) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentGoal: StateFlow<MonthlyGoalModel?> =
        repo.getGoalForMonth(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setRange(from: Long, to: Long) {
        _fromMillis.value = from
        _toMillis.value   = to
    }
}