package com.rafi.antireelai

import com.rafi.antireelai.tracking.LimitStage
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import androidx.compose.ui.graphics.luminance

class SettingsPolicyTest {
    @Test fun groupedSettingsPreserveEveryDestinationExactlyOnce() {
        val grouped=settingsCategories.flatMap{it.sections}
        assertEquals(SettingsSection.entries.toSet(),grouped.toSet())
        assertEquals(SettingsSection.entries.size,grouped.size)
        assertEquals(1,settingsCategories.count{it.includesPlatforms})
    }
    @Test fun darkDividersAreVisibleWithoutBecomingBrightBorders() {
        val line=settingsDividerColor(true).luminance()
        val surface=AppStyle.Dark.surface.luminance()
        val contrast=(line+.05f)/(surface+.05f)
        assertTrue(contrast>=1.7f);assertTrue(contrast<3f)
    }
    @Test fun destructiveSettingsActionsRemainReadableInBothThemes() {
        listOf(false,true).forEach{dark->
            val foreground=settingsErrorColor(dark).luminance()
            val background=AppStyle.colorsFor(dark).surface.luminance()
            assertTrue((maxOf(foreground,background)+.05f)/(minOf(foreground,background)+.05f)>=4.5f)
        }
    }
    @Test fun customGoalsAreNotRoundedOrRestrictedToPresets() {
        listOf(1,7,37,151,999,Int.MAX_VALUE).forEach{assertEquals(it,parseDailyGoal(it.toString()))}
    }
    @Test fun invalidGoalsCannotBeSaved() {
        listOf("", "0", "-5", "1.5", "2147483648", "words").forEach{assertNull(parseDailyGoal(it))}
    }
    @Test fun warningStagesHaveDifferentMessagesAndNoWarningBelowThreshold() {
        val spoken=listOf(LimitStage.NEAR,LimitStage.REACHED,LimitStage.EXCEEDED).map(::warningMessage)
        assertEquals(3,spoken.distinct().size);assertTrue(spoken.all{it.isNotBlank()})
        assertTrue(warningMessage(LimitStage.NONE).isBlank())
    }
    @Test fun staleAlarmCannotFinishAReplacementBreak() {
        assertEquals(BreakDue.STALE,breakDue(5000,3000,6000))
        assertEquals(BreakDue.INACTIVE,breakDue(0,3000,6000))
    }
    @Test fun earlyAlarmWaitsAndExactDeadlineCompletes() {
        assertEquals(BreakDue.WAITING,breakDue(5000,5000,4999))
        assertEquals(BreakDue.COMPLETE,breakDue(5000,5000,5000))
        assertEquals(BreakDue.COMPLETE,breakDue(5000,0,6000))
    }
    private fun day(reels:Int=0,millis:Long=0):ActivityDay=ActivityDay(LocalDate.of(2026,10,25),List(platforms.size){if(it==0)reels else 0},List(platforms.size){if(it==0)millis else 0})
    @Test fun emptyInsightsNeverInventUsageOrComparisons() {
        val insight=dailyInsight(day(),null,50)
        assertTrue(insight.detail.contains("No activity"));assertFalse(insight.detail.contains("%"))
    }
    @Test fun insightUsesRecordedPlatformShareAndActualLimitProgress() {
        val insight=dailyInsight(day(45,60000),null,50)
        assertTrue(insight.detail.contains("Instagram"));assertTrue(insight.detail.contains("100%"))
        assertTrue(insight.advice.contains("5 reels"))
    }
    @Test fun oldCountsWithoutDurationsDoNotInventTime() {
        val insight=dailyInsight(day(20),null,50)
        assertTrue(insight.detail.contains("No video-screen duration"));assertFalse(insight.detail.contains("%"))
    }

    @Test fun bothThemesHaveReadablePrimaryAndSecondaryTextOnCardsAndBackgrounds() {
        val original=AppStyle.dark
        try {
            listOf(false,true).forEach{dark->
                AppStyle.dark=dark
                listOf(AppStyle.Background,AppStyle.Surface).forEach{background->
                    listOf(AppStyle.Text,AppStyle.Muted,AppStyle.Accent).forEach{foreground->
                        val a=foreground.luminance();val b=background.luminance()
                        val contrast=(maxOf(a,b)+.05f)/(minOf(a,b)+.05f)
                        assertTrue("Insufficient text contrast in dark=$dark: $contrast",contrast>=4.5f)
                    }
                }
            }
        } finally {AppStyle.dark=original}
    }
}
