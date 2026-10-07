package com.example.testing1.data

import com.example.testing1.model.ExpenseModel
import com.example.testing1.model.MonthlyGoalModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BudgetRepository {

    private val auth      = Firebase.auth
    private val firestore = Firebase.firestore

    // ── Helpers ───────────────────────────────────────────────────────────────

    private val uid: String
        get() = auth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

    private val expensesRef
        get() = firestore
            .collection("expenses")
            .document(uid)
            .collection("entries")

    private val goalsRef
        get() = firestore
            .collection("goals")
            .document(uid)
            .collection("monthly")

    private fun goalDocId(year: Int, month: Int) = "${year}_${month}"

    // ── Expenses ──────────────────────────────────────────────────────────────

    suspend fun addExpense(expense: ExpenseModel): Result<String> = runCatching {
        val doc = expensesRef.document()
        val withId = expense.copy(id = doc.id)
        doc.set(withId.toMap()).await()
        doc.id
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> = runCatching {
        expensesRef.document(expenseId).delete().await()
    }


    fun getAllExpenses(): Flow<List<ExpenseModel>> = callbackFlow {
        val listener = expensesRef
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExpenseModel::class.java)
                } ?: emptyList()
                trySend(expenses)
            }
        // Cancel the Firestore listener when the Flow is cancelled
        awaitClose { listener.remove() }
    }

    /** 5 most recent expenses (for Home screen). */
    fun getRecentExpenses(): Flow<List<ExpenseModel>> = callbackFlow {
        val listener = expensesRef
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val expenses = snapshot?.documents?.mapNotNull {
                    it.toObject(ExpenseModel::class.java)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }


    fun getExpensesInRange(fromMillis: Long, toMillis: Long): Flow<List<ExpenseModel>> =
        callbackFlow {
            val listener = expensesRef
                .whereGreaterThanOrEqualTo("dateMillis", fromMillis)
                .whereLessThanOrEqualTo("dateMillis", toMillis)
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) { close(error); return@addSnapshotListener }
                    val expenses = snapshot?.documents?.mapNotNull {
                        it.toObject(ExpenseModel::class.java)
                    } ?: emptyList()
                    trySend(expenses)
                }
            awaitClose { listener.remove() }
        }


    fun getTotalSpentInRange(fromMillis: Long, toMillis: Long): Flow<Double> =
        getExpensesInRange(fromMillis, toMillis).map { expenses ->
            expenses.sumOf { it.amount }
        }


    fun getCategoryTotalsInRange(
        fromMillis: Long,
        toMillis: Long
    ): Flow<List<CategoryTotal>> =
        getExpensesInRange(fromMillis, toMillis).map { expenses ->
            expenses
                .groupBy { it.category }
                .map { (category, list) ->
                    CategoryTotal(category, list.sumOf { it.amount })
                }
                .sortedByDescending { it.total }
        }

    // ── Goals ─────────────────────────────────────────────────────────────────

    // Upsert monthly goal document
    suspend fun saveGoal(goal: MonthlyGoalModel): Result<Unit> = runCatching {
        goalsRef
            .document(goalDocId(goal.year, goal.month))
            .set(goal.toMap())
            .await()
    }

    fun getGoalForMonth(year: Int, month: Int): Flow<MonthlyGoalModel?> = callbackFlow {
        val listener = goalsRef
            .document(goalDocId(year, month))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val goal = snapshot?.toObject(MonthlyGoalModel::class.java)
                trySend(goal)
            }
        awaitClose { listener.remove() }
    }
}
data class CategoryTotal(
    val category: String = "",
    val total: Double = 0.0,
)