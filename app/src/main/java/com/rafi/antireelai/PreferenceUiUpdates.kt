package com.rafi.antireelai

/** Merge a preference transaction into one UI-frame refresh, without dropping domains. */
internal class PreferenceUiUpdates {
    private var pending=0
    fun collect(key:String?):Boolean {
        if(key==null || key.startsWith("status:") || key.startsWith("nudged:") || key=="refreshUsageRequest")return false
        pending=pending or SETTINGS
        if(key==CustomPlatforms.KEY || key=="lastUsageUpdate" || key=="activityEpoch" || key.startsWith("count:") || key.startsWith("usage:"))pending=pending or ACTIVITY
        return true
    }
    fun drain():Int=pending.also{pending=0}
    companion object {const val ACTIVITY=1;const val SETTINGS=2}
}
internal fun historyDaysForScreen(tab:Int):Int=when(tab){0,1->7;2->30;else->0}
