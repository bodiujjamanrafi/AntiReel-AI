package com.rafi.antireelai

import com.rafi.antireelai.tracking.dailyLimitReached
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HistoryAndBoundaryTest {
    private val today=LocalDate.of(2026,10,26)
    @Test fun retainedDatesAppearExactlyOnceAcrossAllWindows() {
        val all=(0..4).flatMap{historyWindow(today,today.minusDays(it*7L))}
        assertEquals(30,all.size)
        assertEquals(30,all.distinct().size)
        assertEquals((0L..29L).map{today.minusDays(it)}.toSet(),all.toSet())
    }
    @Test fun selectedDayBelongsToItsDisplayedWindow() {
        (0L..29L).forEach{offset->val day=today.minusDays(offset);assertTrue(historyWindow(today,day).contains(day))}
    }
    @Test fun oldestPartialWindowDoesNotInventUnretainedDates() {
        val dates=historyWindow(today,today.minusDays(29))
        assertEquals(listOf(today.minusDays(29),today.minusDays(28)),dates)
    }
    @Test fun futureAndExpiredSelectionsClampToValidWindows() {
        assertEquals(historyWindow(today,today),historyWindow(today,today.plusDays(1)))
        assertEquals(historyWindow(today,today.minusDays(29)),historyWindow(today,today.minusDays(90)))
    }
    @Test fun boundaryStopsExactlyAtGoalIncludingOneReelGoals() {
        listOf(1,2,50,Int.MAX_VALUE).forEach{goal->
            assertFalse(dailyLimitReached(goal-1,goal))
            assertTrue(dailyLimitReached(goal,goal))
        }
        assertTrue(dailyLimitReached(51,50))
    }
}
