package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.File
import android.graphics.Bitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/** Real controls with isolated preference files; no permissions, bonus allowance or user data are changed. */
class SettingsRedesignTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private fun isolated(action:(TrackerStore,MisoSpeaker)->Unit) {
        val base=InstrumentationRegistry.getInstrumentation().targetContext
        val file="settings-redesign-${System.nanoTime()}"
        val wrapped=object:ContextWrapper(base){override fun getSharedPreferences(name:String,mode:Int):SharedPreferences=base.getSharedPreferences(file,mode)}
        val store=TrackerStore(wrapped)
        store.prefs.edit().putInt("goal",20).putInt("dailyMinutes",15).putString("userName","Rafi").putBoolean("onboardingMuted",true).putBoolean("reducedMotion",true).putInt("count:${java.time.LocalDate.now()}:instagram",7).commit()
        lateinit var speaker:MisoSpeaker
        val oldDark=AppStyle.dark
        ui.runOnUiThread {speaker=MisoSpeaker(base);ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
        try{action(store,speaker)}finally{
            ui.runOnUiThread {speaker.close();AppStyle.dark=oldDark;ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
            base.deleteSharedPreferences(file)
        }
    }
    @Test fun everySubpageHasWorkingBackControlInBothThemesAtLargeFontScale()=isolated {store,speaker->
        var section by mutableStateOf(SettingsSection.LANGUAGE)
        var dark by mutableStateOf(false)
        var language by mutableStateOf("en")
        var backs=0
        ui.setContent {
            AppStyle.dark=dark
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.5f),LocalUiLanguage provides language) {
                MaterialTheme {SettingsTheme {
                    Column(Modifier.width(320.dp).fillMaxHeight().background(AppStyle.Background).testTag("settings-fixture").verticalScroll(rememberScrollState())) {
                        SettingsDetails(section,store,0,false,speaker,store.contextForSpeech,{backs++},{},{},{},{},{},{},{})
                    }
                }}
            }
        }
        for(lang in listOf("en","bn"))for(theme in listOf(false,true))for(page in SettingsSection.entries) {
            ui.runOnIdle {dark=theme;section=page;language=lang}
            ui.onNodeWithContentDescription("Back to Settings").performScrollTo().assertIsDisplayed()
            val context=InstrumentationRegistry.getInstrumentation().targetContext
            val directory=File(context.getExternalFilesDir(null),"settings-redesign-previews").apply{mkdirs()}
            File(directory,"${lang}-${if(theme)"dark" else "light"}-${page.name}.png").outputStream().use{ui.onNodeWithTag("settings-fixture").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG,100,it)}
            ui.onNodeWithContentDescription("Back to Settings").performClick()
        }
        assertEquals(SettingsSection.entries.size*4,backs)
        assertEquals(20,store.baseGoal);assertEquals(7,store.total());assertEquals(15,store.dailyMinutes)
    }
    @Test fun goalInputsValidateAndPersistWithoutResettingHistory()=isolated {store,_->
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){SettingsGoalPage(store,{},{})}}}}
        ui.onNodeWithTag("settings-minutes").performScrollTo().performTextReplacement("1441")
        ui.onNodeWithText("Save daily goal").performScrollTo().assertIsNotEnabled()
        ui.onNodeWithTag("settings-minutes").performScrollTo().performTextReplacement("0")
        ui.onNodeWithTag("settings-reels").performScrollTo().performTextReplacement("37")
        ui.onNodeWithText("Save daily goal").performScrollTo().performClick()
        assertEquals(0,store.dailyMinutes);assertEquals(37,store.baseGoal);assertEquals(7,store.total())
        ui.onNodeWithTag("settings-reels").performScrollTo().performTextReplacement("0")
        ui.onNodeWithText("Save daily goal").performScrollTo().assertIsNotEnabled()
        assertEquals(37,store.baseGoal)
    }
    @Test fun accessibilityDetailsAndFaqsExpandWithoutOpeningAndroid()=isolated {store,_->
        var opens=0
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){SettingsAccessPage(store,false,{opens++},{opens++},{})}}}}
        ui.onNodeWithText("Connect accessibility").assertIsDisplayed()
        ui.onNodeWithText("Details").performClick()
        ui.onNodeWithText("Manage Android access").performScrollTo().assertIsDisplayed()
        ui.onNodeWithText("Accessibility help").performScrollTo().performClick()
        ui.onNodeWithText("If Android denies access",substring=true).performScrollTo().assertIsDisplayed()
        assertEquals(0,opens);assertEquals(7,store.total())
    }
    @Test fun helpActionsAndExpandableContentRemainReachable()=isolated {_,_->
        var notifications=0;var access=0
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){SettingsHelpPage({access++},{notifications++})}}}}
        ui.onNodeWithText("Counts look low?",substring=true).assertDoesNotExist()
        ui.onNodeWithText("Counting issues").performClick()
        ui.onNodeWithText("Counts look low?",substring=true).assertIsDisplayed()
        ui.onNodeWithText("Notification settings").performScrollTo().performClick()
        ui.onNodeWithText("Review accessibility").performScrollTo().performClick()
        assertEquals(1,notifications);assertEquals(1,access)
    }
    @Test fun settingsPlatformSelectionUsesRealCallbacksAndReturns()=isolated {store,_->
        var selected by mutableStateOf(emptySet<String>())
        var backs=0
        ui.setContent {MaterialTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SettingsPlatforms("",{},PlatformFilter.ALL,{},selected,setOf("youtube"),List(platforms.size){0},false,{""},{p,on->selected=if(on)selected+p.id else selected-p.id},{},{backs++},{},{},{})
        }}}
        ui.onNodeWithContentDescription("Track YouTube").performScrollTo().performClick()
        assertEquals(setOf("youtube"),selected)
        ui.onNodeWithContentDescription("Back to Settings").performScrollTo().performClick()
        assertEquals(1,backs);assertEquals(7,store.total())
    }
    @Test fun reminderSwitchesUpdateOnFirstTapRapidTapsAndExternalWrites()=isolated {store,speaker->
        var mounted by mutableStateOf(true)
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){if(mounted)SettingsAlertsPage(store,speaker,store.contextForSpeech,{}, {})}}}}
        val options=listOf("Welcome greeting" to "voiceEnabled","Spoken limit warnings" to "goalReminders","Near-limit red warning" to "limitTint","Goal notifications" to "limitNotifications","Break notifications" to "breakNotifications","Touch feedback" to "haptics")
        options.forEach{(title,key)->
            val row=ui.onNode(hasText(title) and hasClickAction())
            row.performScrollTo().assertIsOn().performClick().assertIsOff()
            assertFalse(store.prefs.getBoolean(key,true))
            val click=row.fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
            ui.runOnIdle{repeat(5){click()}}
            row.assertIsOn();assertTrue(store.prefs.getBoolean(key,false))
        }
        ui.runOnIdle{store.limitTint=false}
        ui.onNode(hasText("Near-limit red warning") and hasClickAction()).performScrollTo().assertIsOff()
        ui.runOnIdle{mounted=false};ui.waitForIdle();ui.runOnIdle{mounted=true}
        ui.onNode(hasText("Near-limit red warning") and hasClickAction()).performScrollTo().assertIsOff()
        assertEquals(7,store.total());assertEquals(20,store.baseGoal)
    }
    @Test fun fourMisoCategoriesAreVisibleAndPreviewTracksAnUncommittedDrag()=isolated {store,_->
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.width(320.dp).fillMaxHeight().verticalScroll(rememberScrollState())){MisoDisplaySettings(store,false,{})}}}}
        val labels=listOf("Size","Position","Miso visibility","Number visibility")
        labels.forEach{ui.onAllNodesWithText(it).assertCountEquals(1);ui.onNodeWithText(it).assertIsDisplayed()}
        val slider=ui.onNodeWithTag("miso-display-slider")
        slider.performTouchInput{down(Offset(width*.35f,centerY));moveTo(Offset(width*.65f,centerY));advanceEventTime(32)}
        val current=slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current.toInt()
        assertTrue(current>96);assertEquals(96,store.overlaySize)
        ui.onNodeWithContentDescription("Miso preview:",substring=true).assert(hasContentDescription("Miso preview: $current dp",substring=true))
        slider.performTouchInput{up()}
        assertEquals(current,store.overlaySize)
        labels.drop(1).forEach{ui.onNodeWithText(it).performClick();ui.onAllNodesWithText(it).assertCountEquals(1)}
    }
    @Test fun appearanceSelectorExistsOnlyInAppearanceAndPreservesLanguage()=isolated {store,speaker->
        var page by mutableStateOf(SettingsSection.LANGUAGE)
        var revision by mutableIntStateOf(0)
        ui.setContent {MaterialTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SettingsDetails(page,store,revision,false,speaker,store.contextForSpeech,{},{},{},{},{},{},{},{})
        }}}
        ui.onNodeWithTag("theme-dark").assertDoesNotExist()
        ui.runOnIdle{page=SettingsSection.APPEARANCE}
        ui.onNodeWithTag("theme-dark").performClick();assertEquals("dark",store.themeMode)
        ui.runOnIdle{revision++}
        ui.onNodeWithTag("theme-dark").assertIsSelected()
        ui.runOnIdle{page=SettingsSection.LANGUAGE}
        ui.onNodeWithTag("theme-dark").assertDoesNotExist();assertEquals("dark",store.themeMode)
        assertEquals(7,store.total())
    }
    @Test fun externalGoalEditsRefreshWithoutOverwritingTheNextInput()=isolated {store,_->
        var revision by mutableIntStateOf(0)
        ui.setContent {MaterialTheme {SettingsTheme {Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){SettingsGoalPage(store,{},{},revision)}}}}
        ui.runOnIdle{store.goal=42;revision++}
        ui.onNodeWithTag("settings-reels").performScrollTo().assertTextContains("42")
        ui.onNodeWithTag("settings-reels").performTextReplacement("38")
        ui.onNodeWithText("Save daily goal").performScrollTo().performClick()
        assertEquals(38,store.baseGoal)
        ui.onNodeWithTag("settings-reels").performScrollTo().performTextReplacement("39");ui.onNodeWithTag("settings-reels").assertTextContains("39")
        assertEquals(7,store.total())
    }

}
