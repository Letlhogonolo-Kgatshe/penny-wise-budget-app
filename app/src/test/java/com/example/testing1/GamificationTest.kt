package com.example.testing1

import com.example.testing1.model.MonthlyGoalModel
import com.example.testing1.viewmodel.SpendingStatus
import com.example.testing1.viewmodel.badgesFor
import com.example.testing1.viewmodel.spendingStatusFor
import com.example.testing1.viewmodel.xpFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationTest {

    private val goal = MonthlyGoalModel(
        year = 2026, month = 5,
        spendingMinGoal = 1_000.0, spendingMaxGoal = 5_000.0,
    )

    private fun labels(goal: MonthlyGoalModel?, spent: Double, count: Int) =
        badgesFor(goal, spent, count).map { it.label }

    // ── XP ───────────────────────────────────────────────────────────────────

    @Test
    fun xp_isTenPerExpense() {
        assertEquals(0, xpFor(0))
        assertEquals(30, xpFor(3))
    }

    @Test
    fun xp_isNotCappedAtFiveExpenses() {
        // Regression: XP used to be computed from the 5 most recent expenses only.
        assertEquals(120, xpFor(12))
    }

    // ── Spending status ──────────────────────────────────────────────────────

    @Test
    fun status_noGoal_whenGoalMissingOrMaxIsZero() {
        assertEquals(SpendingStatus.NO_GOAL, spendingStatusFor(null, 500.0))
        assertEquals(SpendingStatus.NO_GOAL, spendingStatusFor(MonthlyGoalModel(), 500.0))
    }

    @Test
    fun status_reflectsSpendAgainstMinAndMax() {
        assertEquals(SpendingStatus.UNDER_MIN, spendingStatusFor(goal, 500.0))
        assertEquals(SpendingStatus.ON_TRACK, spendingStatusFor(goal, 3_000.0))
        assertEquals(SpendingStatus.ON_TRACK, spendingStatusFor(goal, 5_000.0))
        assertEquals(SpendingStatus.OVER_MAX, spendingStatusFor(goal, 5_000.01))
    }

    // ── Badges ───────────────────────────────────────────────────────────────

    @Test
    fun badges_noneForNewUserWithoutGoals() {
        assertTrue(badgesFor(null, 0.0, 0).isEmpty())
    }

    @Test
    fun badges_firstStepAfterFirstExpense() {
        assertEquals(listOf("First Step"), labels(null, 50.0, 1))
    }

    @Test
    fun badges_streakUnlocksAtSevenExpensesThisMonth() {
        assertFalse("Streak" in labels(null, 0.0, 6))
        assertTrue("Streak" in labels(null, 0.0, 7))
    }

    @Test
    fun badges_onBudgetAndUnderSpend() {
        assertTrue("On Budget" in labels(goal, 4_000.0, 1))
        assertFalse("Under Spend" in labels(goal, 4_000.0, 1))

        assertTrue("Under Spend" in labels(goal, 200.0, 1))
        assertFalse("On Budget" in labels(goal, 6_000.0, 1))
    }

    @Test
    fun badges_investorAndSafetyNetFollowGoals() {
        val saver = goal.copy(investingMinGoal = 500.0, emergencyMinGoal = 1_000.0)

        val result = labels(saver, 3_000.0, 2)

        assertTrue("Investor" in result)
        assertTrue("Safety Net" in result)
    }
}
