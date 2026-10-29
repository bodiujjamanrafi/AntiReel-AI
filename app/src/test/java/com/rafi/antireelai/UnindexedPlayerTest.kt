package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class UnindexedPlayerTest {
    @Test fun realYoutubeZeroMetricEventAllowsNewIdentitiesButNotReplaysOrEdits() {
        platforms.forEach{platform->
            val pager=ReelPager();val tracker=ReelTracker()
            fun read(identity:String?,time:Long,recognized:Boolean=true)=tracker.observe(platform.id,ReelFrame(recognized,identity,"fixture"),pager.token,true,pager.position,pager.direction,pager.changedAt,time,"today",pager.unindexedScroll)
            fun scroll(time:Long)=pager.observe(-1,-1,0,0,2761,time,-1,true)
            scroll(1000);assertFalse(read("a",1000));assertTrue(platform.id,read("a",1100))
            // Changing labels alone never invent another scroll.
            assertFalse(read("a-edit",1300));assertFalse(read("a-edit",1400))
            scroll(2000);assertFalse(read("a",2000));assertFalse(read("a",2100))
            assertFalse(read("b",2700));assertTrue(platform.id,read("b",2800))
            scroll(3000);assertFalse(read(null,3000,false));assertFalse(read(null,3100,false))
            scroll(4000);assertFalse(read("c",4000));assertTrue(platform.id,read("c",4100))
            scroll(5000);assertFalse(read("a",5000));assertFalse(read("a",5100))
        }
    }
    @Test fun noDirectionAndNoIdentityNeverGuessAReel() {
        val g=NovelReelGate()
        assertFalse(g.allow("youtube",ReelFrame(true,null,"player_ready"),null,0,1000,"today",true))
    }
    @Test fun unindexedFallbackCannotOverrideKnownBackwardDirection() {
        val g=NovelReelGate()
        assertFalse(g.allow("youtube",ReelFrame(true,"a","metadata_ready"),null,-1,1000,"today",true))
    }
    @Test fun oldScrollNotificationsCannotAuthorizeLaterMetadataChanges() {
        val t=ReelTracker();val a=ReelFrame(true,"a","metadata_ready")
        assertFalse(t.observe("youtube",a,"unknown",true,null,0,1000,3000,"today",true))
        // First visible video can count once without movement evidence.
        assertTrue(t.observe("youtube",a,"unknown",true,null,0,1000,3100,"today",true))
        val b=ReelFrame(true,"b","metadata_ready")
        assertFalse(t.observe("youtube",b,"unknown",true,null,0,1000,4000,"today",true))
        assertFalse(t.observe("youtube",b,"unknown",true,null,0,1000,4100,"today",true))
    }
    @Test fun ordinaryZeroMetricNotificationsDoNotCreatePlayerScrollTokens() {
        val p=ReelPager();p.observe(-1,-1,0,0,2761,1000)
        assertFalse(p.unindexedScroll);assertNull(p.token)
    }

    @Test fun shortForwardFlingCountsNewVideoWithoutRequiringFullPlayerDistance() {
        val p=ReelPager();val t=ReelTracker()
        fun read(s:String,now:Long)=t.observe("youtube",ReelFrame(true,s,"fixture"),p.token,true,p.position,p.direction,p.changedAt,now,"today",p.unindexedScroll)
        read("a",1000);assertTrue(read("a",1100))
        p.observe(-1,-1,0,420,2761,2000,-1,true)
        assertNull(p.position);assertEquals(1,p.direction)
        assertFalse(read("b",2000));assertTrue(read("b",2100))
        p.observe(-1,-1,0,-420,2761,3000,-1,true)
        assertFalse(read("a",3000));assertFalse(read("a",3100))
        p.observe(-1,-1,0,420,2761,4000,-1,true)
        assertFalse(read("b",4000));assertFalse(read("b",4100))
        p.observe(-1,-1,0,420,2761,5000,-1,true)
        assertFalse(read("c",5000));assertTrue(read("c",5100))
    }

    @Test fun unsetHorizontalDeltaCannotDiscardVerticalOrOpaqueYoutubeEvents() {
        assertFalse(isHorizontalPlayerScroll(-1,0))
        assertFalse(isHorizontalPlayerScroll(-1,-1))
        assertFalse(isHorizontalPlayerScroll(-1,450))
        assertTrue(isHorizontalPlayerScroll(450,-1))
        assertTrue(isHorizontalPlayerScroll(-450,0))
    }
    @Test fun unsetVerticalDeltaIsNotBackwardMovement() {
        val p=ReelPager();val t=ReelTracker()
        fun read(s:String,now:Long)=t.observe("youtube",ReelFrame(true,s,"fixture"),p.token,true,p.position,p.direction,p.changedAt,now,"today",p.unindexedScroll)
        p.observe(-1,-1,0,-1,2761,1000,-1,true)
        assertEquals(0,p.direction);assertTrue(p.unindexedScroll)
        read("a",1000);assertTrue(read("a",1100))
        p.observe(-1,-1,0,-1,2761,2000,-1,true)
        assertFalse(read("b",2000));assertTrue(read("b",2100))
    }
}
