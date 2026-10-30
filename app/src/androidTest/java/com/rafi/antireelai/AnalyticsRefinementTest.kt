package com.rafi.antireelai

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Isolated in-memory datasets; never writes usage, allowances, permissions or preferences. */
class AnalyticsRefinementTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val today=LocalDate.now()
    private val output get()=File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,"analytics-review").apply{mkdirs()}
    private fun history(mode:Int)=(0..29).map{offset->
        ActivityDay(today.minusDays(offset.toLong()), platforms.indices.map{i->
            when(mode){0->0;1->if(offset==2 && i==3)7 else 0;2->if(offset<7)(i+1)*(7-offset) else 0;else->if(offset==0 && i==1)1_000_000 else 0}
        },platforms.indices.map{i->
            when(mode){0->0L;1->if(offset==2 && i==3)17_000L else 0L;2->if(offset<7)(i+1)*23_000L*(7-offset) else 0L;else->if(offset==0 && i==1)86_400_000L else 0L}
        })
    }
    @Test fun chartSelectionAndMetricSwitchesUseTheRealSelectedDay() {
        var selected by mutableStateOf(today)
        val data=history(1)
        val oldDark=AppStyle.dark
        try {
            ui.setContent {MaterialTheme {
                Column(Modifier.width(320.dp).verticalScroll(rememberScrollState())) {
                    UsageTrendChart(data,true,selected,{selected=it})
                    Box(Modifier.testTag("history")){UsageWeekChart(data,selected){selected=it}}
                    SelectedDaySummary(activityDayOrEmpty(data,selected))
                    Box(Modifier.testTag("distribution")){UsageDonut(activityDayOrEmpty(data,selected))}
                }
            }}
            val date=today.minusDays(2)
            ui.onNodeWithContentDescription("$date, 7 reels, 17s").performScrollTo().performClick()
            ui.runOnIdle{assertEquals(date,selected)}
            ui.onNodeWithContentDescription("Select $today").performScrollTo().performClick()
            ui.runOnIdle{assertEquals(today,selected)}
            ui.onAllNodesWithText("Reels").fetchSemanticsNodes().also{assertEquals(2,it.size)}
            ui.onAllNodesWithText("Reels")[0].performScrollTo().performClick()
            ui.onAllNodesWithText("Reels")[1].performScrollTo().performClick()
            ui.onNodeWithText("No data yet").assertExists()
            // The oldest partial week cannot navigate beyond retention, and future is disabled.
            ui.onNodeWithContentDescription("Next seven days").performScrollTo().assertIsNotEnabled()
            repeat(4){ui.onNodeWithContentDescription("Previous seven days").performScrollTo().performClick()}
            ui.onNodeWithContentDescription("Previous seven days").assertIsNotEnabled()
            ui.runOnIdle{assertFalse(selected.isBefore(today.minusDays(29)))}
        } finally {ui.runOnIdle{AppStyle.dark=oldDark}}
    }
    @Test fun bothThemesRenderZeroSingleMultiAndHighValuesAtNarrowWidth() {
        var dark by mutableStateOf(false)
        var scenario by mutableStateOf(0)
        var language by mutableStateOf("en")
        val oldDark=AppStyle.dark
        try {
            ui.setContent {
                AppStyle.dark=dark
                val density=LocalDensity.current
                val data=remember(scenario){history(scenario)}
                val day=if(scenario==1)today.minusDays(2) else today
                CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f),LocalUiLanguage provides language) {
                    MaterialTheme {
                        Column(Modifier.width(320.dp).fillMaxHeight().background(AppStyle.Background).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.testTag("utilities"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                    ReferenceQuickAction("Focus mode",UtilityIcon.FOCUS,AppStyle.Teal,Modifier.weight(1f)){}
                                    ReferenceQuickAction("Break time",UtilityIcon.BREAK,AppStyle.Coral,Modifier.weight(1f)){}
                                }
                                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                    ReferenceQuickAction("Daily goal",UtilityIcon.GOAL,AppStyle.Blue,Modifier.weight(1f)){}
                                    ReferenceQuickAction("Counter style",UtilityIcon.COUNTER,AppStyle.Accent,Modifier.weight(1f)){}
                                }
                            }
                            Box(Modifier.testTag("recent")){RecentDays(data){}}
                            Box(Modifier.testTag("trend")){UsageTrendChart(data,true,day,{})}
                            Box(Modifier.testTag("bars")){UsageWeekChart(data,day){}}
                            Box(Modifier.testTag("summary")){SelectedDaySummary(activityDayOrEmpty(data,day))}
                            Box(Modifier.testTag("donut")){UsageDonut(activityDayOrEmpty(data,day))}
                        }
                    }
                }
            }
            for(theme in listOf(false,true))for(mode in 0..3) {
                ui.runOnIdle {dark=theme;scenario=mode;language=if(mode==2)"bn" else "en"}
                for(tag in listOf("utilities","recent","trend","bars","summary","donut")) {
                    val node=ui.onNodeWithTag(tag).performScrollTo()
                    node.assertIsDisplayed()
                    val bitmap=node.captureToImage().asAndroidBitmap()
                    File(output,"${if(theme)"dark" else "light"}-$mode-$tag.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
                }
            }
        } finally {ui.runOnIdle{AppStyle.dark=oldDark}}
    }
    @Test fun correctedMisoKeepsBothArmsAndRendersFromFrontAndAngles() {
        // Real mesh projection, not a generated replacement or a flattened reference image.
        for(angle in listOf(-.35f,0f,.35f))for(resting in listOf(false,true)) {
            val bitmap=MisoBodyRenderer(angle).render(640,resting)
            val pixels=IntArray(640*640);bitmap.getPixels(pixels,0,640,0,0,640,640)
            assertTrue(pixels.count{android.graphics.Color.alpha(it)>0}>640*640/10)
            File(output,"miso-$angle-$resting.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
    }
}
