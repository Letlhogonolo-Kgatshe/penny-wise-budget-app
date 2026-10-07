package com.example.testing1.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.BudgetRepository
import com.example.testing1.model.ExpenseModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddExpenseViewModel : ViewModel() {

    private val repo    = BudgetRepository()
    private val storage = Firebase.storage

    private val _uiState = MutableStateFlow<AddExpenseUiState>(AddExpenseUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun saveExpense(
        amount: String,
        category: String,
        note: String,
        dateMillis: Long,
        photoUri: Uri?,
    ) {
        val parsedAmount = amount.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0) {
            _uiState.value = AddExpenseUiState.Error("Please enter a valid amount")
            return
        }

        _uiState.value = AddExpenseUiState.Loading

        viewModelScope.launch {
            try {
                val uploadedPhotoUrl = photoUri?.let { uploadPhoto(it) }

                repo.addExpense(
                    ExpenseModel(
                        amount     = parsedAmount,
                        category   = category,
                        note       = note.ifBlank { null },
                        photoUri   = uploadedPhotoUrl,
                        dateMillis = dateMillis,
                    )
                ).getOrThrow()

                _uiState.value = AddExpenseUiState.Success

            } catch (e: Exception) {
                _uiState.value = AddExpenseUiState.Error(e.message ?: "Failed to save")
            }
        }
    }

    private suspend fun uploadPhoto(uri: Uri): String {
        val uid      = FirebaseAuth.getInstance().currentUser!!.uid
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref      = storage.reference.child("photos/$uid/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    fun resetState() { _uiState.value = AddExpenseUiState.Idle }
}

sealed class AddExpenseUiState {
    object Idle    : AddExpenseUiState()
    object Loading : AddExpenseUiState()
    object Success : AddExpenseUiState()
    data class Error(val message: String) : AddExpenseUiState()
}