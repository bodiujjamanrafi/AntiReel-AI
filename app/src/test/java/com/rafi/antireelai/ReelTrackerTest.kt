package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class ReelTrackerTest {
    private val a=ReelFrame(true,"a","metadata_ready")
    private val b=ReelFrame(true,"b","metadata_ready")
    @Test fun captionBeforePagerCannotLoseTheNextReelOnAnyPlatform() {
        platforms.forEach{p->
            val t=ReelTracker()
            fun read(f:ReelFrame,page:Int?,dir:Int,at:Long,time:Long)=t.observe(p.id,f,"page:${page ?:0}",true,page,dir,at,time,"today")
            assertFalse(p.id,read(a,null,0,0,1000));assertTrue(p.id,read(a,null,0,0,1100))
            assertFalse(p.id,read(b,null,0,0,1200));assertFalse(p.id,read(b,null,0,0,1300))
            // The candidate settles before forward evidence: it must remain eligible.
            assertTrue(p.id,read(b,1,1,1350,1400));assertFalse(p.id,read(b,1,1,1350,1600))
            assertFalse(p.id,read(a,0,-1,1700,1800));assertFalse(p.id,read(a,0,-1,1700,1900))
        }
    }
    @Test fun hiddenMetadataCountsDistinctForwardPagesButNeverAdsOrRevisits() {
        val t=ReelTracker();val f=ReelFrame(true,null,"player_ready");val ad=ReelFrame(false,null,"advertisement")
        fun read(frame:ReelFrame,page:Int,dir:Int,time:Long)=t.observe("youtube",frame,"page:$page",true,page,dir,page*1000L,time,"today")
        assertFalse(read(f,1,1,1000));assertTrue(read(f,1,1,1100))
        assertFalse(read(ad,2,1,2000));assertFalse(read(ad,2,1,2200))
        assertFalse(read(f,3,1,3000));assertTrue(read(f,3,1,3100))
        assertFalse(read(f,2,-1,4000));assertFalse(read(f,2,-1,4100))
        assertFalse(read(f,3,1,5000));assertFalse(read(f,3,1,5100))
        assertFalse(read(f,4,1,6000));assertTrue(read(f,4,1,6100))
    }
    @Test fun playbackMetadataEditsWithoutMovementNeverIncreaseCount() {
        val t=ReelTracker()
        fun read(f:ReelFrame,time:Long)=t.observe("youtube",f,"page:0",true,null,0,0,time,"today")
        read(a,1000);assertTrue(read(a,1100))
        read(b,1200);assertFalse(read(b,1300));assertFalse(read(b,2000))
    }

    @Test fun pagerBeforeDelayedCaptionDoesNotPermanentlyConsumeThePage() {
        platforms.forEach{p->
            val t=ReelTracker()
            fun read(f:ReelFrame,page:Int,time:Long)=t.observe(p.id,f,"page:$page",true,page,if(page==0)0 else 1,page*1000L,time,"today")
            read(a,0,1000);assertTrue(p.id,read(a,0,1100))
            assertFalse(p.id,read(a,1,2000));assertFalse(p.id,read(a,1,2100))
            assertFalse(p.id,read(b,1,2700));assertTrue(p.id,read(b,1,2800))
            assertFalse(p.id,read(b,1,3000))
            val c=ReelFrame(true,"c","metadata_ready")
            assertFalse(p.id,read(c,2,4000));assertTrue(p.id,read(c,2,4100))
        }
    }
}
