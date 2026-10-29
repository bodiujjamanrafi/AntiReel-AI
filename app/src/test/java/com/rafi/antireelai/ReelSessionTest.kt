package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class ReelSessionTest {
    private fun frame(text:String?="a")=ReelFrame(true,text,"test")
    @Test fun repeatedEventsAndPollsCountOnce() {
        val c=ReelSession();assertFalse(c.observe("youtube",frame(),"0",true,0));assertTrue(c.observe("youtube",frame(),"0",true,100));repeat(20){assertFalse(c.observe("youtube",frame(),"0",true,200L+it))}
    }
    @Test fun hiddenMetadataUsesDistinctPagerPages() {
        val c=ReelSession();c.observe("facebook",frame(null),"0",true,0);assertTrue(c.observe("facebook",frame(null),"0",true,100))
        assertFalse(c.observe("facebook",frame(null),"1",true,200));assertTrue(c.observe("facebook",frame(null),"1",true,300));assertFalse(c.observe("facebook",frame(null),"1",true,400))
    }
    @Test fun delayedCaptionAfterPagerDoesNotDoubleCount() {
        val c=ReelSession();c.observe("tiktok",frame(),"0",true,0);c.observe("tiktok",frame(),"0",true,100)
        c.observe("tiktok",frame(),"1",true,200);assertTrue(c.observe("tiktok",frame(),"1",true,300))
        assertFalse(c.observe("tiktok",frame("b"),"1",true,350));assertFalse(c.observe("tiktok",frame("b"),"1",true,500))
    }
    @Test fun delayedPagerAfterCaptionDoesNotDoubleCount() {
        val c=ReelSession();c.observe("youtube",frame(),"0",true,0);c.observe("youtube",frame(),"0",true,100)
        c.observe("youtube",frame("b"),"0",true,200);assertTrue(c.observe("youtube",frame("b"),"0",true,300))
        assertFalse(c.observe("youtube",frame("b"),"1",true,350));assertFalse(c.observe("youtube",frame("b"),"1",true,500))
    }
    @Test fun fastConsecutiveCaptionChangesCanBothCount() {
        val c=ReelSession();c.observe("instagram",frame(),null,true,0);assertTrue(c.observe("instagram",frame(),null,true,100))
        c.observe("instagram",frame("b"),null,true,200);assertTrue(c.observe("instagram",frame("b"),null,true,300))
        c.observe("instagram",frame("c"),null,true,400);assertTrue(c.observe("instagram",frame("c"),null,true,500))
    }
    @Test fun strictModeDoesNotInventCountsWhenMetadataHidden() {
        val c=ReelSession();assertFalse(c.observe("facebook",frame(null),"0",false,0));assertFalse(c.observe("facebook",frame(null),"1",false,500));assertFalse(c.pending)
    }
    @Test fun unrelatedScreensNeverCountEvenWithPagerEvents() {
        val c=ReelSession();val normal=ReelFrame(false,"post","waiting_player");assertFalse(c.observe("facebook",normal,"0",true,0));assertFalse(c.observe("facebook",normal,"1",true,500))
    }
    @Test fun switchingPlatformsCountsIndependently() {
        val c=ReelSession();c.observe("instagram",frame(),"0",true,0);assertTrue(c.observe("instagram",frame(),"0",true,100));assertFalse(c.observe("youtube",frame(),"0",true,200));assertTrue(c.observe("youtube",frame(),"0",true,300))
    }
    @Test fun sameAuthorCaptionOnDifferentPagesStillCounts() {
        val c=ReelSession();c.observe("likee",frame(),"0",true,0);c.observe("likee",frame(),"0",true,100);c.observe("likee",frame(),"1",true,200);assertTrue(c.observe("likee",frame(),"1",true,300));c.observe("likee",frame(),"2",true,450);assertTrue(c.observe("likee",frame(),"2",true,550))
    }
    @Test fun briefRecognitionGapsDoNotRecountSameVideo() {
        val c=ReelSession();c.observe("snapchat",frame(),"0",true,0);c.observe("snapchat",frame(),"0",true,100);c.observe("snapchat",ReelFrame(false,null,"waiting_player"),"0",true,300);assertFalse(c.observe("snapchat",frame(),"0",true,400))
    }
    @Test fun expressionStagesFollowGoal() {
        assertEquals(CompanionMood.RELAXED,CompanionMood.fromProgress(.2f));assertEquals(CompanionMood.RELAXED,CompanionMood.fromProgress(.5f));assertEquals(CompanionMood.RELAXED,CompanionMood.fromProgress(.8f));assertEquals(CompanionMood.BREATHE,CompanionMood.fromProgress(1f))
    }

    @Test fun breakRestsAndOtherStatesFollowTheDailyGoal() {
        assertEquals(CompanionMood.RESTING,CompanionMood.forActivity(.1f,CompanionActivity.BREAK))
        assertEquals(CompanionMood.RESTING,CompanionMood.forActivity(2f,CompanionActivity.BREAK))
        assertEquals(CompanionMood.BREATHE,CompanionMood.forActivity(2f,CompanionActivity.STANDBY))
        assertEquals(CompanionMood.BREATHE,CompanionMood.forActivity(1f,CompanionActivity.ACTIVE))
    }

    @Test fun forwardReelsCountButBacktrackingAndRevisitsDoNot() {
        val g=NovelReelGate()
        assertTrue(g.allow("youtube",frame("a"),0,0,0,"today"))
        assertTrue(g.allow("youtube",frame("b"),1,1,100,"today"))
        assertFalse(g.allow("youtube",frame("a"),0,-1,200,"today"))
        assertFalse(g.allow("youtube",frame("b"),1,1,300,"today"))
        assertTrue(g.allow("youtube",frame("c"),2,1,400,"today"))
    }
    @Test fun captionChangesWithoutForwardEvidenceNeverCreateNewReels() {
        val g=NovelReelGate();assertTrue(g.allow("instagram",frame("a"),0,0,0,"today"))
        assertFalse(g.allow("instagram",frame("edited"),0,0,0,"today"))
    }
    @Test fun hiddenLabelsUseForwardPositionsAndNeverCountBackward() {
        val g=NovelReelGate();assertTrue(g.allow("facebook",frame(null),0,0,0,"today"))
        assertTrue(g.allow("facebook",frame(null),1,1,100,"today"))
        assertFalse(g.allow("facebook",frame(null),0,-1,200,"today"))
        assertFalse(g.allow("facebook",frame(null),1,1,300,"today"))
    }
    @Test fun replayMemorySurvivesPlayerSwitchButResetsNextDay() {
        val g=NovelReelGate();assertTrue(g.allow("youtube",frame("a"),0,0,0,"day1"))
        g.resetPosition();assertFalse(g.allow("youtube",frame("a"),0,0,0,"day1"))
        assertTrue(g.allow("youtube",frame("a"),0,0,0,"day2"))
    }
    @Test fun knownIdentityIsScopedToPlatform() {
        val g=NovelReelGate();assertTrue(g.allow("youtube",frame("a"),0,0,0,"day1"))
        assertTrue(g.allow("instagram",frame("a"),0,0,0,"day1"))
    }
    @Test fun clearingActivityClearsDuplicateMemory() {
        val g=NovelReelGate();g.allow("youtube",frame("a"),0,0,0,"today");g.clear()
        assertTrue(g.allow("youtube",frame("a"),0,0,0,"today"))
    }
    @Test fun limitStagesAreExactAtEightyPercentAndAfterGoal() {
        assertEquals(LimitStage.NONE,LimitStage.at(39,50))
        assertEquals(LimitStage.NEAR,LimitStage.at(40,50))
        assertEquals(LimitStage.REACHED,LimitStage.at(50,50))
        assertEquals(LimitStage.EXCEEDED,LimitStage.at(51,50))
    }

    @Test fun happyBelowWarningAndAngryFromTheWarningBoundary() {
        listOf(0f,.49f,.5f,.79f,.7999f,.8f,.9f,.999f).forEach{assertEquals(CompanionMood.RELAXED,CompanionMood.fromProgress(it))}
        listOf(1f,2f).forEach{assertEquals(CompanionMood.BREATHE,CompanionMood.fromProgress(it))}
    }
    @Test fun redCurtainIsAbsentBeforeWarningAndOpaqueOnlyAtTheActualLimit() {
        assertEquals(0f,limitCoverOpacity(.79f),.0001f)
        assertTrue(limitCoverOpacity(.8f)>=.48f)
        assertTrue(limitCoverOpacity(.9f)>limitCoverOpacity(.8f))
        assertTrue(limitCoverOpacity(.95f)<1f)
        assertEquals(1f,limitCoverOpacity(1f),.0001f)
        assertEquals(1f,limitCoverOpacity(2f),.0001f)
    }
    @Test fun redCurtainStrengthIsBoundedAndNeverDecreasesAsUsageGrows() {
        val values=(0..150).map{limitCoverOpacity(it/100f)}
        assertTrue(values.all{it in 0f..1f})
        assertTrue(values.zipWithNext().all{(a,b)->b>=a})
    }
}
