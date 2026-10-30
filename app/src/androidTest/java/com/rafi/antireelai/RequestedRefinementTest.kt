package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.VideoView
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
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** Isolated preferences and captured external intents; no permission grants or fake usage. */
class RequestedRefinementTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 private fun isolated(action:(TrackerStore,Context,MutableList<Intent>)->Unit) {
  val base=InstrumentationRegistry.getInstrumentation().targetContext
  val file="requested-refinement-${System.nanoTime()}"
  val opened=mutableListOf<Intent>()
  val context=object:ContextWrapper(base) {
   override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences(file,mode)
   override fun startActivity(intent:Intent){opened.add(intent)}
  }
  val store=TrackerStore(context);store.prefs.edit().putBoolean("onboardingMuted",true).commit()
  ui.runOnUiThread{ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
  try{action(store,context,opened)}finally{ui.runOnUiThread{ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)};base.deleteSharedPreferences(file)}
 }
 @Test fun privacyOpensPublicBrowserDirectly()=isolated{store,context,opened->
  var back=0
  val speaker=MisoSpeaker(context)
  try {
   ui.setContent{MaterialTheme{SettingsDetails(SettingsSection.PRIVACY,store,0,false,speaker,context,{back++},{},{},{},{},{},{},{})}}
   ui.waitUntil(3000){opened.isNotEmpty()}
   assertEquals(LegalDocument.PRIVACY.url,opened.single().dataString)
   assertEquals(Intent.ACTION_VIEW,opened.single().action);assertEquals(1,back)
   ui.onNodeWithText("Legal & support").assertDoesNotExist()
  }finally{speaker.close()}
 }
 @Test fun helpRetainsLocalControlsAndSupportOpensWebWithoutSubtitle()=isolated{store,context,opened->
  var erased=0;var managed=0
  val speaker=MisoSpeaker(context)
  try {
   ui.setContent{MaterialTheme{Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){SettingsDetails(SettingsSection.HELP,store,0,false,speaker,context,{},{},{managed++},{},{},{erased++},{},{})}}}
   for(title in listOf("Private by design","Your data","Your controls"))ui.onNodeWithText(title).performScrollTo().assertIsDisplayed()
   ui.onNodeWithText("Manage accessibility").performScrollTo().performClick();assertEquals(1,managed)
   ui.onNodeWithText("Delete activity history").performScrollTo().performClick();assertEquals(1,erased)
   ui.onNodeWithText("Help & Support").performScrollTo().performClick()
   assertEquals(LegalDocument.SUPPORT.url,opened.single().dataString)
   ui.onNodeWithText("Full guidance and public support page").assertDoesNotExist()
  }finally{speaker.close()}
 }
 @Test fun newRestartIsThirtyAndLegacyGrantedAllowanceIsPreserved()=isolated{store,context,_->
  val today=LocalDate.now().toString()
  store.goal=20;store.prefs.edit().putInt("count:$today:instagram",20).commit()
  assertTrue(store.restartForToday());assertEquals(50,store.goal);assertFalse(store.restartForToday())
  store.prefs.edit().putInt("restartLimit",70).commit()
  assertEquals(70,TrackerStore(context).goal)
  assertEquals(20,store.total())
 }
 @Test fun preloaderUsesVisiblePackagedMisoIcon() {
  ui.setContent{MaterialTheme{MisoPreloader()}}
  ui.onNodeWithContentDescription("Miso app logo").assertIsDisplayed()
  ui.onNodeWithText("antireel").assertDoesNotExist()
 }
 @Test fun compactMisoPlaybackControlsStayAlignedAndVideoPlays()=isolated{store,context,_->
  val speaker=MisoSpeaker(context)
  try {
   ui.setContent{MaterialTheme{MisoPage(store,speaker,false,false,false,Modifier.fillMaxSize(),{},{},{},{})}}
   ui.onNodeWithText("20-second setup video").assertIsDisplayed()
   val replay=ui.onNodeWithText("Replay").fetchSemanticsNode().boundsInRoot
   val full=ui.onNodeWithText("Full screen").fetchSemanticsNode().boundsInRoot
   assertEquals(replay.center.y,full.center.y,1f)
   ui.waitUntil(10000){var active=false;ui.runOnUiThread{val v=ui.activity.window.decorView.findViewWithTag<VideoView>("miso_setup_player");active=v?.isPlaying==true && v.currentPosition>0};active}
   ui.onNodeWithText("Pause").performClick()
   ui.onNodeWithText("Play").assertIsDisplayed()
   ui.onNodeWithText("Replay").performClick()
   ui.onNodeWithText("Full screen").performClick();ui.onNodeWithText("Done").performClick()
   ui.onNodeWithText("Agree & open Settings").performScrollTo().assertIsEnabled()
   assertFalse(store.consent)
  }finally{speaker.close()}
 }
}
