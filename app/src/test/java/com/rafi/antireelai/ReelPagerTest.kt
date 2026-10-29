package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class ReelPagerTest {
    @Test fun recycledZeroIndicesAndConstantScrollOffsetStillAdvanceShorts() {
        val p=ReelPager()
        p.observe(0,0,0,0,1000,1000)
        assertNull(p.position)
        p.observe(0,0,0,650,1000,1200)
        assertEquals(1,p.position);assertEquals(1,p.direction)
        p.observe(0,0,0,650,1000,1500)
        assertEquals(2,p.position)
    }
    @Test fun multiItemRecyclerUsesPixelsInsteadOfRequiringSingleItemIndices() {
        val p=ReelPager();p.observe(0,2,0,0,1000,1000)
        p.observe(0,2,0,300,1000,1200);assertNull(p.position)
        p.observe(0,2,0,300,1000,1250);assertEquals(1,p.position)
    }
    @Test fun actualPageIndicesWorkWithoutPixelDeltas() {
        val p=ReelPager();p.observe(10,10,-1,0,1000,1000)
        p.observe(11,11,-1,0,1000,1200);assertEquals(1,p.position)
        p.observe(10,10,-1,0,1000,1500);assertEquals(0,p.position);assertEquals(-1,p.direction)
    }
    @Test fun partialScrollDoesNotCountAndOldPartialScrollExpires() {
        val p=ReelPager();p.observe(0,0,0,300,1000,1000)
        p.observe(0,0,0,300,1000,2000);assertNull(p.position)
    }
    @Test fun forwardBackwardForwardRevisitDoesNotCountTwice() {
        val p=ReelPager();val g=NovelReelGate();val f=ReelFrame(true,null,"player_ready")
        p.observe(0,0,0,650,1000,1000)
        assertTrue(g.allow("youtube",f,p.position,p.direction,p.changedAt,"today"))
        p.observe(0,0,0,-650,1000,1300)
        assertFalse(g.allow("youtube",f,p.position,p.direction,p.changedAt,"today"))
        p.observe(0,0,0,650,1000,1600)
        assertFalse(g.allow("youtube",f,p.position,p.direction,p.changedAt,"today"))
        p.observe(0,0,0,650,1000,1900)
        assertTrue(g.allow("youtube",f,p.position,p.direction,p.changedAt,"today"))
    }
    @Test fun unknownInitialPositionDoesNotFreezeLaterForwardPages() {
        val g=NovelReelGate()
        assertTrue(g.allow("youtube",ReelFrame(true,"a","metadata_ready"),null,0,0,"today"))
        assertFalse(g.allow("youtube",ReelFrame(true,"edited","metadata_ready"),null,0,0,"today"))
        assertTrue(g.allow("youtube",ReelFrame(true,"b","metadata_ready"),1,1,1000,"today"))
        assertFalse(g.allow("youtube",ReelFrame(true,"c","metadata_ready"),1,1,1000,"today"))
    }
    @Test fun resetDropsPositionsAndPartialScroll() {
        val p=ReelPager();p.observe(0,0,0,650,1000,1000);p.reset()
        assertNull(p.position);assertNull(p.token);assertEquals(0,p.direction)
        p.observe(0,0,0,300,1000,2000);assertNull(p.position)
    }

    @Test fun explicitCurrentIndexWorksWhenRangesAndPixelsAreUnavailable() {
        val p=ReelPager()
        p.observe(-1,-1,-1,0,1000,1000,currentIndex=20)
        p.observe(-1,-1,-1,0,1000,1200,currentIndex=21)
        assertEquals(1,p.position);assertEquals(1,p.direction)
        p.observe(-1,-1,-1,0,1000,1500,currentIndex=20)
        assertEquals(0,p.position);assertEquals(-1,p.direction)
    }
}
