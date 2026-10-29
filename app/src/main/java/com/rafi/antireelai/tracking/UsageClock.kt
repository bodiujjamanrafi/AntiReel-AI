package com.rafi.antireelai.tracking

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Monotonic duration, attributed only between recent foreground-player observations. */
data class UsageSlice(val platform:String,val fromEpochMs:Long,val toEpochMs:Long)
class UsageClock(private val maxGapMs:Long=2000) {
    private var previous:String?=null
    private var elapsed=0L
    fun sample(platform:String?,nowElapsed:Long,nowWall:Long):UsageSlice? {
        val before=previous
        val delta=nowElapsed-elapsed
        previous=platform;elapsed=nowElapsed
        return if(before!=null && delta in 1..maxGapMs) UsageSlice(before,nowWall-delta,nowWall) else null
    }
    fun reset() { previous=null;elapsed=0 }
}
object UsageDays {
    fun split(from:Long,to:Long,zone:ZoneId=ZoneId.systemDefault()):List<Pair<LocalDate,Long>> {
        if(to<=from) return emptyList()
        var cursor=from
        val result=mutableListOf<Pair<LocalDate,Long>>()
        while(cursor<to) {
            val day=Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate()
            val end=minOf(to,day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli())
            result.add(day to end-cursor);cursor=end
        }
        return result
    }
}
