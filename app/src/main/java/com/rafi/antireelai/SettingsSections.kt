package com.rafi.antireelai

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color

internal enum class SettingsSection(val title:String,val description:String,val glyph:Int,val group:String) {
    LANGUAGE("Language & personal preferences","Onboarding profile and language",11,"Your app"),
    APPEARANCE("Appearance","Light, dark & device theme",7,"Make it yours"),
    MISO("Miso & display","Character, counter & motion",8,"Make it yours"),
    GOAL("Daily goal","A custom boundary for your day",5,"Your pace"),
    RESTART("Restart allowance","One extra allowance of 50 reels per day",24,"Your pace"),
    ALERTS("Breaks & reminders","Break timer, voice & notifications",13,"Your pace"),
    ACCESS("Accessibility","Connection & platform recognition",9,"Your app"),
    ACTIVITY("Activity & storage","Local history & data controls",10,"Your app"),
    GUIDE("Understanding tracking","What your recorded time means",11,"Your app"),
    PRIVACY("Privacy & security","Data usage & protection",12,"Your app"),
    HELP("Help & guidance","Answers and useful controls",11,"Your app"),
    APP_INFO("App info","Version & Android app settings",11,"Your app")
}
internal data class SettingsCategory(val title:String,val sections:List<SettingsSection>,val includesPlatforms:Boolean=false)
internal val settingsCategories=listOf(
    SettingsCategory("Personalization",listOf(SettingsSection.LANGUAGE,SettingsSection.APPEARANCE,SettingsSection.MISO)),
    SettingsCategory("Goals & wellbeing",listOf(SettingsSection.GOAL,SettingsSection.RESTART,SettingsSection.ALERTS)),
    SettingsCategory("Platforms & tracking",listOf(SettingsSection.ACCESS,SettingsSection.ACTIVITY,SettingsSection.GUIDE),includesPlatforms=true),
    SettingsCategory("Privacy & security",listOf(SettingsSection.PRIVACY)),
    SettingsCategory("Support & app",listOf(SettingsSection.HELP,SettingsSection.APP_INFO))
)
@Composable internal fun SettingsIndex(onOpen:(String)->Unit,onPlatforms:()->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
        UiText("Settings",fontSize=29.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,modifier=Modifier.padding(bottom=2.dp))
        settingsCategories.forEach {category->
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                UiText(category.title,fontSize=12.sp,fontWeight=FontWeight.Medium,color=AppStyle.Muted,modifier=Modifier.padding(horizontal=6.dp))
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AppStyle.Surface)) {
                    if(category.includesPlatforms) {
                        SettingsMenuRow(1,"Manage platforms",onPlatforms)
                        SettingsDivider()
                    }
                    category.sections.forEachIndexed {index,section->
                        SettingsMenuRow(section.glyph,section.title){onOpen(section.name)}
                        if(index<category.sections.lastIndex)SettingsDivider()
                    }
                }
            }
        }
    }
}
@Composable private fun SettingsDivider() {
    HorizontalDivider(Modifier.padding(start=56.dp,end=16.dp),thickness=1.dp,color=settingsDividerColor(AppStyle.dark))
}
@Composable private fun SettingsMenuRow(glyph:Int,title:String,onClick:()->Unit) {
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    Row(Modifier.fillMaxWidth().background(if(pressed)AppStyle.Accent.copy(alpha=.08f) else Color.Transparent)
        .clickable(interactionSource=interaction,indication=null,role=Role.Button,onClick=onClick)
        .heightIn(min=60.dp).padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
        AppGlyph(glyph,AppStyle.Accent,Modifier.size(23.dp))
        UiText(title,modifier=Modifier.weight(1f),fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text)
        UiText("›",fontSize=22.sp,color=AppStyle.Muted)
    }
}
@Composable internal fun SettingsDetails(section:SettingsSection,store:TrackerStore,revision:Int,connected:Boolean,speaker:MisoSpeaker,context:Context,onBack:()->Unit,onConnect:()->Unit,onManage:()->Unit,onAppInfo:()->Unit,onGoal:()->Unit,onErase:()->Unit,onNotifications:()->Unit,tap:()->Unit,onEditAnswers:()->Unit={},onLegalNavigation:()->Unit={}) {
    var legal by rememberSaveable(section.name){mutableStateOf<String?>(null)}
    fun closeLegal(){legal=null;onLegalNavigation()}
    fun openLegal(document:LegalDocument){legal=document.slug;onLegalNavigation()}
    BackHandler(enabled=legal!=null){closeLegal()}
    legal?.let {slug->
        LegalDocument.entries.firstOrNull{it.slug==slug}?.let{document->
            SettingsTheme {LegalReader(document,::closeLegal)}
            return
        }
    }
    CompositionLocalProvider(LocalMotionReduced provides motionReduced(store.reducedMotion)) {
    SettingsTheme {
        Column(Modifier.widthIn(max=640.dp).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            SettingsPageHeader(section.title,onBack)
            when(section) {
                SettingsSection.LANGUAGE->PersonalPreferencesSettings(store,revision,tap,onEditAnswers)
                SettingsSection.APPEARANCE->{
                    SettingsGroup("Theme") {SettingsInset {SettingsThemeSelector(store.themeMode){tap();store.themeMode=it};SettingsText("Device follows your phone’s appearance. Your choice applies throughout AntiReel.")}}
                    SettingsGroup("Display") {SettingsRow("Light & dark",7,"Text, controls and Miso adapt to your selected theme.")}
                }
                SettingsSection.MISO->MisoDisplaySettings(store,speaker.speaking,tap,revision)
                SettingsSection.GOAL->SettingsGoalPage(store,tap,onGoal,revision)
                SettingsSection.RESTART->{
                    SettingsStats(Triple("${store.total()}","Reels today",23),Triple("${store.goal}","Allowed today",5))
                    SettingsGroup("Restart allowance") {SettingsInset {
                        UiText(if(store.restartUsed)"Extra allowance used today" else "50 more reels, once today",fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
                        SettingsText("Available after reaching your daily limit. Your history stays intact. Once these 50 reels are used, the boundary stays in place until tomorrow.")
                        SettingsAction(if(store.restartUsed)"Available again tomorrow" else "Add 50 reels & restart",{tap();store.restartForToday()},enabled=!store.restartUsed && store.total()>=store.goal,primary=true)
                    }}
                    SettingsGroup {SettingsFaq("How the extra allowance works",24){SettingsText("Changes to your regular goal apply tomorrow while this allowance is active. Your time boundary still applies.")}}
                }
                SettingsSection.ALERTS->SettingsAlertsPage(store,speaker,context,onNotifications,tap,revision)
                SettingsSection.ACCESS->SettingsAccessPage(store,connected,onConnect,onManage,tap,revision)
                SettingsSection.ACTIVITY->SettingsStoragePage(store,revision,context,onErase)
                SettingsSection.GUIDE->{
                    SettingsGroup("Recorded time") {SettingsInset {SettingsText("Time is recorded while a selected short-video screen is recognized in the foreground with Miso on. Breaks, locked screens and unrecognized screens are excluded.")}}
                    SettingsGroup("How tracking works") {
                        SettingsFaq("Time & attention",24){SettingsText("It estimates screen visibility, not attention or playback. Historic reel counts are never converted into guessed time. Live observations are saved in short batches, approximately every three seconds.")}
                        SettingsRule()
                        SettingsFaq("Reels & repeat detection",23){SettingsText("Forward page evidence helps reject backtracking. Hidden ads and missing labels can affect accuracy. Repeat detection is temporary; identical captions can also hide distinct videos.")}
                    }
                }
                SettingsSection.HELP->{
                    SettingsHelpPage(onConnect,onNotifications)
                    SettingsGroup("Support") {
                        SettingsRow("Help & Support",11,"Full guidance and public support page",onClick={openLegal(LegalDocument.SUPPORT)})
                        SettingsRule()
                        SettingsRow("Email support",11,SUPPORT_EMAIL,onClick={ExternalLinks.support(context)})
                    }
                }
                SettingsSection.PRIVACY->{
                    SettingsGroup("Legal & support") {
                        LegalDocument.entries.forEachIndexed{index,document->
                            SettingsRow(document.title,document.glyph,onClick={openLegal(document)})
                            if(index<LegalDocument.entries.lastIndex)SettingsRule()
                        }
                    }
                    SettingsGroup("Private by design") {
                        SettingsRow("On-device storage",10,"No account, advertising or internet permission.")
                        SettingsRule()
                        SettingsRow("Selected apps only",12,"Accessibility access can be revoked at any time.")
                    }
                    SettingsGroup("Your data") {
                        SettingsFaq("What AntiReel saves",10){SettingsText("AntiReel saves your local profile and onboarding choices, settings, daily reel counts, recorded video-screen duration and connection status.")}
                        SettingsRule()
                        SettingsFaq("What stays private",12){SettingsText("It never saves captions, messages, screenshots or video. Only your selected platforms are inspected.")}
                        SettingsRule()
                        SettingsFaq("Android permissions",9){SettingsText("Accessibility requires your approval in Android Settings. The break timer uses Android alarms; system battery restrictions can delay its notification.")}
                    }
                    SettingsGroup("Your controls") {
                        SettingsRow("Manage accessibility",9,onClick=onManage)
                        SettingsRule()
                        SettingsRow("Delete activity history",10,onClick=onErase)
                        SettingsRule()
                        SettingsRow("Android app settings",17,"Clear all storage or manage permissions in Android",onClick=onAppInfo)
                    }
                }
                SettingsSection.APP_INFO->{
                    val version=remember{runCatching{context.packageManager.getPackageInfo(context.packageName,0).versionName}.getOrNull() ?: ""}
                    SettingsGroup {SettingsInset {UiText("AntiReels AI",fontSize=22.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text);SettingsText("Version $version")}}
                    SettingsGroup("Android") {SettingsRow("Open AntiReel App info",17,"Permissions, notifications and system settings",onClick=onAppInfo)}
                }
            }
        }
    }
    }
}

/** Retained for non-Settings callers; scoped redesign does not alter their layout. */
@Composable internal fun SettingsBody(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalArrangement=Arrangement.spacedBy(16.dp),content=content)
}
