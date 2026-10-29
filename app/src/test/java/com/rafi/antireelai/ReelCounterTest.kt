package com.rafi.antireelai

import com.rafi.antireelai.tracking.ReelCounter
import org.junit.Assert.*
import org.junit.Test

class ReelCounterTest {
    @Test fun repeatedEventsCountOnceAfterDwell() {
        val c = ReelCounter(1000, 0)
        assertFalse(c.observe("instagram:a", 0))
        assertFalse(c.observe("instagram:a", 999))
        assertTrue(c.observe("instagram:a", 1000))
        assertFalse(c.observe("instagram:a", 2500))
    }
    @Test fun fastSwipesAreNotWatchedViews() {
        val c = ReelCounter(1000, 0)
        c.observe("a", 0)
        assertFalse(c.observe("b", 300))
        assertFalse(c.observe("c", 600))
        assertTrue(c.observe("c", 1600))
    }
    @Test fun nextReelAndDifferentPlatformCountIndependently() {
        val c = ReelCounter(1000, 0)
        c.observe("instagram:a", 0)
        assertTrue(c.observe("instagram:a", 1000))
        assertFalse(c.observe("youtube:a", 1100))
        assertTrue(c.observe("youtube:a", 2100))
    }
    @Test fun unsupportedScreenCancelsPendingView() {
        val c = ReelCounter(1000, 0)
        c.observe("a", 0)
        c.observe(null, 500)
        assertFalse(c.observe("a", 1500))
        assertTrue(c.observe("a", 2500))
    }
    @Test fun resetCancelsDwell() {
        val c = ReelCounter(1000, 0)
        c.observe("a", 0)
        c.reset()
        assertFalse(c.observe("a", 2000))
    }

    @Test fun fastModeCountsWithin120Milliseconds() {
        val c=ReelCounter()
        assertFalse(c.observe("tiktok:a",0))
        assertFalse(c.observe("tiktok:a",119))
        assertTrue(c.observe("tiktok:a",120))
        assertFalse(c.observe("tiktok:a",200))
        assertFalse(c.observe("tiktok:b",250))
        assertTrue(c.observe("tiktok:b",370))
    }
    @Test fun temporaryMissingLabelsDoNotCountSameReelAgain() {
        val c=ReelCounter()
        c.observe("facebook:a",0)
        assertTrue(c.observe("facebook:a",120))
        assertFalse(c.observe(null,200))
        assertFalse(c.observe("facebook:a",350))
        assertFalse(c.observe("facebook:a",600))
    }
    @Test fun leavingPlayerAllowsLaterRewatch() {
        val c=ReelCounter()
        c.observe("youtube:a",0)
        assertTrue(c.observe("youtube:a",120))
        c.observe(null,900)
        assertFalse(c.observe("youtube:a",1000))
        assertTrue(c.observe("youtube:a",1120))
    }
}
