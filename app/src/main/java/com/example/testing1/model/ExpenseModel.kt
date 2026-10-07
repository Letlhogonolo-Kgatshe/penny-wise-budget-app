package com.example.testing1.model

data class ExpenseModel(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val note: String? = null,
    val photoUri: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
)

{
    fun toMap(): Map<String, Any?> = mapOf(
        "id"          to id,
        "amount"      to amount,
        "category"    to category,
        "note"        to note,
        "photoUri"    to photoUri,
        "dateMillis"  to dateMillis,
    )
}