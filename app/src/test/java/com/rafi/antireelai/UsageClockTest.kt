package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class UsageClockTest {
    @Test fun startsAtFirstRecognizedFrameWithoutInventingHistory() {
        val c=UsageClock();assertNull(c.sample("youtube",100,10000));val s=c.sample("youtube",750,10650)!!;assertEquals("youtube",s.platform);assertEquals(650L,s.toEpochMs-s.fromEpochMs)
    }
    @Test fun pauseStopsAccruingImmediatelyAfterLastObservedInterval() {
        val c=UsageClock();c.sample("instagram",0,10000);assertEquals(500L,c.sample(null,500,10500)!!.let{it.toEpochMs-it.fromEpochMs});assertNull(c.sample(null,1500,11500));assertNull(c.sample("instagram",1800,11800))
    }
    @Test fun platformSwitchAttributesPreviousIntervalToPreviousPlatform() {
        val c=UsageClock();c.sample("youtube",0,10000);assertEquals("youtube",c.sample("tiktok",600,10600)!!.platform);assertEquals("tiktok",c.sample("tiktok",1200,11200)!!.platform)
    }
    @Test fun suspendedWorkerGapDoesNotCountUnobservedMinutes() {
        val c=UsageClock();c.sample("facebook",0,10000);assertNull(c.sample("facebook",60000,70000));assertEquals(500L,c.sample("facebook",60500,70500)!!.let{it.toEpochMs-it.fromEpochMs})
    }
    @Test fun wallClockJumpDoesNotInflateMonotonicDuration() {
        val c=UsageClock();c.sample("moj",100,10000);val s=c.sample("moj",600,900000)!!;assertEquals(500L,s.toEpochMs-s.fromEpochMs)
    }
    @Test fun resetDiscardsPendingAttribution() {
        val c=UsageClock();c.sample("josh",0,10000);c.reset();assertNull(c.sample("josh",500,10500))
    }
    @Test fun midnightSplitsIntoCorrectLocalDays() {
        val zone=ZoneId.of("Asia/Dhaka");val midnight=LocalDate.of(2026,10,25).atStartOfDay(zone).toInstant().toEpochMilli();val parts=UsageDays.split(midnight-800,midnight+1200,zone);assertEquals(listOf(LocalDate.of(2026,10,24) to 800L,LocalDate.of(2026,10,25) to 1200L),parts)
    }
    @Test fun daylightSavingDayUsesRealMidnightBoundaries() {
        val zone=ZoneId.of("America/New_York");val day=LocalDate.of(2026,3,8);val from=day.atStartOfDay(zone).toInstant().toEpochMilli();val to=day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();assertEquals(listOf(day to 23*3600_000L),UsageDays.split(from,to,zone))
    }
    @Test fun noDataDoesNotCreateAStreakAndMissingDayBreaksIt() {
        val today=LocalDate.of(2026,10,24);assertEquals(0,usageStreak(today){false});val active=setOf(today,today.minusDays(1),today.minusDays(3));assertEquals(2,usageStreak(today){it in active})
    }
    @Test fun durationFormattingUsesActualRecordedTime() {
        assertEquals("0s",durationLabel(0));assertEquals("59s",durationLabel(59000));assertEquals("1m 5s",durationLabel(65000));assertEquals("1h 2m",durationLabel(3720000))
    }
}
