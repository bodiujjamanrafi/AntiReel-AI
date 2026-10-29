package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class YouTubeShortsTrackerTest {
    private fun video(id:String)=ReelFrame(true,id,"youtube_metadata")
    private val missing=ReelFrame(true,null,"youtube_missing_identity")
    private val outside=ReelFrame(false,null,"waiting_player")
    private val ad=ReelFrame(false,null,"advertisement")
    private fun settle(t:YouTubeShortsTracker,id:String,now:Long):Boolean {
        assertFalse(t.observe(video(id),now,"day"))
        return t.observe(video(id),now+80,"day")
    }
    @Test fun thirtyConsecutiveZeroMetricShortsWithRapidAndSlowScrolls() {
        val t=YouTubeShortsTracker();var count=0
        repeat(30){i->
            if(i>0)t.scroll(0,null)
            if(settle(t,"video-$i",i*250L))count++
            repeat(8){assertFalse(t.observe(video("video-$i"),i*250L+90+it,"day"))}
        }
        assertEquals(30,count)
    }
    @Test fun delayedMetadataDoesNotExpirePlayerTransition() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        t.scroll(0,null)
        assertFalse(t.observe(video("a"),1000,"day"))
        assertFalse(t.observe(missing,2500,"day"))
        assertTrue(settle(t,"b",6000))
    }
    @Test fun scrollBurstWithOneNewIdentityCountsOnce() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        repeat(20){t.scroll(0,null);assertFalse(t.observe(video("a"),100L+it,"day"))}
        assertTrue(settle(t,"b",300))
        repeat(20){t.scroll(0,null);assertFalse(t.observe(video("b"),500L+it,"day"))}
    }
    @Test fun metadataBeforeScrollAndScrollBeforeMetadataBothWork() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        assertFalse(t.observe(video("b"),200,"day"));assertFalse(t.observe(video("b"),300,"day"))
        t.scroll(0,null);assertTrue(settle(t,"b",400))
        t.scroll(0,null);assertFalse(t.observe(video("b"),600,"day"))
        assertTrue(settle(t,"c",900))
    }
    @Test fun snapshotFromBeforeLatestScrollCannotCommit() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        val old=t.generation;t.scroll(0,null)
        assertFalse(t.observe(video("b"),1000,"day",old))
        assertEquals("stale_snapshot",t.diagnostic)
        assertTrue(settle(t,"c",1100))
    }
    @Test fun reverseRevisitAndRepeatedVideoNeverDoubleCount() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        t.scroll(1,1);assertTrue(settle(t,"b",200))
        t.scroll(-1,0);assertFalse(settle(t,"a",400))
        t.scroll(-1,-1);assertFalse(settle(t,"unseen-backward",600))
        t.scroll(1,1);assertFalse(settle(t,"b",800))
        t.scroll(1,2);assertTrue(settle(t,"c",1000))
    }
    @Test fun stationaryMetadataChangesAndSwipesWithoutIdentityDoNotCount() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        assertFalse(settle(t,"edited-caption",200))
        t.scroll(0,null)
        repeat(30){assertFalse(t.observe(missing,500L+it*100,"day"))}
        assertEquals("missing_identity",t.diagnostic)
    }
    @Test fun identityMustActuallySettleAndNotBeAnIntermediateCaption() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0));t.scroll(0,null)
        assertFalse(t.observe(video("partial"),200,"day"))
        assertFalse(t.observe(video("complete"),240,"day"))
        assertFalse(t.observe(video("complete"),300,"day"))
        assertTrue(t.observe(video("complete"),320,"day"))
        assertFalse(t.observe(video("later-edit"),500,"day"))
    }
    @Test fun adsDoNotCountOrAuthorizeFollowingTextUpdates() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0));t.scroll(0,null)
        assertFalse(t.observe(ad,200,"day"));assertFalse(settle(t,"ad-caption",400))
        t.scroll(0,null);assertTrue(settle(t,"b",600))
    }
    @Test fun reopeningKeepsSeenVideosAndDayChangeClearsThem() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0));t.resetSession()
        assertFalse(settle(t,"a",300));assertTrue(settle(t,"b",600))
        assertFalse(t.observe(video("a"),1000,"next-day"))
        assertTrue(t.observe(video("a"),1080,"next-day"))
    }
    @Test fun absentPlayerResetsPositionWithoutForgettingSeenContent() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0));t.scroll(1,5)
        assertTrue(settle(t,"b",300))
        assertFalse(t.observe(outside,600,"day"));assertFalse(t.observe(outside,1600,"day"))
        assertTrue(settle(t,"c",1800));assertFalse(settle(t,"b",2000))
    }
    @Test fun indexedFallbackRequiresAnActualPageAndRespectsCompatibility() {
        val t=YouTubeShortsTracker()
        assertFalse(t.observe(missing,0,"day"));t.scroll(1,1)
        assertFalse(t.observe(missing,100,"day",compatibility=false))
        assertFalse(t.observe(missing,200,"day"));assertTrue(t.observe(missing,280,"day"))
        t.scroll(0,1);assertFalse(t.observe(missing,500,"day"))
        t.scroll(1,2);assertFalse(t.observe(missing,600,"day"));assertTrue(t.observe(missing,680,"day"))
    }
    @Test fun fastReentryFromHomeSearchOrSubscriptionsAcceptsNewVideo() {
        val t=YouTubeShortsTracker();assertTrue(settle(t,"a",0))
        assertFalse(t.observe(outside,100,"day"))
        assertTrue(settle(t,"b",120))
        assertFalse(t.observe(outside,220,"day"))
        assertFalse(settle(t,"a",240))
    }
}
