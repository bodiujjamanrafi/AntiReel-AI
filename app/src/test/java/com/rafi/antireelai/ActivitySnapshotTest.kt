package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ActivitySnapshotTest {
    private val day=LocalDate.of(2026,10,24)
    @Test fun newRecordedTimeProducesANewChartSnapshot() {
        val values=mutableMapOf<String,Any>("count:$day:instagram" to 3)
        val old=ActivityDay.read(day,values)
        values["usage:$day:instagram"]=12_345L
        val current=ActivityDay.read(day,values)
        assertEquals(0L,old.totalMillis)
        assertEquals(12345L,current.totalMillis)
        assertNotEquals(old,current)
    }
    @Test fun countOnlyHistoryNeverBecomesInventedTime() {
        val data=ActivityDay.read(day,mapOf("count:$day:youtube" to 200))
        assertEquals(200,data.totalReels);assertEquals(0L,data.totalMillis)
    }
    @Test fun eachDayAndPlatformStayIndependent() {
        val values=mapOf("usage:$day:instagram" to 60000L,"usage:$day:youtube" to 90000L,"usage:${day.minusDays(1)}:instagram" to 20000L)
        assertEquals(150000L,ActivityDay.read(day,values).totalMillis)
        assertEquals(20000L,ActivityDay.read(day.minusDays(1),values).totalMillis)
    }
    @Test fun emptyComparisonHasNoInventedBaseline() {
        assertEquals("No recorded time yet",comparisonLabel(0,0))
        assertEquals("No previous-day baseline",comparisonLabel(60000,0))
    }
    @Test fun comparisonExpressesRealDurationDifference() {
        assertEquals("1m 0s more",comparisonLabel(120000,60000))
        assertEquals("1m 0s less",comparisonLabel(60000,120000))
        assertEquals("Same recorded time",comparisonLabel(60000,60000))
    }
}
