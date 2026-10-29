package com.rafi.antireelai

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun SettingsGoalPage(store:TrackerStore,tap:()->Unit,onGoal:()->Unit,revision:Int=0) {
    @Suppress("UNUSED_VARIABLE") val refresh=revision
    var minutes by remember{mutableStateOf(store.dailyMinutes.toString())}
    var observedMinutes by remember{mutableIntStateOf(store.dailyMinutes)}
    var reels by remember{mutableStateOf(store.baseGoal.toString())}
    var observedGoal by remember{mutableIntStateOf(store.baseGoal)}
    LaunchedEffect(store.dailyMinutes,store.baseGoal){
        if(store.dailyMinutes!=observedMinutes){observedMinutes=store.dailyMinutes;minutes=observedMinutes.toString()}
        if(store.baseGoal!=observedGoal){observedGoal=store.baseGoal;reels=observedGoal.toString()}
    }
    var saved by remember{mutableStateOf(false)}
    val parsedMinutes=minutes.toIntOrNull()?.takeIf{it in 0..1440}
    val parsedReels=parseDailyGoal(reels)
    SettingsStats(Triple(if(store.dailyMinutes>0)"${store.dailyMinutes} min" else "Not set","Daily time",24),Triple("${store.goal}","Allowed today",23))
    SettingsGroup("Daily boundaries") {SettingsInset {
        OutlinedTextField(minutes,{minutes=it.take(4);saved=false},label={UiText("Daily time")},suffix={UiText("min")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=parsedMinutes==null,supportingText={SettingsText("0–1440 minutes. Set 0 for no time boundary.")},modifier=Modifier.fillMaxWidth().testTag("settings-minutes"))
        OutlinedTextField(reels,{reels=it.take(10);saved=false},label={UiText("Reels per day")},suffix={UiText("reels")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=parsedReels==null,supportingText={if(parsedReels==null)SettingsText("Enter a positive whole number.")},modifier=Modifier.fillMaxWidth().testTag("settings-reels"))
        SettingsAction("Save daily goal",{if(parsedMinutes!=null && parsedReels!=null){tap();store.prefs.edit().putInt("dailyMinutes",parsedMinutes).apply();store.goal=parsedReels;observedMinutes=parsedMinutes;observedGoal=parsedReels;saved=true}},enabled=parsedMinutes!=null && parsedReels!=null,primary=true)
        if(saved)UiText("Saved",fontSize=14.sp,color=AppStyle.Accent)
        SettingsAction("Edit custom goal",onGoal)
    }}
    SettingsGroup("At your boundary") {SettingsInset {
        SettingsText("Miso warns you at 80% and stops at whichever daily boundary is reached first: time or reels.")
        if(store.restartUsed)SettingsText("Changes to your regular goal apply tomorrow while this allowance is active.")
    }}
}
@Composable internal fun SettingsAlertsPage(store:TrackerStore,speaker:MisoSpeaker,context:Context,onNotifications:()->Unit,tap:()->Unit,revision:Int=0) {
    @Suppress("UNUSED_VARIABLE") val refresh=revision
    val notificationsAllowed=BreakReminders.allowed(context)
    if(store.pauseUntil>System.currentTimeMillis())BreakTimer(store.pauseUntil){store.pauseUntil=0}
    SettingsGroup("Voice & greetings") {
        SettingsPreferenceSwitch(store,"voiceEnabled","Welcome greeting","Hear Miso automatically on first launch.",6){tap();store.voiceEnabled=it;if(!it)speaker.stop()}
        SettingsRule()
        SettingsInset {SettingsAction("Hear Miso",{tap();speaker.greet()},enabled=speaker.ready)}
        if(speaker.unavailable)SettingsInset {SettingsText("Install an offline voice for your selected language in Android text-to-speech settings. Keep speech/media volume audible to hear Miso.")}
    }
    SettingsGroup("Limit warnings") {
        SettingsPreferenceSwitch(store,"goalReminders","Spoken limit warnings","Miso speaks near, at and beyond your goal.",6){tap();store.goalReminders=it}
        SettingsRule()
        SettingsPreferenceSwitch(store,"limitTint","Near-limit red warning","A red warning from 80%. At your limit, counting stops.",12){tap();store.limitTint=it}
        SettingsRule()
        SettingsPreferenceSwitch(store,"limitNotifications","Goal notifications",if(notificationsAllowed)"Distinct sounds near and at your boundary." else "Saved preference. Allow Android notifications for delivery.",13){tap();store.limitNotifications=it}
    }
    SettingsGroup("Break notifications") {
        SettingsPreferenceSwitch(store,"breakNotifications","Break notifications",if(notificationsAllowed)"A countdown and chime when your break ends." else "Android permission is needed for break notifications.",4){tap();store.breakNotifications=it}
        SettingsRule()
        SettingsRow("Notification permission & sound",13,onClick=onNotifications)
        SettingsInset {SettingsText(if(BreakReminders.allowed(context))"Android notifications are enabled. Sound follows channel settings and Do Not Disturb." else "These switches save your preferences. Android notification permission is required for delivery.")}
    }
    ReminderSounds()
    SettingsGroup("Touch feedback") {SettingsPreferenceSwitch(store,"haptics","Touch feedback","Light vibration for controls and reminders.",18){store.haptics=it;tap()}}
}
@Composable internal fun SettingsAccessPage(store:TrackerStore,connected:Boolean,onConnect:()->Unit,onManage:()->Unit,tap:()->Unit,revision:Int=0) {
    @Suppress("UNUSED_VARIABLE") val refresh=revision
    val service=MisoServiceConnection.connected
    SettingsGroup {
        SettingsRow(when{connected && service->"Connected to Miso";connected->"Waiting for Android to connect Miso";else->"Access is off or restricted by Android"},9,trailing={AppGlyph(if(connected && service)20 else 11,if(connected && service)AppStyle.Teal else AppStyle.Accent,Modifier.size(24.dp))})
        SettingsInset {SettingsAction(if(connected)"Manage access" else "Connect accessibility",if(connected)onManage else onConnect,primary=true)}
        SettingsRule()
        SettingsFaq("Details",11){
            SettingsText("Miso uses visible player labels and page changes only in your selected apps. Screen text is never saved. App updates can affect recognition. You can revoke access at any time.")
            SettingsAction(if(connected)"Review how access works" else "Manage Android access",if(connected)onConnect else onManage)
        }
    }
    SettingsGroup("Connection check") {
        SettingsRow("Android access",9,trailing={UiText(if(connected)"Enabled" else "Off",fontSize=14.sp,color=AppStyle.Text)})
        SettingsRule()
        SettingsRow("Service",8,trailing={UiText(if(service)"Connected" else "Not connected",fontSize=14.sp,color=AppStyle.Text,modifier=Modifier.widthIn(max=110.dp))})
        SettingsRule()
        SettingsRow("Selected apps",1,trailing={UiText("${store.selected.size}",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Accent)})
    }
    SettingsGroup {
        SettingsFaq("Accessibility help",9){
            SettingsText("If Android denies access, open Settings → App info in AntiReel, then open the device’s App info menu. Choose Allow restricted settings if offered, confirm yourself, and return to Accessibility to enable AntiReel. Keep Play Protect enabled. Work, school and device security policies can prevent access. The optional shortcut alone does not start tracking.")
            if(!connected)SettingsText("An app update or reinstall can require setup again. Android approval must be completed in Settings. AntiReel cannot grant its own accessibility permission.")
            if(connected && !service)SettingsText("Access is enabled but the service has not connected. Reopen Android Accessibility and check AntiReel’s service switch. The app stays on standby until Android connects it.")
        }
    }
    SettingsGroup("Recognition") {SettingsPreferenceSwitch(store,"compatibility","Compatible counting","Use recognized forward video-page transitions when labels are hidden.",23){tap();store.compatibility=it}}
}
@Composable internal fun SettingsStoragePage(store:TrackerStore,revision:Int,context:Context,onErase:()->Unit) {
    val values=remember(revision){store.prefs.all}
    val bytes by produceState<Long?>(null,revision){value=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){java.io.File(context.applicationInfo.dataDir,"shared_prefs/antireel.xml").length()}}
    val recorded=remember(values){values.keys.filter{it.startsWith("count:") || it.startsWith("usage:")}.map{it.split(":")[1]}.distinct().size}
    SettingsStats(Triple("$recorded","Days with records",10),Triple(bytes?.let{android.text.format.Formatter.formatShortFileSize(context,it)} ?: "—","Local storage",10))
    SettingsGroup("On this phone") {
        SettingsRow("History retention",24,"Up to 30 days of local activity.")
        SettingsRule()
        SettingsRow("Local settings & activity",10,"Saved on this phone. No cloud account.")
    }
    SettingsGroup("Danger zone") {SettingsInset {
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            AppGlyph(12,MaterialTheme.colorScheme.error,Modifier.size(22.dp))
            Column(Modifier.weight(1f)){SettingsText("Delete recorded activity. Your settings and daily goal stay in place.")}
        }
        SettingsAction("Delete activity history",onErase,destructive=true)
    }}
}
@Composable internal fun SettingsHelpPage(onConnect:()->Unit,onNotifications:()->Unit) {
    SettingsGroup("Troubleshooting") {
        SettingsFaq("Counting issues",23){SettingsText("Counts look low? Connect accessibility, select an installed platform and start Miso before opening its short-video player. Confirmed forward page changes are needed; normal feeds and backtracking are excluded.")}
        SettingsRule()
        SettingsFaq("Accessibility",9){SettingsText("Review Android’s Accessibility service switch for AntiReel. The optional shortcut alone does not start tracking. If access is restricted, review Accessibility help before changing device settings.")}
        SettingsRule()
        SettingsFaq("Voice problems",6){SettingsText("No speech? Install an offline voice for your selected language in Android's text-to-speech settings and check speech/media volume. Welcome speech and spoken limit warnings have separate controls.")}
        SettingsRule()
        SettingsFaq("Notification problems",13){SettingsText("No break sound? Allow notifications, enable the Miso break-chime channel and check Do Not Disturb. Battery restrictions can delay alarms.")}
    }
    SettingsGroup("Quick actions") {
        SettingsRow("Notification settings",13,onClick=onNotifications)
        SettingsRule()
        SettingsRow("Review accessibility",9,onClick=onConnect)
    }
}
