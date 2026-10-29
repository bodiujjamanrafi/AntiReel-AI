package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class PreferenceUpdateTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun isolated(action:(TrackerStore)->Unit){
  val base=InstrumentationRegistry.getInstrumentation().targetContext;val file="preferences-test-${System.nanoTime()}"
  val wrapped=object:ContextWrapper(base){override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences(file,mode)}
  val store=TrackerStore(wrapped);store.prefs.edit().putBoolean("onboardingMuted",true).putString("userName","Rafi").putInt("dailyMinutes",15).putInt("goal",20).commit()
  InstrumentationRegistry.getInstrumentation().runOnMainSync{ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
  try{action(store)}finally{InstrumentationRegistry.getInstrumentation().runOnMainSync{ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};base.deleteSharedPreferences(file)}
 }
 @Test fun personalPreferencesUpdateImmediatelyWithoutResettingHistory()=isolated{store->
  var edits=0
  ui.setContent{MaterialTheme{var revision by remember{mutableIntStateOf(0)};DisposableEffect(store){val listener=SharedPreferences.OnSharedPreferenceChangeListener{_,_->revision++};store.prefs.registerOnSharedPreferenceChangeListener(listener);onDispose{store.prefs.unregisterOnSharedPreferenceChangeListener(listener)}}
   Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){PersonalPreferencesSettings(store,revision,{}){edits++}}
  }}
  ui.onNodeWithText("Rafi").assertIsDisplayed()
  ui.onNodeWithTag("language-bn").performClick();assertEquals("bn",store.prefs.getString("language",null))
  ui.onNodeWithTag("theme-dark").assertDoesNotExist();assertEquals("system",store.themeMode)
  ui.onNodeWithTag("preference-voice").performScrollTo().assertIsOff().performClick().assertIsOn()
  assertFalse(store.prefs.getBoolean("onboardingMuted",true));assertTrue(store.voiceEnabled);assertTrue(store.goalReminders)
  ui.onNodeWithText("Edit my answers").performScrollTo().performClick();assertEquals(1,edits)
  assertEquals(15,store.dailyMinutes);assertEquals(20,store.baseGoal)
 }
 @Test fun defaultAgreementStillRequiresExplicitSettingsAction()=isolated{store->
  var opened=0;lateinit var speaker:MisoSpeaker
  InstrumentationRegistry.getInstrumentation().runOnMainSync{speaker=MisoSpeaker(InstrumentationRegistry.getInstrumentation().targetContext)}
  try{
   ui.setContent{MaterialTheme{MisoPage(store,speaker,false,false,false,Modifier.fillMaxSize(),onAgree={opened++},onManage={},onToggle={},onPlatforms={})}}
   val agreement=ui.onNodeWithContentDescription("Agree to terms and privacy")
   val settings=ui.onNodeWithText("Agree & open Settings")
   agreement.assertIsOn();settings.assertIsEnabled();assertEquals(0,opened);assertEquals(0,store.prefs.getInt("setupTermsVersion",0));assertFalse(store.consent)
   agreement.performClick().assertIsOff();settings.assertIsNotEnabled()
   agreement.performClick();settings.performClick();assertEquals(1,opened);assertEquals(1,store.prefs.getInt("setupTermsVersion",0));assertFalse(store.consent)
  }finally{InstrumentationRegistry.getInstrumentation().runOnMainSync{speaker.close()}}
 }
}
