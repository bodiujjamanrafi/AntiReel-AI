package com.rafi.antireelai

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class SettingsMotionTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    @Test fun settingsEntryAndBackNeverTranslateOrScaleThePage() {
        var page by mutableStateOf<String?>(null)
        ui.mainClock.autoAdvance=false
        ui.setContent {MaterialTheme {
            PageMotion(3,page,false,Modifier.fillMaxSize()) {
                Box(Modifier.size(160.dp,120.dp).testTag("page-anchor"))
            }
        }}
        ui.mainClock.advanceTimeBy(200)
        val resting=ui.onNodeWithTag("page-anchor").fetchSemanticsNode().boundsInRoot
        for(target in listOf("GOAL",null,"ALERTS",null,"MISO",null)) {
            ui.runOnUiThread{page=target}
            repeat(12) {
                ui.mainClock.advanceTimeByFrame()
                val bounds=ui.onNodeWithTag("page-anchor").fetchSemanticsNode().boundsInRoot
                assertEquals(resting.left,bounds.left,.5f);assertEquals(resting.top,bounds.top,.5f)
                assertEquals(resting.width,bounds.width,.5f);assertEquals(resting.height,bounds.height,.5f)
            }
        }
    }
}
