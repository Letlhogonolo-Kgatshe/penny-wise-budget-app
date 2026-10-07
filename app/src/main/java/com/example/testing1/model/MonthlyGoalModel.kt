package com.example.testing1.model

data class MonthlyGoalModel(
    val year: Int = 0,
    val month: Int = 0,
    val incomeMinGoal: Double = 0.0,
    val incomeMaxGoal: Double = 0.0,
    val spendingMinGoal: Double = 0.0,
    val spendingMaxGoal: Double = 0.0,
    val investingMinGoal: Double = 0.0,
    val investingMaxGoal: Double = 0.0,
    val emergencyMinGoal: Double = 0.0,
    val emergencyMaxGoal: Double = 0.0,
)

{
    fun toMap(): Map<String, Any> = mapOf(
        "year"             to year,
        "month"            to month,
        "incomeMinGoal"    to incomeMinGoal,
        "incomeMaxGoal"    to incomeMaxGoal,
        "spendingMinGoal"  to spendingMinGoal,
        "spendingMaxGoal"  to spendingMaxGoal,
        "investingMinGoal" to investingMinGoal,
        "investingMaxGoal" to investingMaxGoal,
        "emergencyMinGoal" to emergencyMinGoal,
        "emergencyMaxGoal" to emergencyMaxGoal,
    )
}