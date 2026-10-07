package com.example.testing1.viewmodel

import com.example.testing1.model.MonthlyGoalModel

// Pure rules behind the Home screen's XP, badges and spending status.
// Kept free of Android/Firebase types so they can be unit tested on the JVM.

const val XP_PER_EXPENSE = 10
const val STREAK_EXPENSE_COUNT = 7

fun xpFor(expensesThisMonth: Int): Int = expensesThisMonth * XP_PER_EXPENSE

fun spendingStatusFor(goal: MonthlyGoalModel?, spent: Double): SpendingStatus = when {
    goal == null || goal.spendingMaxGoal == 0.0 -> SpendingStatus.NO_GOAL
    spent > goal.spendingMaxGoal                -> SpendingStatus.OVER_MAX
    spent < goal.spendingMinGoal                -> SpendingStatus.UNDER_MIN
    else                                        -> SpendingStatus.ON_TRACK
}

fun badgesFor(goal: MonthlyGoalModel?, spent: Double, expensesThisMonth: Int): List<HomeBadge> = buildList {
    if (expensesThisMonth > 0) add(HomeBadge("🥾", "First Step"))
    if (goal != null && spent <= goal.spendingMaxGoal && goal.spendingMaxGoal > 0)
        add(HomeBadge("🎯", "On Budget"))
    if (goal != null && spent < goal.spendingMinGoal && goal.spendingMinGoal > 0)
        add(HomeBadge("💎", "Under Spend"))
    if (expensesThisMonth >= STREAK_EXPENSE_COUNT) add(HomeBadge("🔥", "Streak"))
    if (goal != null && goal.investingMinGoal > 0) add(HomeBadge("📈", "Investor"))
    if (goal != null && goal.emergencyMinGoal > 0) add(HomeBadge("🛡️", "Safety Net"))
}
