package com.rafi.antireelai

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class AnalyticsPresentationTest {
    private val today = LocalDate.of(2026, 10, 30)
    private fun day(date:LocalDate, reels:Int=0, millis:Long=0) = ActivityDay(
        date, List(platforms.size){if(it==3)reels else 0}, List(platforms.size){if(it==3)millis else 0L})

    @Test fun recentWeekIsChronologicalRegardlessOfInputOrder() {
        val history=(0..29).map{day(today.minusDays(it.toLong()),it)}.shuffled(java.util.Random(7))
        val week=recentActivityDays(history)
        assertEquals((6 downTo 0).map{today.minusDays(it.toLong())},week.map{it.date})
        assertEquals(21,week.sumOf{it.totalReels})
    }
    @Test fun missingSelectedDateNeverBorrowsTodaysUsage() {
        val selected=today.minusDays(2)
        val data=activityDayOrEmpty(listOf(day(today,99,600000)),selected)
        assertEquals(selected,data.date)
        assertEquals(0,data.totalReels)
        assertEquals(0L,data.totalMillis)
    }
    @Test fun facebookAndIndependentMetricsSurviveSelection() {
        val entry=day(today,500,17000)
        val selected=activityDayOrEmpty(listOf(entry),today)
        assertEquals(500,selected.reels[platforms.indexOfFirst{it.id=="facebook"}])
        assertEquals(17000L,selected.totalMillis)
        assertEquals(1.0,usageShares(selected.millis.map{it.toDouble()}).single().fraction,0.0)
    }
    @Test fun distributionHandlesHugeFiniteValuesAndZeroDays() {
        assertTrue(usageShares(listOf(0.0,0.0)).isEmpty())
        val shares=usageShares(listOf(Double.MAX_VALUE,Double.MAX_VALUE))
        assertEquals(listOf(.5,.5),shares.map{it.fraction})
        assertTrue(shares.all{it.middleDegrees.isFinite()})
    }
    @Test fun historyWindowsCoverOnlyTheThirtyRetainedDaysWithoutGaps() {
        val windows=(0..4).map{historyWindow(today,today.minusDays((it*7).toLong()))}
        val dates=windows.flatten()
        assertEquals(30,dates.size)
        assertEquals(30,dates.distinct().size)
        assertEquals(today.minusDays(29),dates.minOrNull())
        assertEquals(today,dates.maxOrNull())
        assertTrue(historyWindow(today,today.plusDays(10)).none{it>today})
    }
}
