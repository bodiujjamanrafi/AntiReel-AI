package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Real app route switching, without changing permissions, goals or selected platforms. */
class SingleSplashNavigationTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 @Test fun nativeSplashIsNotFollowedByAnotherPreloaderAndRepeatedNavigationRemainsStable() {
  assumeTrue(android.os.Build.VERSION.SDK_INT>=31)
  val store=TrackerStore(ui.activity)
  assumeTrue(store.prefs.getBoolean("onboardingComplete",false))
  val before=store.prefs.all.toMap()
  ui.onNodeWithTag("miso-preloader").assertDoesNotExist()
  repeat(3) {
   for(label in listOf("Platforms","Activity","Settings","Miso","Today")) {
    ui.onNodeWithContentDescription(label).performClick().assertIsSelected()
    ui.onNodeWithTag("miso-preloader").assertDoesNotExist()
   }
  }
  ui.onNodeWithText("Reels today").assertIsDisplayed()
  for(key in listOf("goal","dailyMinutes","platforms","restartDay","restartLimit","onboardingComplete"))
   assertEquals(key,before[key],store.prefs.all[key])
 }
}
