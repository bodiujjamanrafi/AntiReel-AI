package com.rafi.antireelai
import org.junit.Assert.*
import org.junit.Test
class DailyAllowanceTest {
 @Test fun firstReachedBoundaryWins(){assertFalse(dailyAllowanceReached(29,30,59_999,1));assertTrue(dailyAllowanceReached(29,30,60_000,1));assertTrue(dailyAllowanceReached(30,30,1,60))}
 @Test fun disabledTimeGoalDoesNotStopReels(){assertFalse(dailyAllowanceReached(1,30,86_400_000,0));assertEquals(.5f,dailyAllowanceProgress(15,30,86_400_000,0),.0001f)}
 @Test fun timeProgressDrivesWarningBeforeReelGoal(){assertEquals(.8f,dailyAllowanceProgress(1,50,48_000,1),.0001f)}
 @Test fun dayLongAllowancesDoNotOverflow(){assertFalse(dailyAllowanceReached(0,30,86_399_999,1440));assertTrue(dailyAllowanceReached(0,30,86_400_000,1440))}
 @Test fun restoredIncompleteAnswersCannotFinish(){assertFalse(validOnboardingAnswer(7,"Rafi",listOf(5),"0"));assertFalse(validOnboardingAnswer(8,"Rafi",listOf(5),"10001"));assertFalse(validOnboardingAnswer(2,"Rafi",listOf(99),""));assertTrue(validOnboardingAnswer(8,"Rafi",listOf(5),"42"))}
 @Test fun reviewShowsActualCustomValues(){assertEquals("42",onboardingAnswerLabel(8,"Rafi",listOf(5),"42","en"));assertEquals("রাফি",onboardingAnswerLabel(0,"রাফি",emptyList(),"","bn"))}
 @Test fun reactionsDependOnAnswersAndJourneyState(){assertEquals(MisoPose.ENCOURAGING,onboardingPose(8,listOf(4),true));assertEquals(MisoPose.HAPPY,onboardingPose(1,listOf(0),true));assertEquals(MisoPose.CELEBRATING,onboardingPose(12,emptyList(),false))}
 @Test fun localizedNavigationDoesNotRewriteBrandNames(){assertEquals("সেটিংস",UiCopy.translate("Settings","bn"));assertEquals("Instagram",UiCopy.translate("Instagram","bn"));assertEquals("Selected",UiCopy.translate("Selected","en"));assertEquals("12টি রিলস",UiCopy.translate("12 reels","bn"))}
}
