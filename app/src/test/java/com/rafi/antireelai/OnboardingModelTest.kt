package com.rafi.antireelai
import org.junit.Assert.*
import org.junit.Test
class OnboardingModelTest {
 @Test fun themeStageIsBetweenLanguageAndWelcome(){assertEquals(-4,nextOnboardingStep(-1));assertEquals(-3,nextOnboardingStep(-4));assertEquals(0,nextOnboardingStep(-3))}
 @Test fun backNavigationPreservesQuestionIndices(){assertEquals(-3,previousOnboardingStep(0));assertEquals(-4,previousOnboardingStep(-3));assertEquals(-1,previousOnboardingStep(-4));assertEquals(2,nextOnboardingStep(0));assertEquals(8,nextOnboardingStep(2));assertEquals(11,nextOnboardingStep(8));assertEquals(12,nextOnboardingStep(11));assertEquals(8,previousOnboardingStep(11))}

 @Test fun everyQuestionHasBothLanguages(){assertEquals(4,onboardingQuestions.size);onboardingQuestions.values.forEach{assertTrue(it.en.isNotBlank());assertTrue(it.bn.isNotBlank());assertEquals(it.options.size,it.banglaOptions.size)}}
 @Test fun removedDraftStagesResumeAtRetainedQuestions(){mapOf(1 to 2,3 to 8,4 to 8,5 to 8,6 to 8,7 to 8,9 to 11,10 to 11).forEach{(old,next)->assertEquals(next,restoredOnboardingStep(old))}}
 @Test fun reelOnlyPlanIsValidWithoutInventingATimeLimit(){assertTrue(OnboardingProfile("Rafi","en",0,20,emptySet(),0,true,"{}").valid)}
 @Test fun retainedCustomAnswersStillValidate(){assertTrue(validOnboardingAnswer(8,"",listOf(5),"42"));assertFalse(validOnboardingAnswer(8,"",listOf(5),"0"));assertFalse(validOnboardingAnswer(2,"",listOf(4),""));assertTrue(validOnboardingAnswer(2,"",listOf(4),"Other app"))}
 @Test fun durationBounds(){assertEquals(90,parseMinutes("1:30"));assertNull(parseMinutes("25:00"));assertNull(parseMinutes("0"));assertNull(parseMinutes("1:99"));assertEquals(1440,parseMinutes("24:00"))}
 @Test fun namesSupportBengaliAndRejectControlCharacters(){assertTrue(validName("রাফি"));assertTrue(validName("Rafi"));assertFalse(validName(" "));assertFalse(validName("A\nB"));assertFalse(validName("x".repeat(41)))}
 @Test fun reelBounds(){assertFalse(validReels("0"));assertFalse(validReels("10001"));assertTrue(validReels("20"));assertFalse(validReels("1.5"))}
}
