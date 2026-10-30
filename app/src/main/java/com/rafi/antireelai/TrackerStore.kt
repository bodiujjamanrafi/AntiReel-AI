package com.rafi.antireelai

import android.content.Context
import java.time.LocalDate
import com.rafi.antireelai.tracking.UsageDays

class TrackerStore(context: Context) {
    internal val contextForSpeech=context.applicationContext
    private val appContext=context.applicationContext
    val dailyMinutes:Int get()=prefs.getInt("dailyMinutes",0)
    fun allowanceProgress()=dailyAllowanceProgress(total(),goal,totalUsage(),dailyMinutes)
    fun allowanceReached()=dailyAllowanceReached(total(),goal,totalUsage(),dailyMinutes)

    internal fun saveOnboardingProfile(profile:OnboardingProfile):Boolean {
        if(!profile.valid)return false
        val ok=prefs.edit().remove("firstUseGuide").putString("onboardingDraft",profile.draft).putInt("onboardingStep",12)
            .putString("userName",profile.name).putString("language",profile.language)
            .putInt("dailyMinutes",profile.minutes).putInt("goal",profile.reels).putStringSet("platforms",profile.platforms)
            .putString("reminderStyle",profile.reminderStyle.toString()).putBoolean("onboardingMuted",profile.muted)
            .putBoolean("voiceEnabled",!profile.muted).putBoolean("goalReminders",!profile.muted)
            .putBoolean("onboardingComplete",true).putBoolean("voiceGreeted",true).putBoolean("welcomeSeen",true).commit()
        if(!ok)prefs.edit().putBoolean("onboardingComplete",false).commit()
        return ok
    }

    var limitNotifications:Boolean
        get()=prefs.getBoolean("limitNotifications",true)
        set(value){prefs.edit().putBoolean("limitNotifications",value).apply()}
    var breakNotifications:Boolean
        get()=prefs.getBoolean("breakNotifications",true)
        set(value){prefs.edit().putBoolean("breakNotifications",value).apply();BreakReminders.showCurrent(appContext)}
    val prefs = context.getSharedPreferences("antireel", Context.MODE_PRIVATE)
    private var catalogSource:String?=null
    private var catalogValue:List<Platform> = emptyList()
    val catalog:List<Platform> get() {
        val source=prefs.getString(CustomPlatforms.KEY,"") ?: ""
        if(source!=catalogSource || catalogValue.isEmpty()) {
            catalogValue=CustomPlatforms.catalog(CustomPlatforms.decode(source));catalogSource=source
        }
        return catalogValue
    }
    val visibleSelected:Set<String> get()=catalog.filter{it.available && it.id in selected}.map{it.id}.toSet()
    val trackedPlatforms:List<Platform> get()=catalog.filter{it.available && it.detectorId!=null && it.id in selected}
    fun addCustomPlatform(packageName:String,name:String):Boolean {
        if(!CustomPlatforms.validPackage(packageName) || packageName==CustomPlatforms.REMOVED_FACEBOOK_LITE)return false
        val existing=catalog.find{packageName in it.packages && it.available}
        if(existing!=null)return false
        val records=CustomPlatforms.decode(prefs.getString(CustomPlatforms.KEY,"") ?: "").toMutableList()
        val record=CustomPlatformRecord(packageName,name.filterNot{it.isISOControl()}.trim().take(80).ifBlank{packageName},true)
        val index=records.indexOfFirst{it.packageName==packageName}
        if(index<0)records.add(record) else records[index]=record
        val id=CustomPlatforms.id(packageName)
        prefs.edit().putString(CustomPlatforms.KEY,CustomPlatforms.encode(records))
            .putStringSet("platforms",if(packageName==CustomPlatforms.YOUTUBE)selected+id else selected).apply()
        PlatformCatalog.refresh(this)
        return true
    }
    fun removeCustomPlatform(platform:Platform) {
        if(!platform.isCustom)return
        val records=CustomPlatforms.decode(prefs.getString(CustomPlatforms.KEY,"") ?: "").map {
            if(it.packageName in platform.packages)it.copy(visible=false) else it
        }
        prefs.edit().putString(CustomPlatforms.KEY,CustomPlatforms.encode(records)).putStringSet("platforms",selected-platform.id).apply()
        PlatformCatalog.refresh(this)
    }
    var themeMode:String
        get()=prefs.getString("themeMode","system") ?: "system"
        set(value){prefs.edit().putString("themeMode",value).apply()}
    var limitTint:Boolean
        get()=prefs.getBoolean("limitTint",true)
        set(value){prefs.edit().putBoolean("limitTint",value).apply()}
    var voiceEnabled:Boolean
        get()=prefs.getBoolean("voiceEnabled",true)
        set(value){prefs.edit().putBoolean("voiceEnabled",value).apply()}
    var voiceGreeted:Boolean
        get()=prefs.getBoolean("voiceGreeted",false)
        set(value){prefs.edit().putBoolean("voiceGreeted",value).apply()}
    fun usage(id:String,day:LocalDate=LocalDate.now())=prefs.getLong("usage:$day:$id",0)
    fun totalUsage(day:LocalDate=LocalDate.now())=catalog.sumOf {usage(it.id,day)}
    fun addUsage(id:String,from:Long,to:Long) {
        val edit=prefs.edit()
        UsageDays.split(from,to).forEach { (day,ms)->edit.putLong("usage:$day:$id",usage(id,day)+ms) }
        prune(edit,LocalDate.now())
        edit.putLong("lastUsageUpdate",System.currentTimeMillis()).apply()
    }
    var welcomeSeen: Boolean
        get() = prefs.getBoolean("welcomeSeen",false)
        set(value) { prefs.edit().putBoolean("welcomeSeen",value).apply() }
    var overlaySize: Int
        get() = prefs.getInt("overlaySize", 96).coerceIn(80, 128)
        set(value) { prefs.edit().putInt("overlaySize", value.coerceIn(80,128)).apply() }
    var overlayTop: Int
        get() = prefs.getInt("overlayTop", 38).coerceIn(20,180)
        set(value) { prefs.edit().putInt("overlayTop", value.coerceIn(20,180)).apply() }
    var expressions: Boolean
        get() = prefs.getBoolean("expressions", true)
        set(value) { prefs.edit().putBoolean("expressions", value).apply() }
    var reducedMotion: Boolean
        get() = prefs.getBoolean("reducedMotion", false)
        set(value) { prefs.edit().putBoolean("reducedMotion", value).apply() }
    var compatibility: Boolean
        get() = prefs.getBoolean("compatibility", true)
        set(value) { prefs.edit().putBoolean("compatibility", value).apply() }
    var goalReminders: Boolean
        get() = prefs.getBoolean("goalReminders", true)
        set(value) { prefs.edit().putBoolean("goalReminders", value).apply() }
    var remainingMode: Boolean
        get() = prefs.getBoolean("remainingMode", false)
        set(value) { prefs.edit().putBoolean("remainingMode", value).apply() }
    var showPlatform: Boolean
        get() = prefs.getBoolean("showPlatform", true)
        set(value) { prefs.edit().putBoolean("showPlatform", value).apply() }
    var pauseUntil: Long
        get() = prefs.getLong("pauseUntil", 0)
        set(value) { prefs.edit().putLong("pauseUntil", value).apply();BreakReminders.schedule(appContext,value) }
    fun status(id: String): String = prefs.getString("status:$id", "not_seen") ?: "not_seen"
    fun status(id: String, status: String) { if(this.status(id) != status) prefs.edit().putString("status:$id", status).apply() }
    var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) { prefs.edit().putBoolean("haptics", value).apply() }
    var counterOpacity: Float
        get() = prefs.getFloat("counterOpacity", .90f)
        set(value) { prefs.edit().putFloat("counterOpacity", value).apply() }
    var running: Boolean
        get() = prefs.getBoolean("running", false)
        set(value) { prefs.edit().putBoolean("running", value).putBoolean("manuallyPaused", !value).apply() }
    var consent: Boolean
        get() = AccessibilityConsent.current(prefs.getBoolean("consent", false),prefs.getInt(AccessibilityConsent.KEY,0))
        set(value) { prefs.edit().putBoolean("consent", value).putInt(AccessibilityConsent.KEY,if(value)AccessibilityConsent.VERSION else 0).apply() }
    var selected: Set<String>
        get() = prefs.getStringSet("platforms", emptySet())!!.toSet()
        set(value) { prefs.edit().putStringSet("platforms", value).apply() }
    var goal: Int
        get() = if(restartUsed) prefs.getInt("restartLimit", baseGoal) else baseGoal
        set(value) { prefs.edit().putInt("goal", value.coerceAtLeast(1)).apply() }
    val baseGoal:Int get()=prefs.getInt("goal",30).coerceAtLeast(1)
    val restartUsed:Boolean get()=prefs.getString("restartDay",null)==LocalDate.now().toString()
    fun restartForToday():Boolean {
        if(restartUsed || total()<goal)return false
        val limit=(goal.toLong()+30).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        prefs.edit().putString("restartDay",LocalDate.now().toString()).putInt("restartLimit",limit).apply()
        if(pauseUntil!=0L)pauseUntil=0
        running=true
        return true
    }
    fun startAfterConnection() {
        if(consent && !running && !prefs.getBoolean("manuallyPaused",false)) {
            prefs.edit().putBoolean("running",true).apply()
        }
    }
    fun requestActivation() {
        prefs.edit().putBoolean("consent",true).putInt(AccessibilityConsent.KEY,AccessibilityConsent.VERSION).putBoolean("manuallyPaused",false).putBoolean("running",true).apply()
    }
    var opacity: Float
        get() = prefs.getFloat("opacity", .60f)
        set(value) { prefs.edit().putFloat("opacity", value).apply() }
    fun count(id: String, day: LocalDate = LocalDate.now()) = prefs.getInt("count:$day:$id", 0)
    fun total(day: LocalDate = LocalDate.now()) = catalog.sumOf { count(it.id, day).toLong() }.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    private var lastPrunedDay: LocalDate? = null
    fun increment(id: String) {
        val today = LocalDate.now()
        if(allowanceReached())return
        val edit = prefs.edit().putInt("count:$today:$id", count(id) + 1)
        prune(edit,today)
        edit.apply()
    }
    private fun prune(edit:android.content.SharedPreferences.Editor,today:LocalDate) {
        if(lastPrunedDay!=today) {
            prefs.all.keys.filter { it.startsWith("count:") || it.startsWith("nudged:") || it.startsWith("usage:") }.forEach { key ->
                val day = runCatching { LocalDate.parse(key.split(":")[1]) }.getOrNull()
                if (day != null && day.isBefore(today.minusDays(29))) edit.remove(key)
            }
            lastPrunedDay=today
        }
    }
    fun clearCounts() { prefs.edit().remove("lastUsageUpdate").putLong("activityEpoch",prefs.getLong("activityEpoch",0)+1).also { e -> prefs.all.keys.filter { it.startsWith("count:") || it.startsWith("usage:") || it.startsWith("nudged:") }.forEach(e::remove) }.apply() }
}
