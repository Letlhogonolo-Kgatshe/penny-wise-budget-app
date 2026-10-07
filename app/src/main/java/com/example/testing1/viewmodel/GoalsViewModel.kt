
package com.example.testing1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.BudgetRepository
import com.example.testing1.model.MonthlyGoalModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class GoalsViewModel : ViewModel() {

    private val repo = BudgetRepository()
    private val now          = Calendar.getInstance()
    val currentYear: Int     = now.get(Calendar.YEAR)
    val currentMonth: Int    = now.get(Calendar.MONTH) + 1

    val currentGoal: StateFlow<MonthlyGoalModel?> =
        repo.getGoalForMonth(currentYear, currentMonth)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _saveState = MutableStateFlow<GoalSaveState>(GoalSaveState.Idle)
    val saveState = _saveState.asStateFlow()

    fun saveGoal(
        incomeMin: String,    incomeMax: String,
        spendingMin: String,  spendingMax: String,
        investingMin: String, investingMax: String,
        emergencyMin: String, emergencyMax: String,
    ) {
        _saveState.value = GoalSaveState.Loading
        viewModelScope.launch {
            val result = repo.saveGoal(
                MonthlyGoalModel(
                    year             = currentYear,
                    month            = currentMonth,
                    incomeMinGoal    = incomeMin.toDoubleOrNull()    ?: 0.0,
                    incomeMaxGoal    = incomeMax.toDoubleOrNull()    ?: 0.0,
                    spendingMinGoal  = spendingMin.toDoubleOrNull()  ?: 0.0,
                    spendingMaxGoal  = spendingMax.toDoubleOrNull()  ?: 0.0,
                    investingMinGoal = investingMin.toDoubleOrNull() ?: 0.0,
                    investingMaxGoal = investingMax.toDoubleOrNull() ?: 0.0,
                    emergencyMinGoal = emergencyMin.toDoubleOrNull() ?: 0.0,
                    emergencyMaxGoal = emergencyMax.toDoubleOrNull() ?: 0.0,
                )
            )
            _saveState.value = if (result.isSuccess) GoalSaveState.Success
            else GoalSaveState.Error(result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    fun resetSaveState() { _saveState.value = GoalSaveState.Idle }
}

sealed class GoalSaveState {
    object Idle    : GoalSaveState()
    object Loading : GoalSaveState()
    object Success : GoalSaveState()
    data class Error(val message: String) : GoalSaveState()
}