package com.rafi.antireelai.tracking

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.SharedPreferences
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.*
import android.view.*
import android.view.accessibility.*
import android.widget.*
import android.util.Log
import com.rafi.antireelai.*
import java.time.LocalDate
import java.util.concurrent.Executors
import kotlin.math.abs

private const val YOUTUBE_LOG_TAG="AntiReelShorts"

class ReelAccessibilityService : AccessibilityService(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var store: TrackerStore
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val tracker=ReelTracker()
    private val youtube=YouTubeShortsTracker()
    private var youtubePackage=""
    private var scanRequested=false
    private var lastYoutubeDiagnostic=""
    private var lastYoutubeLogAt=0L
    private var speaker:MisoSpeaker?=null
    private var warningGlow:LimitGlowView?=null
    private val pagerState=ReelPager()
    private val usageClock=UsageClock()
    private var usagePending:UsageSlice?=null
    private fun recordUsage(platform:String?,flush:Boolean=false) {
        val slice=usageClock.sample(platform,SystemClock.elapsedRealtime(),System.currentTimeMillis())
        if(slice!=null) {
            val pending=usagePending
            if(pending!=null && pending.platform==slice.platform && pending.toEpochMs==slice.fromEpochMs) usagePending=pending.copy(toEpochMs=slice.toEpochMs)
            else { flushUsage();usagePending=slice }
        }
        if(flush || usagePending?.let { it.toEpochMs-it.fromEpochMs>=3000 || (store.dailyMinutes>0 && store.totalUsage()+it.toEpochMs-it.fromEpochMs>=store.dailyMinutes*60000L) }==true) flushUsage()
    }
    private fun flushUsage() {
        val pending=usagePending ?: return
        usagePending=null
        store.addUsage(pending.platform,pending.fromEpochMs,pending.toEpochMs)
    }
    private var overlay: LinearLayout? = null
    private var companion: CompanionView? = null
    private var label: TextView? = null
    private var contextLabel: TextView? = null
    private var active: Platform? = null
    private var selectedIds:Set<String> = emptySet()
    private var monitoredByPackage:Map<String,Platform> = emptyMap()
    private var busy = false
    private var lastInspectAt = Long.MIN_VALUE / 2
    private var destroyed = false
    private val scanGate=ServiceScanGate()
    private var scrollPackage = ""
    private var lastLimitExit=Long.MIN_VALUE/2
    private var overlayFailed = false
    private var breakFinish:Runnable?=null
    private var limitScreen:Runnable?=null
    private fun cancelLimitScreen(){limitScreen?.let{handler.removeCallbacks(it)};limitScreen=null}
    private fun scheduleBreakFinish() {
        breakFinish?.let{handler.removeCallbacks(it)}
        breakFinish=null
        val until=store.pauseUntil
        if(until<=0)return
        val task=Runnable{BreakReminders.finish(this,until)}
        breakFinish=task
        handler.postDelayed(task,(until-System.currentTimeMillis()).coerceAtLeast(0L))
    }
    private val eventInspection = Runnable { inspect() }
    private val tick = object : Runnable {
        override fun run() { inspect(); if(!destroyed && heartbeatRequired() && !handler.hasCallbacks(this)) handler.postDelayed(this,if(active==null)1200 else 650) }
    }
    private fun heartbeatRequired()=::store.isInitialized && trackingHeartbeatRequired(
        store.running,store.consent,selectedIds.isNotEmpty(),store.pauseUntil>System.currentTimeMillis())
    override fun onServiceConnected() {
        if(destroyed)return
        if(!::store.isInitialized)store = TrackerStore(this)
        CompanionBodyCache.warm()
        scanGate.connect()
        handler.removeCallbacks(tick);handler.removeCallbacks(eventInspection);cancelLimitScreen()
        recordUsage(null,true);usageClock.reset();tracker.reset();clearPager();active=null;hide()
        speaker?.close()
        store.startAfterConnection()
        speaker=MisoSpeaker(this,onSpeakingChanged={talking->companion?.talking=talking}){key->store.prefs.edit().putBoolean(key,true).apply()}
        store.prefs.registerOnSharedPreferenceChangeListener(this)
        restrictEventPackages()
        scheduleBreakFinish()
        MisoServiceConnection.onConnected()
        handler.post(tick)
    }
    private fun restrictEventPackages() {
        PlatformCatalog.refresh(store)
        val tracked=store.trackedPlatforms
        selectedIds=tracked.map{it.id}.toSet()
        monitoredByPackage=tracked.flatMap{p->p.packages.map{it to p}}.toMap()
        val packages=monitoredByPackage.keys
        // An empty packageNames array means "all apps" to Android. Use our own
        // package as a harmless placeholder when no platforms are selected.
        val info=serviceInfo ?: return
        if(Build.VERSION.SDK_INT>=34)info.eventTypes=info.eventTypes or AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL
        info.packageNames=if(packages.isEmpty()) arrayOf(packageName) else packages.toTypedArray()
        serviceInfo=info
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::store.isInitialized || event == null || destroyed || !scanGate.connected) return
        val pkg=event.packageName?.toString() ?: return
        if(pkg == packageName) return
        val p=monitoredByPackage[pkg]
        if(p == null && active == null) return
        if(!store.running || !store.consent || store.pauseUntil>System.currentTimeMillis()) return
        val playerEvent=event.eventType==AccessibilityEvent.TYPE_VIEW_SCROLLED || event.eventType==AccessibilityEvent.TYPE_VIEW_SELECTED ||
            (p?.id=="youtube" && Build.VERSION.SDK_INT>=34 && event.eventType==AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL)
        if(!store.allowanceReached() && playerEvent && p != null && store.running && store.consent) capturePager(event, pkg)
        // Content/window events also request a fresh tree. Never retain an event/node.
        // A request arriving while the worker runs must not be dropped.
        if(busy)scanRequested=true
        if (!handler.hasCallbacks(eventInspection)) handler.postDelayed(eventInspection, 45)
    }
    private fun capturePager(event: AccessibilityEvent, pkg: String) {
        val source=event.source ?: return
        try {
            val bounds=Rect(); source.getBoundsInScreen(bounds)
            val screen=resources.displayMetrics
            val name=source.className?.toString().orEmpty().lowercase()
            val id=source.viewIdResourceName.orEmpty().lowercase()
            if(listOf("comment","replies","reply_list","chat","bottom_sheet","seek","time_bar","slider").any{it in id})return
            val pager=name.contains("pager") || listOf("reel", "shorts", "clips", "feed_pager", "video_pager").any { it in id }
            val full=bounds.height() > screen.heightPixels*.57f && bounds.width() > screen.widthPixels*.72f
            // Never convert comments, horizontal carousels, or normal list scrolls into reels.
            if(!full || (!pager && !name.contains("recycler") && !source.isScrollable)) return
            if(isHorizontalPlayerScroll(event.scrollDeltaX,event.scrollDeltaY)) return
            if(pkg != scrollPackage) { clearPager(); scrollPackage=pkg }
            val completedTarget=Build.VERSION.SDK_INT>=34 && event.eventType==AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL
            pagerState.observe(event.fromIndex,event.toIndex,event.scrollY,event.scrollDeltaY,bounds.height(),SystemClock.elapsedRealtime(),event.currentItemIndex,playerScrollNotification=pager && (event.eventType==AccessibilityEvent.TYPE_VIEW_SCROLLED || completedTarget))
            if(pkg=="com.google.android.youtube" && pager &&
                (event.eventType==AccessibilityEvent.TYPE_VIEW_SCROLLED || completedTarget || event.currentItemIndex>=0)) {
                youtubePackage=pkg
                // Adapter indices and scrollY may reset when YouTube recycles a page.
                // Only an explicit relative delta establishes direction. Without it,
                // changed stable metadata is required; never invent an absolute page.
                youtube.scroll(scrollDeltaOrZero(event.scrollDeltaY).compareTo(0),null)
                logYoutubeEvent(event)
            }
        } finally { source.recycle() }
    }
    private fun clearPager() { scrollPackage="";pagerState.reset();tracker.reset();youtube.resetSession();youtubePackage="" }
    private fun root(): AccessibilityNodeInfo? {
        rootInActiveWindow?.let { if(it.packageName?.toString() != packageName) return it else it.recycle() }
        val currentWindows=windows
        return try {
            currentWindows.filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && (it.isFocused || it.isActive) }
                .sortedByDescending { it.layer }.firstNotNullOfOrNull { it.root }
        } finally {currentWindows.forEach{it.recycle()}}
    }
    private fun trackingAllowed()=scanGate.connected && ::store.isInitialized && store.running && store.consent &&
        selectedIds.isNotEmpty() && store.pauseUntil<=System.currentTimeMillis() &&
        !getSystemService(KeyguardManager::class.java).isKeyguardLocked && getSystemService(PowerManager::class.java).isInteractive
    private fun foregroundPackage():String? {
        val node=runCatching{root()}.getOrNull() ?: return null
        return try{node.packageName?.toString()}finally{node.recycle()}
    }
    private fun inspect() {
        if(destroyed || !scanGate.connected || !::store.isInitialized) return
        if(store.pauseUntil>0 && store.pauseUntil<=System.currentTimeMillis()){BreakReminders.finish(this,store.pauseUntil);return}
        if (!trackingAllowed()) {
            recordUsage(null,true);tracker.reset(); clearPager(); active=null; hide(); return
        }
        if (busy) {scanRequested=true;return}
        val now=SystemClock.elapsedRealtime()
        val wait=90-(now-lastInspectAt)
        if(wait>0) {
            if(!handler.hasCallbacks(eventInspection)) handler.postDelayed(eventInspection,wait)
            return
        }
        lastInspectAt=now
        busy = true
        scanRequested=false
        val youtubeGeneration=youtube.generation
        val ticket = scanGate.ticket()
        val selected = selectedIds
        val detectors = monitoredByPackage
        executor.execute {
            val result = runCatching {
                if(!scanGate.accepts(ticket))return@runCatching null
                val root = root() ?: return@runCatching null
                try {
                    val p = detectors[root.packageName?.toString()]?.takeIf{it.id in selected} ?: return@runCatching null
                    val nodes=snapshotReelNodes(root,resources.displayMetrics.widthPixels,resources.displayMetrics.heightPixels,p.id=="youtube"){!scanGate.accepts(ticket)}
                    Triple(p, root.packageName.toString(), PackageDetectors.recognize(p.detectorId,nodes))
                } finally { root.recycle() }
            }.getOrNull()
            handler.post {
                busy=false
                if(scanRequested && !handler.hasCallbacks(eventInspection))handler.post(eventInspection)
                if(destroyed || !scanGate.accepts(ticket)) return@post
                if(!trackingAllowed()) {recordUsage(null,true);usageClock.reset();tracker.reset();clearPager();active=null;hide();return@post}
                if(result == null) {
                    recordUsage(null,true)
                    active?.let { store.status(it.id,"background") }; active=null; tracker.reset(); clearPager(); hide(); return@post
                }
                val (p,pkg,frame)=result
                if(p.id !in selectedIds)return@post
                if(frame.recognized && store.allowanceReached()){enforceLimit(p);return@post}
                val obscured=warningGlow!=null && store.limitTint && store.allowanceProgress()>=LIMIT_CURTAIN_FRACTION
                recordUsage(if(frame.recognized && !obscured) p.id else null,!frame.recognized || obscured)
                if(active?.id != p.id) { tracker.reset(); if(scrollPackage != pkg && p.id!="youtube") clearPager() }
                if(p.id!="youtube" && youtubePackage.isNotEmpty()){youtube.resetSession();youtubePackage=""}
                active=p
                val now=SystemClock.elapsedRealtime()
                val pager=if(scrollPackage == pkg) pagerState.token ?: "page:0" else "page:0"
                // Metadata and scroll events can arrive in either order.
                val counted=if(!store.allowanceReached() && !obscured) {
                    if(p.id=="youtube") {
                        youtubePackage=pkg
                        youtube.observe(frame,now,LocalDate.now().toString(),youtubeGeneration,store.compatibility)
                    } else now-pagerState.scrolledAt>=65 && tracker.observe(p.id,frame,pager,store.compatibility,pagerState.position,pagerState.direction,pagerState.changedAt,now,LocalDate.now().toString(),pagerState.unindexedScroll)
                } else false
                if(p.id=="youtube")logYoutube(frame,counted,now)
                if(counted) {
                    store.increment(p.id)
                    companion?.reactToCount()
                }
                if(frame.recognized && store.allowanceReached()){enforceLimit(p);return@post}
                if(frame.recognized) {show(p,frame);nudge()} else hide()
                store.status(p.id, when { frame.reason=="accessibility_unavailable" -> "accessibility_unavailable"; overlayFailed -> "overlay_failed"; !frame.recognized -> "waiting_player"; obscured -> "limit_covered"; frame.fingerprint != null -> "tracking"; store.compatibility -> "pager_mode"; else -> "missing_labels" })
                if((if(p.id=="youtube")youtube.pending else tracker.pending) && !handler.hasCallbacks(eventInspection)) handler.postDelayed(eventInspection,80)
            }
        }
    }
    private fun logYoutubeEvent(event:AccessibilityEvent) {
        if(!Log.isLoggable(YOUTUBE_LOG_TAG,Log.DEBUG))return
        Log.d(YOUTUBE_LOG_TAG,"event=${event.eventType} generation=${youtube.generation} from=${event.fromIndex} to=${event.toIndex} current=${event.currentItemIndex} dy=${event.scrollDeltaY} indexed=false")
    }
    /** Opt-in via adb logcat tag; no captions, creators, URLs or identity hashes are logged. */
    private fun logYoutube(frame:ReelFrame,counted:Boolean,now:Long) {
        if(!Log.isLoggable(YOUTUBE_LOG_TAG,Log.DEBUG))return
        val state="${youtube.diagnostic}:${frame.reason}"
        if(counted || (state!=lastYoutubeDiagnostic && now-lastYoutubeLogAt>=250)) {
            Log.d(YOUTUBE_LOG_TAG,"state=$state generation=${youtube.generation} count=$counted")
            lastYoutubeDiagnostic=state;lastYoutubeLogAt=now
        }
    }
    private fun enforceLimit(platform:Platform) {
        // A scan may finish after the user has left the selected player. Never
        // send Home or pause another app based on that old frame.
        if(!trackingAllowed() || platform.id !in selectedIds || foregroundPackage() !in platform.packages)return
        recordUsage(null,true);tracker.reset();clearPager()
        store.status(platform.id,"limit_reached")
        val now=SystemClock.elapsedRealtime()
        if(now-lastLimitExit<2000)return
        lastLimitExit=now
        nudge();hide();pauseCurrentMedia(this)
        // Remove the player from the foreground, rather than leaving a scrollable feed
        // behind a non-touchable overlay. System Back/Home remain available.
        performGlobalAction(GLOBAL_ACTION_HOME)
        cancelLimitScreen()
        val ticket=scanGate.ticket()
        val task=Runnable{
            limitScreen=null
            if(!destroyed && scanGate.accepts(ticket) && trackingAllowed() && platform.id in selectedIds && store.allowanceReached()) {
                runCatching{startActivity(Intent(this,LimitReachedActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))}
            }
        }
        limitScreen=task;handler.postDelayed(task,180)
    }
    private fun nudge() {
        val stage=LimitStage.at((store.allowanceProgress()*100).toInt(),100)
        if(stage==LimitStage.NONE || (!store.goalReminders && !store.limitNotifications))return
        val key="nudged:${LocalDate.now()}:${store.goal}:${store.dailyMinutes}:v3:${stage.name.lowercase()}"
        if(!store.prefs.getBoolean(key,false)) {
            store.prefs.edit().putBoolean(key,true).apply()
            GoalNotifications.show(this,stage,store.total(),store.goal)
            if(store.haptics)runCatching{getSystemService(Vibrator::class.java).vibrate(VibrationEffect.createWaveform(longArrayOf(0,35,80,35),-1))}
        }
        val speechKey="$key:spoken"
        if(store.goalReminders && speaker?.ready==true && !store.prefs.getBoolean(speechKey,false) && speaker?.pendingWarning!=speechKey)speaker?.warn(stage,speechKey)
    }

    private fun show(p: Platform, frame: ReelFrame) {
        if(overlay == null) {
            val box=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                companion=CompanionView(context); addView(companion,LinearLayout.LayoutParams(dp(store.overlaySize),dp(store.overlaySize)))
                label=TextView(context).apply {
                    setTextColor(0xFFFFFFFF.toInt()); textSize=42f; gravity=Gravity.CENTER
                    typeface=android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD)
                    fontFeatureSettings="tnum"; includeFontPadding=false
                    setShadowLayer(dp(3).toFloat(),0f,dp(1).toFloat(),0xF0000000.toInt())
                    setPadding(dp(5),0,dp(5),dp(2))
                }; addView(label)
                contextLabel=TextView(context).apply { textSize=9f; gravity=Gravity.CENTER; setTextColor(0xFFD9E9E2.toInt()); setShadowLayer(2f,0f,1f,0xCC081C20.toInt()) }
                addView(contextLabel)
            }
            try { getSystemService(WindowManager::class.java).addView(box,params()); overlay=box; overlayFailed=false }
            catch (_: RuntimeException) { overlayFailed=true; return }
        }
        // One coherent preference snapshot per overlay update, rather than repeatedly
        // summing every platform for each label and reaction property.
        val total=store.total()
        val goal=store.goal
        val progress=dailyAllowanceProgress(total,goal,store.totalUsage(),store.dailyMinutes)
        val stage=LimitStage.at((progress*100).toInt(),100)
        val over=stage!=LimitStage.NONE && store.limitTint
        if(over && warningGlow==null) {
            val glow=LimitGlowView(this)
            val layout=WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,PixelFormat.TRANSLUCENT)
            runCatching {getSystemService(WindowManager::class.java).addView(glow,layout);warningGlow=glow}
        }
        if(!over)removeGlow()
        warningGlow?.originY=dp(store.overlayTop+store.overlaySize/2).toFloat()
        warningGlow?.reducedMotion=store.reducedMotion
        warningGlow?.progress=progress
        if(warningGlow!=null && progress>=LIMIT_CURTAIN_FRACTION)recordUsage(null,true)
        companion?.apply { alpha=store.opacity; animated= !store.reducedMotion;talking=speaker?.speaking==true; activity=CompanionActivity.ACTIVE; expressive=store.expressions; this.progress=progress }
        label?.alpha=store.counterOpacity
        val count=if(store.remainingMode) (goal-total).coerceAtLeast(0) else store.count(p.id)
        if(label?.text?.toString() != count.toString()) {
            label?.text=count.toString()
            if(!store.reducedMotion && android.animation.ValueAnimator.areAnimatorsEnabled()) {
                label?.animate()?.cancel(); label?.scaleX=1.08f;label?.scaleY=1.08f
                label?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(140)?.start()
            }
        }
        label?.setTextColor(if(total>=goal) 0xFFFFB9CA.toInt() else 0xFFFFFFFF.toInt())
        label?.contentDescription="${p.name}, $count ${if(store.remainingMode) "remaining today" else "estimated reels"}"
        contextLabel?.apply { visibility=if(store.showPlatform || stage!=LimitStage.NONE) View.VISIBLE else View.GONE; alpha=store.counterOpacity
            val next=when(stage){LimitStage.NEAR->"Near your daily limit";LimitStage.REACHED->"Daily limit reached";LimitStage.EXCEEDED->"Limit crossed · take a break";else->if(store.remainingMode) "left today" else p.name}
            textSize=if(stage==LimitStage.NONE)9f else 11f
            if(text.toString()!=next) text=next }
    }
    private fun params()=WindowManager.LayoutParams(dp(168),WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; y=dp(store.overlayTop) }
    private fun removeGlow() {warningGlow?.let{runCatching{getSystemService(WindowManager::class.java).removeView(it)}};warningGlow=null}
    private fun hide() {removeGlow();speaker?.stop(); overlay?.let { runCatching { getSystemService(WindowManager::class.java).removeView(it) } };overlay=null;companion=null;label=null;contextLabel=null }
    private fun dp(n: Int)=(n*resources.displayMetrics.density).toInt()
    override fun onSharedPreferenceChanged(p: SharedPreferences?, key: String?) {
        if(key=="platforms" || key==CustomPlatforms.KEY)restrictEventPackages()
        if(key=="pauseUntil")scheduleBreakFinish()
        if(key=="refreshUsageRequest") {recordUsage(null,true);scanGate.invalidate();cancelLimitScreen();usageClock.reset();active=null;hide()}
        if(key=="activityEpoch") {scanGate.invalidate();cancelLimitScreen();usageClock.reset();usagePending=null;tracker.clear();youtube.clear()}
        if(key in setOf("running","platforms",CustomPlatforms.KEY,"consent","compatibility","pauseUntil")) { recordUsage(null,true); scanGate.invalidate();cancelLimitScreen();tracker.reset(); clearPager(); hide(); active=null }
        if(key in setOf("running","platforms",CustomPlatforms.KEY,"consent","pauseUntil")) {
            handler.removeCallbacks(tick)
            if(heartbeatRequired())handler.post(tick)
        }
        if(key in setOf("overlaySize","overlayTop")) hide()
        if(key=="goalReminders" && !store.goalReminders)speaker?.stop()
        // Counter writes originate in inspect(); rescanning for those writes creates a feedback loop.
        if(key!=null && key!="lastUsageUpdate" && !key.startsWith("status:") && !key.startsWith("count:") && !key.startsWith("usage:") && !key.startsWith("nudged:") && !handler.hasCallbacks(eventInspection)) handler.post(eventInspection)
    }
    override fun onInterrupt() {
        scanGate.invalidate();cancelLimitScreen()
        if(::store.isInitialized)recordUsage(null,true)
        usageClock.reset();tracker.reset();clearPager();active=null;hide()
    }
    private fun disconnectTracking() {
        scanGate.disconnect()
        MisoServiceConnection.onDisconnected()
        if(::store.isInitialized) recordUsage(null,true)
        handler.removeCallbacks(tick);handler.removeCallbacks(eventInspection);cancelLimitScreen()
        breakFinish?.let{handler.removeCallbacks(it)};breakFinish=null
        usageClock.reset();tracker.reset();clearPager();active=null;hide()
        if(::store.isInitialized) store.prefs.unregisterOnSharedPreferenceChangeListener(this)
        speaker?.close();speaker=null
    }
    override fun onUnbind(intent:Intent?):Boolean {
        disconnectTracking()
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        destroyed=true;disconnectTracking();handler.removeCallbacksAndMessages(null);executor.shutdownNow()
        super.onDestroy()
    }
}
