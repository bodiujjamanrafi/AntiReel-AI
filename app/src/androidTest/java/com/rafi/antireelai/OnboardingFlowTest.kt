package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

/** All state is isolated; no test clears or rewrites the user's real preferences. */
class OnboardingFlowTest {
 @get:Rule val ui=createAndroidComposeRule<androidx.activity.ComponentActivity>()
 private fun waitText(value:String){ui.waitUntil(15000){runCatching{ui.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}}
 private fun isolated(action:(TrackerStore)->Unit){
  val base=InstrumentationRegistry.getInstrumentation().targetContext
  val file="onboarding-test-${System.nanoTime()}"
  val context=object:ContextWrapper(base){override fun getSharedPreferences(name:String,mode:Int):SharedPreferences=base.getSharedPreferences(file,mode)}
  val store=TrackerStore(context)
  store.prefs.edit().putBoolean("onboardingMuted",true).putInt("count:${java.time.LocalDate.now()}:instagram",7).commit()
  InstrumentationRegistry.getInstrumentation().runOnMainSync{ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
  try{action(store)}finally{
   InstrumentationRegistry.getInstrumentation().runOnMainSync{ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
   base.deleteSharedPreferences(file)
  }
 }
 private fun draft(name:String="Rafi")=JSONObject().apply{put("name",name);for(i in 1..11)put("a$i","0")}
 @Test fun englishJourneyPersistsRealAllowanceWithoutClearingHistory()=isolated{store->
  var finished=false
  ui.setContent{MaterialTheme{OnboardingFlow(store){finished=true}}}
  ui.mainClock.advanceTimeBy(2000)
  waitText("Choose your language");ui.onNodeWithText("Continue").performClick()
  waitText("Choose your theme");ui.onNodeWithTag("theme-dark").performClick();ui.onNodeWithText("Continue").performClick()
  waitText("Hi, I'm Miso. Let's make room for your time.");ui.onNodeWithText("Continue").performClick()
  for(i in onboardingQuestionIds){
   waitText(onboardingQuestions.getValue(i).en)
   if(i==0){ui.onNodeWithText("Continue").assertIsNotEnabled();ui.onNodeWithText("Your name").performTextInput("Rafi")}
   else ui.onNodeWithText(onboardingQuestions.getValue(i).options.first()).performClick()
   ui.onNodeWithText("Continue").performClick()
  }
  waitText("Your plan, Rafi.");ui.onNodeWithText("Finish").performClick()
  waitText("You're all set, Rafi!");ui.onNodeWithText("Start My Journey").performClick()
  ui.waitUntil(10000){finished}
  assertTrue(store.prefs.getBoolean("onboardingComplete",false));assertEquals(0,store.dailyMinutes);assertEquals(5,store.baseGoal);assertEquals("dark",store.themeMode)
  assertEquals(7,store.total());assertEquals("Rafi",store.prefs.getString("userName",null))
 }
 @Test fun continueFooterStaysStableAndButtonsEmitHaptics()=isolated{store->
  var taps=0
  ui.setContent{MaterialTheme{OnboardingFlow(store,hapticFeedback={taps++}){}}}
  ui.mainClock.advanceTimeBy(2000);waitText("Choose your language")
  fun bottom()=ui.onNodeWithTag("onboarding-continue").fetchSemanticsNode().boundsInRoot.bottom
  val anchor=bottom()
  ui.onNodeWithText("English").performClick();ui.onNodeWithText("Continue").performClick()
  waitText("Choose your theme");assertEquals(anchor,bottom(),1f)
  ui.onNodeWithTag("theme-light").performClick();assertEquals("light",store.themeMode)
  ui.onNodeWithText("Continue").performClick();waitText("Hi, I'm Miso. Let's make room for your time.")
  assertEquals(anchor,bottom(),1f)
  ui.onNodeWithText("Continue").performClick();waitText(onboardingQuestions.getValue(0).en)
  assertEquals(anchor,bottom(),1f);assertTrue(taps>=5)
  store.haptics=false
  val before=taps;ui.onNodeWithText("Back").performClick();assertEquals(before,taps)
 }
 @Test fun bengaliJourneyRestoresDraftAndKeepsCustomGoals()=isolated{store->
  val state=draft("রাফি").apply{put("a7","5");put("c7","1:30");put("a8","5");put("c8","42")}
  store.prefs.edit().putString("language","bn").putInt("dailyMinutes",90).putInt("onboardingStep",7).putString("onboardingDraft",state.toString()).commit()
  var mounted by mutableStateOf(true);var finished=false
  ui.setContent{MaterialTheme{if(mounted)OnboardingFlow(store){finished=true}}}
  waitText(onboardingQuestions.getValue(8).bn)
  ui.onNodeWithText("রিলস (১–১০০০০)").assertExists()
  // Dispose and recreate the journey from its committed draft, as on process restoration.
  ui.runOnIdle{mounted=false};ui.waitForIdle();ui.runOnIdle{mounted=true}
  waitText(onboardingQuestions.getValue(8).bn)
  for(i in onboardingQuestionIds.filter{it>=8}){waitText(onboardingQuestions.getValue(i).bn);ui.onNodeWithText("এগিয়ে যাই").performClick()}
  waitText("তোমার পরিকল্পনা, রাফি।");ui.onNodeWithText("শেষ করি").performClick()
  waitText("সব প্রস্তুত, রাফি!");ui.onNodeWithText("আমার যাত্রা শুরু করি").performClick()
  ui.waitUntil(10000){finished};assertEquals("bn",store.prefs.getString("language",null));assertEquals(90,store.dailyMinutes);assertEquals(42,store.baseGoal);assertEquals(7,store.total())
 }
 @Test fun failedFinalSaveNeverReportsCompletion()=isolated{store->
  store.prefs.edit().putInt("onboardingStep",12).putString("onboardingDraft",draft().toString()).commit()
  var finished=false
  ui.setContent{MaterialTheme{OnboardingFlow(store,saveProfile={false}){finished=true}}}
  waitText("Your plan, Rafi.");ui.onNodeWithText("Finish").performClick()
  waitText("Start My Journey");ui.onNodeWithText("Start My Journey").performClick()
  waitText("Couldn't save. Please try again.");assertFalse(finished);assertFalse(store.prefs.getBoolean("onboardingComplete",false));assertEquals(7,store.total())
 }
 @Test fun incompleteRestoredSummaryRequiresMissingAnswers()=isolated{store->
  val state=draft().apply{remove("a8")}
  store.prefs.edit().putInt("onboardingStep",12).putString("onboardingDraft",state.toString()).commit()
  ui.setContent{MaterialTheme{OnboardingFlow(store){}}}
  waitText("Your plan, Rafi.");ui.onNodeWithText("Finish").performClick()
  waitText(onboardingQuestions.getValue(8).en);ui.onNodeWithText("Continue").assertIsNotEnabled()
  assertFalse(store.prefs.getBoolean("onboardingComplete",false))
 }
 @Test fun smallScreenAndLargeFontsKeepContinueReachable()=isolated{store->
  store.prefs.edit().putInt("onboardingStep",7).putString("onboardingDraft",draft().toString()).commit()
  ui.setContent{MaterialTheme{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.8f)){Box(Modifier.requiredSize(320.dp,540.dp)){OnboardingFlow(store){}}}}}
  waitText(onboardingQuestions.getValue(8).en);ui.onNodeWithText("Continue").assertIsDisplayed().assertIsEnabled()
 }

 @Test fun answerEditorReturnsToSettingsWithoutStartingOnboardingAgain()=isolated{store->
  store.prefs.edit().putBoolean("onboardingComplete",true).putString("onboardingDraft",draft().toString()).commit()
  var editing by mutableStateOf(false)
  ui.setContent{MaterialTheme{
   if(editing)OnboardingFlow(store,startReview=true,onExit={editing=false}){editing=false}
   else androidx.compose.material3.TextButton(onClick={editing=true}){androidx.compose.material3.Text("Edit my answers")}
  }}
  ui.onNodeWithText("Edit my answers").performClick();waitText("Your plan, Rafi.")
  ui.onNodeWithText(onboardingQuestions.getValue(0).en).performScrollTo().performClick()
  waitText("Your name")
  ui.runOnIdle{ui.activity.onBackPressedDispatcher.onBackPressed()}
  waitText("Your plan, Rafi.")
  ui.runOnIdle{ui.activity.onBackPressedDispatcher.onBackPressed()}
  ui.onNodeWithText("Edit my answers").assertIsDisplayed()
  assertTrue(store.prefs.getBoolean("onboardingComplete",false));assertEquals(7,store.total())
 }

 @Test fun voiceEnabledAnswersAdvanceWithoutSpokenReactionWait()=isolated{store->
  store.prefs.edit().putBoolean("onboardingMuted",false).putInt("onboardingStep",0).putString("onboardingDraft",draft().toString()).commit()
  ui.setContent{MaterialTheme{OnboardingFlow(store){}}}
  waitText("Your name")
  ui.onNodeWithText("Continue").performClick()
  ui.waitUntil(2500){runCatching{ui.onAllNodesWithText(onboardingQuestions.getValue(2).en).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}
  ui.onNodeWithText("Miso is responding…").assertDoesNotExist()
  ui.onNodeWithText("Continue").assertIsDisplayed()
 }
}
