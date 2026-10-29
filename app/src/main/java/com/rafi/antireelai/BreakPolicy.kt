package com.rafi.antireelai

/** Reject obsolete alarms when the user has ended or replaced a break. */
internal enum class BreakDue { INACTIVE, STALE, WAITING, COMPLETE }
internal fun breakDue(until:Long,expected:Long,now:Long):BreakDue = when {
    until<=0->BreakDue.INACTIVE
    expected!=0L && expected!=until->BreakDue.STALE
    until>now->BreakDue.WAITING
    else->BreakDue.COMPLETE
}
