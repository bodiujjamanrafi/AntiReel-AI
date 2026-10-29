package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

class YouTubeShortsRecognitionTest {
    private val player=ReelNode(id="reel_player")
    private fun recognize(vararg nodes:ReelNode)=YouTubeShortsRecognition.recognize(listOf(player)+nodes)
    @Test fun recycledViewIdsWithDifferentTitlesIdentifyDifferentShorts() {
        assertNotEquals(recognize(ReelNode(id="reel_title",text="First video")).fingerprint,
            recognize(ReelNode(id="reel_title",text="Next video")).fingerprint)
    }
    @Test fun resourceFreeCaptionWorksWithoutDependingOnEngagementControls() {
        val caption=ReelNode(description="A real caption",videoMetadata=true)
        assertEquals(recognize(caption,ReelNode(description="Comments, 5")).fingerprint,
            recognize(caption,ReelNode(description="Comments, 19"),ReelNode(description="Pause video")).fingerprint)
    }
    @Test fun resourceCaptionTakesPriorityOverChangingGeometricCandidates() {
        val title=ReelNode(id="reel_title",text="Stable video")
        assertEquals(recognize(title,ReelNode(text="Changing text",videoMetadata=true)).fingerprint,
            recognize(title,ReelNode(text="Other text",videoMetadata=true)).fingerprint)
    }
    @Test fun actualExposedVideoUrlTakesPriorityOverEditedCaption() {
        val url=ReelNode(description="https://www.youtube.com/shorts/AbCdEf123_-?feature=share")
        assertEquals(recognize(url,ReelNode(id="reel_title",text="Original")).fingerprint,
            recognize(url,ReelNode(id="reel_title",text="Edited")).fingerprint)
    }
    @Test fun twoExposedVideoUrlsDoNotSelectAPreloadedVideoArbitrarily() {
        assertNull(recognize(ReelNode(text="https://youtube.com/shorts/AbCdEf123_-"),
            ReelNode(text="https://youtube.com/shorts/ZbCdEf123_-")).fingerprint)
    }
    @Test fun channelAndPlaybackControlsAloneCannotIdentifyAVideo() {
        assertNull(recognize(ReelNode(id="reel_channel_name",text="One creator"),ReelNode(description="Pause video")).fingerprint)
    }
    @Test fun advertisementIsRejectedEvenWithVideoMetadata() {
        assertFalse(recognize(ReelNode(id="reel_title",text="Advert"),ReelNode(text="Sponsored")).recognized)
    }
    @Test fun feedAndSearchTilesAreNotShortsPlayers() {
        assertFalse(YouTubeShortsRecognition.recognize(listOf(ReelNode(text="Shorts",selected=true),
            ReelNode(id="video_title",text="Search result"))).recognized)
    }
}
