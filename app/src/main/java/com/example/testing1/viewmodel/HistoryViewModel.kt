
package com.example.testing1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.BudgetRepository
import com.example.testing1.model.ExpenseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val repo = BudgetRepository()

    private val _fromMillis = MutableStateFlow(startOfCurrentMonth())
    private val _toMillis   = MutableStateFlow(endOfCurrentMonth())

    val fromMillis = _fromMillis.asStateFlow()
    val toMillis   = _toMillis.asStateFlow()

    val expenses: StateFlow<List<ExpenseModel>> =
        combine(_fromMillis, _toMillis) { from, to -> from to to }
            .flatMapLatest { (from, to) -> repo.getExpensesInRange(from, to) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRange(from: Long, to: Long) {
        _fromMillis.value = from
        _toMillis.value   = to
    }

    fun deleteExpense(expense: ExpenseModel) {
        viewModelScope.launch {
            repo.deleteExpense(expense.id)
        }
    }
}