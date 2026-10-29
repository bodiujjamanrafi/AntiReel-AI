package com.rafi.antireelai

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val LocalMisoMotionPaused=compositionLocalOf{false}
private val Ink get()=AppStyle.Background
private val Panel get()=AppStyle.Surface
private val Muted get()=AppStyle.Muted
private val Seafoam get()=AppStyle.Accent
private val Copper get()=AppStyle.Accent
private val Paper get()=AppStyle.Text

class MainActivity: ComponentActivity() {
    private lateinit var store:TrackerStore
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState);CompanionBodyCache.warm();enableEdgeToEdge(statusBarStyle=SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT),navigationBarStyle=SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT));store=TrackerStore(this)
        setContent {
            var mode by remember {mutableStateOf(store.themeMode)}
            var language by remember{mutableStateOf(store.prefs.getString("language","en") ?: "en")}
            DisposableEffect(Unit) {
                val listener=SharedPreferences.OnSharedPreferenceChangeListener {_,key->if(key=="themeMode")mode=store.themeMode;if(key=="language")language=store.prefs.getString("language","en") ?: "en"}
                store.prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {store.prefs.unregisterOnSharedPreferenceChangeListener(listener)}
            }
            val dark=when(mode){"dark"->true;"light"->false;else->isSystemInDarkTheme()}
            val colors=AppStyle.colorsFor(dark)
            SideEffect {
                AppStyle.dark=dark
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(colors.background.toArgb()))
                val bars=if(dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT) else SystemBarStyle.light(android.graphics.Color.TRANSPARENT,android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle=bars,navigationBarStyle=bars)
            }
            val palette=(if(dark)darkColorScheme() else lightColorScheme()).copy(primary=colors.accent,onPrimary=if(dark)Color(0xFF4B1028) else Color.White,background=colors.background,surface=colors.surface,onSurface=colors.text,onBackground=colors.text,onSurfaceVariant=colors.muted,outline=colors.outline)
            MaterialTheme(colorScheme=palette) { CompositionLocalProvider(LocalContentColor provides palette.onBackground,LocalUiLanguage provides language) {
                var onboarding by remember {mutableStateOf(!store.prefs.getBoolean("onboardingComplete",false))}
                DisposableEffect(Unit){val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,key->if(key=="onboardingRestartRequested" && store.prefs.getBoolean(key,false))onboarding=true};store.prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{store.prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
                if(onboarding)OnboardingFlow(store){store.prefs.edit().putBoolean("onboardingNeedsSetup",!connected() || !store.consent).putBoolean("onboardingRestartRequested",false).apply();onboarding=false} else App()
            } }
        }
    }
    private fun openMisoAccessibility(){
        if(!DeviceSettings.open(this,DeviceSettings.accessibilityIntent()))
            android.widget.Toast.makeText(this,"Open Android Settings → Accessibility → AntiReel AI.",android.widget.Toast.LENGTH_LONG).show()
    }
    private fun openAppInfo(){
        if(!DeviceSettings.open(this,DeviceSettings.appInfoIntent(packageName)))
            android.widget.Toast.makeText(this,"Open Android Settings → Apps → AntiReel AI → App info.",android.widget.Toast.LENGTH_LONG).show()
    }
    private fun connected()=getSystemService(AccessibilityManager::class.java).getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
        it.resolveInfo.serviceInfo.let { service -> service.packageName==packageName && service.name==com.rafi.antireelai.tracking.ReelAccessibilityService::class.java.name }
    }
    @Composable private fun App() {
        val speaker=remember {MisoSpeaker(this@MainActivity)}
        var foreground by remember {mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
        var navigationVisible by remember {mutableStateOf(true)}
        var activityDay by rememberSaveable {mutableStateOf(LocalDate.now().toString())}
        var settingsSection by remember {mutableStateOf<String?>(null)}
        val legalNavigationScope=rememberCoroutineScope()
        var settingsPlatforms by remember {mutableStateOf(false)}
        var tab by remember { mutableIntStateOf(0) }
        var editingAnswers by remember {mutableStateOf(false)}
        LaunchedEffect(Unit){store.prefs.edit().remove("onboardingNeedsSetup").apply()}
        var revision by remember { mutableIntStateOf(0) }
        var activityRevision by remember { mutableIntStateOf(0) }
        var enabled by remember { mutableStateOf(connected()) }
        // Binding can finish after Activity.onResume. React to the actual
        // service callback too, including Android 11/12 without API 33 listeners.
        LaunchedEffect(MisoServiceConnection.connected){enabled=connected()}
        var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var showWelcome by rememberSaveable { mutableStateOf(!store.welcomeSeen) }
        var focusMode by rememberSaveable {mutableStateOf(false)}
        var quickBreak by rememberSaveable {mutableStateOf(false)}
        var editGoal by rememberSaveable {mutableStateOf(false)}
        var erase by remember { mutableStateOf(false) }
        var installRevision by remember { mutableIntStateOf(0) }
        var platformQuery by rememberSaveable { mutableStateOf("") }
        var platformTools by rememberSaveable {mutableStateOf(false)}
        var platformFilter by rememberSaveable { mutableIntStateOf(0) }
        val installed by produceState<Set<String>>(emptySet(),installRevision) {
            value=withContext(Dispatchers.IO) { platforms.filter { p -> p.packages.any { pkg -> runCatching { packageManager.getApplicationInfo(pkg,0);true }.getOrDefault(false) } }.map { it.id }.toSet() }
        }
        var tabHistory by remember {mutableStateOf(intArrayOf())}
        BackHandler(enabled=(tab==3 && settingsSection!=null) || tabHistory.isNotEmpty() || tab!=0) {
            if(tab==1 && settingsPlatforms){settingsPlatforms=false;tab=3;if(tabHistory.lastOrNull()==3)tabHistory=tabHistory.dropLast(1).toIntArray()}
            else if(tab==3 && settingsSection!=null)settingsSection=null
            else if(tabHistory.isNotEmpty()){tab=tabHistory.last();tabHistory=tabHistory.dropLast(1).toIntArray()}
            else tab=0
            navigationVisible=true
        }
        val notificationPermission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(granted)BreakReminders.refresh(this@MainActivity)}
        val view=LocalView.current
        fun tap() { if(store.haptics) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }
        fun change(index:Int) { if(index!=1)settingsPlatforms=false; if(index!=tab) { tap();tabHistory=(tabHistory.toList()+tab).takeLast(16).toIntArray();tab=index;navigationVisible=true } }
        val openMisoPage by rememberUpdatedState(newValue={change(AppDestination.Miso.ordinal)})
        val navigationScroll=remember {
            object:NestedScrollConnection {
                var distance=0f
                override fun onPostScroll(consumed:Offset,available:Offset,source:NestedScrollSource):Offset {
                    if(source==NestedScrollSource.UserInput) {
                        if(tab==3 || (tab==1 && settingsPlatforms)){distance=0f;return Offset.Zero}
                        if(consumed.y*distance<0) distance=0f
                        distance+=consumed.y
                        if(distance < -28f) navigationVisible=false
                        if(distance > 16f) navigationVisible=true
                    }
                    return Offset.Zero
                }
            }
        }
        LaunchedEffect(tab) {navigationVisible=true}
        LaunchedEffect(speaker.ready,foreground,store.voiceEnabled) {
            if(foreground && store.voiceEnabled && !store.voiceGreeted && speaker.ready && speaker.greet()) store.voiceGreeted=true
        }
        DisposableEffect(Unit) {
            val accessManager=getSystemService(AccessibilityManager::class.java)
            val accessListener=AccessibilityManager.AccessibilityStateChangeListener { enabled=connected() }
            accessManager.addAccessibilityStateChangeListener(accessListener)
            val servicesListener=if(android.os.Build.VERSION.SDK_INT>=33)
                AccessibilityManager.AccessibilityServicesStateChangeListener { enabled=connected() } else null
            if(android.os.Build.VERSION.SDK_INT>=33 && servicesListener!=null)
                accessManager.addAccessibilityServicesStateChangeListener(mainExecutor,servicesListener)
            val changes=PreferenceUiUpdates()
            val choreographer=android.view.Choreographer.getInstance()
            var refreshScheduled=false
            var packagesDirty=false
            val refreshFrame=android.view.Choreographer.FrameCallback {
                refreshScheduled=false
                val flags=changes.drain()
                if(foreground) {
                    if(flags and PreferenceUiUpdates.ACTIVITY !=0)activityRevision++
                    if(flags and PreferenceUiUpdates.SETTINGS !=0)revision++
                }
            }
            val listener=SharedPreferences.OnSharedPreferenceChangeListener { _,key ->
                // A background service can write several usage keys per transaction.
                // Hidden UI refreshes on resume; visible UI refreshes once per frame.
                if(foreground && changes.collect(key) && !refreshScheduled) {
                    refreshScheduled=true;choreographer.postFrameCallback(refreshFrame)
                }
            }
            val packages=object:android.content.BroadcastReceiver() {
                override fun onReceive(context:android.content.Context?,intent:Intent?) {
                    val pkg=intent?.data?.schemeSpecificPart ?:return
                    if(intent.getBooleanExtra(Intent.EXTRA_REPLACING,false))return
                    if(platforms.none{pkg in it.packages})return
                    if(foreground)installRevision++ else packagesDirty=true
                }
            }
            val packageFilter=android.content.IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED);addAction(Intent.ACTION_PACKAGE_REMOVED);addDataScheme("package")
            }
            androidx.core.content.ContextCompat.registerReceiver(this@MainActivity,packages,packageFilter,androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)
            val observer=LifecycleEventObserver { _,event ->
                if(event==Lifecycle.Event.ON_RESUME) {
                    BreakReminders.refresh(this@MainActivity);foreground=true;enabled=connected()
                    if(enabled){store.startAfterConnection()}
                    if(store.prefs.getBoolean("awaitingAccessSetup",false)) {
                        store.prefs.edit().remove("awaitingAccessSetup").apply()
                        if(!enabled){openMisoPage()}
                    }
                    clock=System.currentTimeMillis();revision++;activityRevision++
                    if(packagesDirty){packagesDirty=false;installRevision++}
                    store.prefs.edit().putLong("refreshUsageRequest",android.os.SystemClock.elapsedRealtime()).apply()
                }
                if(event==Lifecycle.Event.ON_PAUSE) {foreground=false;speaker.stop();choreographer.removeFrameCallback(refreshFrame);refreshScheduled=false;changes.drain()}
            }
            store.prefs.registerOnSharedPreferenceChangeListener(listener);lifecycle.addObserver(observer)
            onDispose {
                choreographer.removeFrameCallback(refreshFrame);unregisterReceiver(packages)
                accessManager.removeAccessibilityStateChangeListener(accessListener)
                if(android.os.Build.VERSION.SDK_INT>=33 && servicesListener!=null)
                    accessManager.removeAccessibilityServicesStateChangeListener(servicesListener)
                speaker.close();store.prefs.unregisterOnSharedPreferenceChangeListener(listener);lifecycle.removeObserver(observer)
            }
        }
        @Suppress("UNUSED_VARIABLE") val refreshed=revision
        val selected=remember(revision){store.selected}
        val total=remember(revision,clock){store.total()}
        val pauseUntil=store.pauseUntil
        LaunchedEffect(pauseUntil) {
            clock=System.currentTimeMillis()
            val remaining=pauseUntil-clock
            if(remaining>0) { delay(remaining);clock=System.currentTimeMillis();BreakReminders.finish(this@MainActivity,pauseUntil) }
        }
        val paused=pauseUntil>clock
        val running=canTrackReels(enabled,MisoServiceConnection.connected,store.consent,store.running,paused,selected.isNotEmpty())
        fun toggleCompanion() {
            tap()
            if(running){store.running=false;speaker.stop()}
            else if(selected.isEmpty())change(1)
            else if(!store.consent || !enabled)change(AppDestination.Miso.ordinal)
            else{
                store.pauseUntil=0;store.running=true
                if(android.os.Build.VERSION.SDK_INT>=33 && store.limitNotifications && !BreakReminders.allowed(this@MainActivity))notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        fun openPlatform(p:Platform) {
            tap()
            val launch=p.packages.firstNotNullOfOrNull{packageManager.getLaunchIntentForPackage(it)}
            if(launch!=null)startActivity(launch)
            else {
                val id=p.packages.first()
                val market=Intent(Intent.ACTION_VIEW,android.net.Uri.parse("market://details?id=$id"))
                if(market.resolveActivity(packageManager)!=null)startActivity(market)
                else startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://play.google.com/store/apps/details?id=$id")))
            }
        }
        val today=LocalDate.now()
        LaunchedEffect(today) {
            val next=today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            delay((next-System.currentTimeMillis()).coerceAtLeast(1000L));clock=System.currentTimeMillis();revision++
        }
        LaunchedEffect(today) {if(LocalDate.parse(activityDay).isBefore(today.minusDays(29)) || LocalDate.parse(activityDay).isAfter(today)) activityDay=today.toString()}
        val historyDays=historyDaysForScreen(tab)
        val loadHistory=foreground && historyDays>0
        val activityHistory by produceState(emptyList<ActivityDay>(),activityRevision,today,historyDays,loadHistory) {
            if(!loadHistory)return@produceState
            value=withContext(Dispatchers.Default) {
                val values=store.prefs.all
                (0 until historyDays).map {ActivityDay.read(today.minusDays(it.toLong()),values)}
            }
        }
        val todayData=activityHistory.firstOrNull() ?: remember(today){ActivityDay(today,List(platforms.size){0},List(platforms.size){0L})}
        val scrollStates=AppDestination.entries.map { rememberLazyListState() }
        val settingsScrollStates=(0..SettingsSection.entries.size).map{rememberLazyListState()}
        val settingsPlatformsScroll=rememberLazyListState()
        val activeScrollState=when {
            tab==3->settingsScrollStates[(SettingsSection.entries.indexOfFirst{it.name==settingsSection}+1).coerceAtLeast(0)]
            tab==1 && settingsPlatforms->settingsPlatformsScroll
            else->scrollStates[tab]
        }
        LaunchedEffect(settingsSection) {navigationVisible=true}
        if(editingAnswers) androidx.compose.ui.window.Dialog(onDismissRequest={editingAnswers=false},properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
            OnboardingFlow(store,startReview=true,onExit={editingAnswers=false}){editingAnswers=false}
        }
        Scaffold(containerColor=Ink,contentColor=Paper) { insets ->
            Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(painter=androidx.compose.ui.res.painterResource(R.drawable.miso_brand_png),contentDescription="AntiReel · Miso bunny logo",modifier=Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)))
                    Column(Modifier.weight(1f).padding(start=6.dp)) {
                        UiText("antireel",fontSize=23.sp,fontWeight=FontWeight.Bold,letterSpacing=(-1).sp,color=Paper)
                    }
                    CompanionStatus(running,paused)
                    IconButton(onClick={tap();settingsSection=SettingsSection.ALERTS.name;change(3)},modifier=Modifier.size(36.dp)) {AppGlyph(13,Paper,Modifier.size(22.dp))}
                }
                PageMotion(tab,if(tab==1 && settingsPlatforms)"platform-manager" else if(tab==3)settingsSection else null,motionReduced(store.reducedMotion),Modifier.weight(1f)) {
                    val section=tab
                    CompositionLocalProvider(LocalMisoMotionPaused provides activeScrollState.isScrollInProgress) {
                    if(section==AppDestination.Miso.ordinal) {
                        MisoPage(store,speaker,enabled,running,paused,
                            modifier=Modifier.fillMaxSize().padding(horizontal=22.dp).padding(bottom=if(navigationVisible)124.dp else 12.dp),
                            onAgree={tap();store.requestActivation();store.prefs.edit().putBoolean("awaitingAccessSetup",true).apply();openMisoAccessibility()},
                            onManage={tap();openMisoAccessibility()},onToggle=::toggleCompanion,onPlatforms={change(1)})
                    } else LazyColumn(Modifier.fillMaxSize().then(if(section==3 || (section==1 && settingsPlatforms))Modifier.padding(bottom=112.dp).imePadding() else Modifier).nestedScroll(navigationScroll),state=activeScrollState,contentPadding=PaddingValues(start=if(section==3 || (section==1 && settingsPlatforms))16.dp else 22.dp,end=if(section==3 || (section==1 && settingsPlatforms))16.dp else 22.dp,top=12.dp,bottom=if(section==3 || (section==1 && settingsPlatforms))24.dp else if(navigationVisible) 124.dp else 24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        when(section) {
                            0 -> {
                                item("heading") {TodayHeadline {Mascot(store,total,Modifier.fillMaxSize(),talking=speaker.speaking)}}
                                item("today-metrics") {TodayMetrics(total,store.goal,todayData.totalMillis,store.dailyMinutes)}
                                item("miso-session") {
                                    HomeCompanionPanel(running,paused,speaker.ready,{tap();speaker.greet()},{if(running)toggleCompanion() else change(AppDestination.Miso.ordinal)})
                                }
                                if(paused)item("break-timer"){BreakTimer(pauseUntil){tap();store.pauseUntil=0}}
                                item("selected-apps") {SelectedApps(platforms.filter{it.id in selected},todayData.reels,{change(1)}) {p->
                                    tap()
                                    val intent=p.packages.firstNotNullOfOrNull{packageManager.getLaunchIntentForPackage(it)}
                                    if(intent!=null) startActivity(intent) else change(1)
                                }}
                                item("quick-actions") {
                                    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                        ReferenceSectionTitle("Quick access",tint=AppStyle.Violet)
                                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                            ReferenceQuickAction("Focus mode",5,AppStyle.Teal,Modifier.weight(1f)){tap();focusMode=true}
                                            ReferenceQuickAction("Break time",16,AppStyle.Coral,Modifier.weight(1f)){tap();if(paused)store.pauseUntil=0 else quickBreak=true}
                                        }
                                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                            ReferenceQuickAction("Daily goal",5,AppStyle.Blue,Modifier.weight(1f)){tap();editGoal=true}
                                            ReferenceQuickAction("Counter style",18,AppStyle.Accent,Modifier.weight(1f)){tap();settingsSection=SettingsSection.MISO.name;change(3)}
                                        }
                                    }
                                }
                                item("recent-days") {RecentDays(activityHistory){day->activityDay=day.toString();change(2)}}
                                if(showWelcome) item("miso-welcome") {MisoWelcome(store,speaker.speaking,{if(store.voiceEnabled)speaker.greet()}) {tap();store.welcomeSeen=true;showWelcome=false}}
                            }
                            1 -> {
                                if(settingsPlatforms) {
                                    item("settings-platforms") {
                                        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.TopCenter) {
                                            SettingsPlatforms(platformQuery,{platformQuery=it},PlatformFilter.entries.getOrElse(platformFilter){PlatformFilter.ALL},{tap();platformFilter=it.ordinal},selected,installed,todayData.reels,running,{statusLabel(store.status(it))},
                                                {p,yes->tap();store.selected=if(yes)selected+p.id else selected-p.id},::openPlatform,
                                                onBack={tap();onBackPressedDispatcher.onBackPressed()},
                                                onSelectInstalled={tap();store.selected=selected+installed},onClear={tap();store.selected=emptySet()},
                                                onSelectVisible={tap();store.selected=selected+filterPlatforms(platforms,platformQuery,PlatformFilter.entries.getOrElse(platformFilter){PlatformFilter.ALL},selected,installed).map{it.id}})
                                        }
                                    }
                                } else {
                                item("heading") {ReferenceHero("","Your apps.","Your boundaries.","",compact=true){Mascot(store,total,Modifier.fillMaxSize(),talking=speaker.speaking)}}
                                item("filters") {
                                    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(value=platformQuery,onValueChange={platformQuery=it},placeholder={UiText("Search platforms…",fontSize=13.sp)},singleLine=true,modifier=Modifier.weight(1f),shape=RoundedCornerShape(18.dp))
                                            IconButton(onClick={tap();platformTools= !platformTools}){AppGlyph(18,Paper,Modifier.size(25.dp))}
                                        }
                                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                            items(PlatformFilter.entries,key={it.name}){filter->FilterChip(selected=platformFilter==filter.ordinal,onClick={tap();platformFilter=filter.ordinal},label={UiText(filter.label,fontSize=11.sp)})}
                                        }
                                        PlatformSummary(selected.size,installed.size,{tap();platformFilter=1},{tap();platformFilter=2})
                                        AnimatedVisibility(platformTools) {
                                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                                                TextButton(onClick={tap();store.selected=selected+installed},enabled=installed.isNotEmpty()){UiText("Select installed",fontSize=12.sp)}
                                                TextButton(onClick={tap();store.selected=emptySet()},enabled=selected.isNotEmpty()){UiText("Clear selection",fontSize=12.sp)}
                                            }
                                        }
                                    }
                                }
                                val visible=filterPlatforms(platforms,platformQuery,PlatformFilter.entries.getOrElse(platformFilter){PlatformFilter.ALL},selected,installed)
                                val chosen=visible.filter{it.id in selected}
                                val available=visible.filter{it.id !in selected}
                                if(visible.isEmpty())item("empty-platforms"){Note("No matching platforms","Try a different search or switch to All.")}
                                if(chosen.isNotEmpty())item("selected-platforms") {
                                    PlatformGroup("Selected (${chosen.size})",chosen,selected,installed,todayData.reels,running,{statusLabel(store.status(it))},{platform,yes->tap();store.selected=if(yes)selected+platform.id else selected-platform.id},::openPlatform,{tap();store.selected=emptySet()},"Clear all")
                                }
                                if(available.isNotEmpty())item("available-platforms") {
                                    PlatformGroup("Available platforms",available,selected,installed,todayData.reels,running,{statusLabel(store.status(it))},{platform,yes->tap();store.selected=if(yes)selected+platform.id else selected-platform.id},::openPlatform,{tap();store.selected=selected+available.map{it.id}},"Select all")
                                }
                                }
                            }
                            2 -> {
                                item("heading") {Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                                    UiText("Activity overview",fontSize=27.sp,fontWeight=FontWeight.SemiBold,letterSpacing=(-.6).sp)
                                }}
                                if(activityHistory.isNotEmpty()) {
                                    item("trend") {UsageTrendChart(activityHistory,store.reducedMotion)}
                                    item("week") {UsageWeekChart(activityHistory,LocalDate.parse(activityDay)){day->tap();activityDay=day.toString()}}
                                }
                                val day=LocalDate.parse(activityDay)
                                val dayData=activityHistory.find{it.date==day} ?: todayData
                                item("day-summary") {ActivityPanel {
                                    UiText(day.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current))),fontWeight=FontWeight.SemiBold)
                                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                                        Column {UiText("${dayData.totalReels}",fontSize=30.sp,color=Seafoam,fontWeight=FontWeight.SemiBold);UiText("estimated reels",fontSize=11.sp,color=Muted)}
                                        Column(horizontalAlignment=Alignment.End) {UiText(durationLabel(dayData.totalMillis),fontSize=30.sp,color=Seafoam,fontWeight=FontWeight.SemiBold);UiText("tracked video-screen time",fontSize=11.sp,color=Muted)}
                                    }
                                }}
                                item("share-chart") {UsageDonut(dayData)}
                                

                            }
                            3 -> {
                                item("settings:${settingsSection ?: "index"}") {
                                    val section=SettingsSection.entries.find{it.name==settingsSection}
                                    if(section==null)SettingsIndex(onOpen={tap();settingsSection=it},onPlatforms={tap();settingsPlatforms=true;change(1)})
                                    else Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.TopCenter) {
                                        SettingsDetails(section,store,revision,enabled,speaker,this@MainActivity,
                                        onBack={tap();settingsSection=null},onConnect={change(AppDestination.Miso.ordinal)},
                                        onManage={tap();openMisoAccessibility()},onAppInfo={tap();openAppInfo()},
                                        onGoal={tap();editGoal=true},onErase={tap();erase=true},
                                        onNotifications={tap();DeviceSettings.open(this@MainActivity,Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,packageName))},tap=::tap,onEditAnswers={tap();editingAnswers=true},
                                        onLegalNavigation={legalNavigationScope.launch{activeScrollState.scrollToItem(0)}})
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                AnimatedVisibility(navigationVisible || tab==3 || (tab==1 && settingsPlatforms),enter=if(store.reducedMotion) EnterTransition.None else fadeIn(tween(160))+slideInVertically(tween(210)){it},exit=if(store.reducedMotion) ExitTransition.None else fadeOut(tween(100))+slideOutVertically(tween(180)){it}) {
                    ReferenceNavigation(tab,store.reducedMotion,::change)
                }
            }
            }
        }
        if(focusMode)FocusModeDialog(running,paused,!enabled || !store.consent,selected.size,total,store.goal,
            onDismiss={focusMode=false},onToggle={focusMode=false;toggleCompanion()},onBreak={tap();focusMode=false;quickBreak=true})
        if(quickBreak) BreakPicker(onDismiss={quickBreak=false}){minutes->tap();speaker.stop();store.pauseUntil=System.currentTimeMillis()+minutes*60_000L;quickBreak=false;if(android.os.Build.VERSION.SDK_INT>=33 && store.breakNotifications && !BreakReminders.allowed(this@MainActivity))notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)}
        if(editGoal) {
            if(tab==3)SettingsGoalDialog(store.baseGoal,onDismiss={editGoal=false},onSave={goal->tap();store.goal=goal;editGoal=false})
            else GoalEditor(store.baseGoal,onDismiss={editGoal=false},onSave={goal->tap();store.goal=goal;editGoal=false})
        }
        if(erase) SettingsTheme {AlertDialog(onDismissRequest={erase=false},title={UiText("Delete activity?")},text={UiText("This removes saved daily reel totals and tracked time from this phone. Your settings and platform choices stay.")},confirmButton={TextButton(onClick={tap();store.clearCounts();erase=false}){UiText("Delete activity")}},dismissButton={TextButton(onClick={tap();erase=false}){UiText("Keep it")}})}
    }
}

private fun statusLabel(status:String)=when(status) {
    "tracking"->"Player recognized · counting"
    "limit_reached"->"Daily limit reached · counting stopped"
    "limit_covered"->"Daily boundary covered · take a break"
    "pager_mode"->"Player recognized · compatible mode"
    "waiting_player"->"App connected · open short videos"
    "missing_labels"->"Labels hidden · enable compatible counting"
    "overlay_failed"->"Overlay unavailable · reconnect accessibility"
    "background"->"Last seen · app in background"
    else->"Not seen yet · open this app"
}
@Composable internal fun Mascot(store:TrackerStore,total:Int,modifier:Modifier,faded:Boolean=false,talking:Boolean=false) {
    val motionPaused=LocalMisoMotionPaused.current
    AndroidView(factory={CompanionView(it)},update={it.animated= !motionPaused && !store.reducedMotion && (talking || (store.running && store.pauseUntil<=System.currentTimeMillis()));it.talking=talking;it.expressive=store.expressions;it.progress=store.allowanceProgress();it.activity=when { store.pauseUntil>System.currentTimeMillis()->CompanionActivity.BREAK;store.running->CompanionActivity.ACTIVE;else->CompanionActivity.STANDBY };it.alpha=if(faded) store.opacity else 1f;it.contentDescription="Miso the bunny, ${CompanionMood.forActivity(store.allowanceProgress(),it.activity).title}"},modifier=modifier)
}
@Composable private fun Eyebrow(text:String) { UiText(text,color=Muted,fontSize=9.sp,letterSpacing=1.5.sp,fontWeight=FontWeight.SemiBold) }
@Composable private fun CardPanel(gradient:Boolean=false,content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).border(1.dp,AppStyle.Frame,RoundedCornerShape(25.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)
}
@Composable private fun Note(title:String,detail:String) { CardPanel { UiText(title,fontWeight=FontWeight.SemiBold,fontSize=15.sp,color=Seafoam);UiText(detail,fontSize=12.sp,lineHeight=20.sp,color=Muted) } }
@Composable internal fun ToggleCard(title:String,detail:String,checked:Boolean,onChange:(Boolean)->Unit) { SettingsBody { Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) { Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){UiText(title,fontWeight=FontWeight.SemiBold,fontSize=15.sp);UiText(detail,color=Muted,fontSize=12.sp,lineHeight=18.sp)};Switch(checked,onChange,colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Paper,uncheckedTrackColor=Color.Transparent,uncheckedBorderColor=AppStyle.Frame)) } } }
@Composable private fun HomeCompanionPanel(running:Boolean,paused:Boolean,canSpeak:Boolean,onHear:()->Unit,onToggle:()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(38.dp,Alignment.CenterHorizontally)) {
        ReferenceRoundAction(if(paused)"Resume" else if(running)"Pause" else "Start",if(running)22 else 14,AppStyle.Teal,onClick=onToggle)
        ReferenceRoundAction("Hear Miso",6,AppStyle.Blue,canSpeak,onHear)
    }
}
@Composable private fun CompanionStatus(running:Boolean,paused:Boolean) {
    Row(Modifier.padding(horizontal=4.dp,vertical=5.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        AppGlyph(if(paused)22 else if(running)20 else 21,AppStyle.Control,Modifier.size(16.dp))
        UiText(if(paused)"Break" else if(running)"Active" else "Standby",fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=Paper)
    }
}

@Composable private fun MisoWelcome(store:TrackerStore,talking:Boolean,onHear:()->Unit,onContinue:()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp)).border(1.dp,AppStyle.Frame,RoundedCornerShape(25.dp)).padding(18.dp).semantics { liveRegion=LiveRegionMode.Polite },verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            val motionPaused=LocalMisoMotionPaused.current
    AndroidView(factory={CompanionView(it)},update={it.animated= !motionPaused && !store.reducedMotion;it.talking=talking;it.expressive=store.expressions},modifier=Modifier.size(90.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                UiText("Hi, I'm Miso.",fontSize=22.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Control)
                UiText("I'll keep count while you scroll and remind you when it's time for a breather. You choose the apps. I'll tag along.",fontSize=12.sp,lineHeight=19.sp,color=Paper)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        TextButton(onClick=onHear){UiText("Hear Miso")}
        TextButton(onClick=onContinue,modifier=Modifier) {UiText("Let's begin  →",color=AppStyle.Control,fontWeight=FontWeight.SemiBold)}
        }
    }
}

@Composable internal fun AccessibilityControls(connected:Boolean,onConnect:()->Unit,onManage:()->Unit) {
    var expanded by rememberSaveable {mutableStateOf(false)}
    SettingsBody {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {
            AppGlyph(3,Seafoam,Modifier.size(29.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                UiText("Accessibility access",fontSize=18.sp,fontWeight=FontWeight.SemiBold)
                UiText(when {
                    connected && MisoServiceConnection.connected->"Connected to Miso"
                    connected->"Waiting for Android to connect Miso"
                    else->"Access is off or restricted by Android"
                },fontSize=12.sp,color=Muted)
            }
        }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick=if(connected)onManage else onConnect,modifier=Modifier.weight(1f).heightIn(min=46.dp),shape=RoundedCornerShape(14.dp)){UiText(if(connected) "Manage access" else "Connect accessibility",fontSize=14.sp,fontWeight=FontWeight.SemiBold)}
            TextButton(onClick={expanded= !expanded}){UiText(if(expanded)"Less" else "Details",fontSize=13.sp)}
        }
        AnimatedVisibility(expanded) {
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                UiText("Miso uses visible player labels and page changes only in your selected apps. Screen text is never saved. App updates can affect recognition. You can revoke access at any time.",fontSize=14.sp,lineHeight=22.sp,color=Muted)
                TextButton(onClick=if(connected)onConnect else onManage){UiText(if(connected) "Review how access works" else "Manage Android access")}
            }
        }
    }
}

