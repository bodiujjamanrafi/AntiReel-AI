package com.rafi.antireelai

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

class LocalizedUiTest {
 @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
 @Test fun bengaliDashboardSettingsAndNavigationStayUsableInBothThemes(){
  var selected by mutableIntStateOf(0);var dark by mutableStateOf(false)
  val previous=AppStyle.dark
  InstrumentationRegistry.getInstrumentation().runOnMainSync{ui.activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
  try{
   ui.setContent{SideEffect{AppStyle.dark=dark};CompositionLocalProvider(LocalUiLanguage provides "bn"){MaterialTheme{Column(Modifier.fillMaxSize()){
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())){SettingsIndex({},{}) ;TodayMetrics(3,30,30_000,1)}
    ReferenceNavigation(selected,true){selected=it}
   }}}}
   ui.waitUntil(10000){runCatching{ui.onAllNodesWithText("মিসো ও পর্দা").fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)}
   ui.onNodeWithText("মিসো ও পর্দা").assertIsDisplayed()
   ui.onNodeWithContentDescription("মিসো").performClick().assertIsSelected()
   ui.runOnIdle{dark=true}
   ui.onNodeWithContentDescription("ব্যবহার").performClick().assertIsSelected()
   ui.onNodeWithText("আজকের রিলস").performScrollTo().assertIsDisplayed()
   ui.onNodeWithText("দৈনিক সীমা").assertIsDisplayed()
  }finally{
   ui.runOnIdle{AppStyle.dark=previous;ui.activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)}
  }
 }
}
