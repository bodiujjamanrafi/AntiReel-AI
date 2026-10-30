package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class FacebookLiteDetectionTest {
    @Test fun removedFacebookLiteDetectorNeverRecognizesPlayers() {
        val nodes=listOf(ReelNode(id="com.facebook.lite:id/reels_viewer",tallPager=true),ReelNode(text="Reels"))
        assertFalse(PackageDetectors.recognize("facebook_lite",nodes).recognized)
    }
    @Test fun idleOrPausedTrackingDoesNotScheduleHeartbeatWork() {
        assertTrue(trackingHeartbeatRequired(true,true,true,false))
        assertFalse(trackingHeartbeatRequired(false,true,true,false))
        assertFalse(trackingHeartbeatRequired(true,false,true,false))
        assertFalse(trackingHeartbeatRequired(true,true,false,false))
        assertFalse(trackingHeartbeatRequired(true,true,true,true))
    }
}
