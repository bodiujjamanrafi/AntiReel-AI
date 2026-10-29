package com.rafi.antireelai

import com.rafi.antireelai.tracking.*
import org.junit.Assert.*
import org.junit.Test

/** Synthetic adapter fixtures: these verify the rules, not compatibility with installed app versions. */
class ReelRecognitionTest {
    private fun player(tab:String)=listOf(ReelNode(text=tab,selected=true),ReelNode(tallPager=true),ReelNode(description="Like 2.3K"),ReelNode(description="Share"),ReelNode(text="@rafi"),ReelNode(text="A short clip"))
    @Test fun allTenPlatformsRecognizeSemanticPlayers() {
        mapOf("instagram" to "Reels","facebook" to "Reels","youtube" to "Shorts","tiktok" to "For You","snapchat" to "Spotlight","likee" to "For You","snackvideo" to "For You","kwai" to "For You","moj" to "For You","josh" to "For You").forEach { (id,tab) ->
            val result=ReelRecognition.recognize(id,player(tab));assertTrue(id,result.recognized);assertNotNull(id,result.fingerprint)
        }
    }
    @Test fun knownNativePlayersDoNotRequireEnglishLabels() {
        mapOf("instagram" to "clips_viewer","youtube" to "reel_player","facebook" to "reels_video","tiktok" to "feed_view_pager","snapchat" to "spotlight_player","likee" to "video_pager","snackvideo" to "photo_view_pager","kwai" to "photo_view_pager","moj" to "video_view_pager","josh" to "feed_view_pager").forEach { (id,resource) ->
            assertTrue(id,ReelRecognition.recognize(id,listOf(ReelNode(id=resource),ReelNode(text="ছোট ভিডিও"))).recognized)
        }
    }
    @Test fun normalFeedWithReelsNavigationDoesNotCount() {
        val nodes=listOf(ReelNode(text="Reels"),ReelNode(tallPager=true),ReelNode(text="Like"),ReelNode(text="Share"),ReelNode(text="A regular post"))
        assertFalse(ReelRecognition.recognize("facebook",nodes).recognized)
    }
    @Test fun commentsAndShortVideoTilesDoNotCount() {
        val nodes=player("Shorts").map { it.copy(tallPager=false) }
        assertFalse(ReelRecognition.recognize("youtube",nodes).recognized)
    }
    @Test fun selectedTabDescriptionsCanIncludeAccessibilityHints() {
        val nodes=player("Shorts").map { if(it.selected) it.copy(text="",description="Shorts, tab 2 of 5") else it }
        assertTrue(ReelRecognition.recognize("youtube",nodes).recognized)
    }
    @Test fun bengaliPlayerControlsAndShortCaptionsWork() {
        val nodes=listOf(ReelNode(text="রিলস",selected=true),ReelNode(tallPager=true),ReelNode(text="লাইক করুন"),ReelNode(text="শেয়ার করুন"),ReelNode(text="সুন্দর দিন"))
        val result=ReelRecognition.recognize("facebook",nodes);assertTrue(result.recognized);assertNotNull(result.fingerprint)
    }
    @Test fun changingLikeCountsDoesNotChangeIdentity() {
        val a=player("For You")
        val b=a.map { if(it.description.startsWith("Like")) it.copy(description="Like 9.9K") else it }
        assertEquals(ReelRecognition.recognize("tiktok",a).fingerprint,ReelRecognition.recognize("tiktok",b).fingerprint)
    }
    @Test fun controlsAloneProduceNoInventedCaption() {
        val a=listOf(ReelNode(id="reel_player"),ReelNode(text="Like"),ReelNode(text="Share"),ReelNode(text="1.2K"),ReelNode(text="123 views"))
        val frame=ReelRecognition.recognize("youtube",a);assertTrue(frame.recognized);assertNull(frame.fingerprint)
    }
    @Test fun shortCaptionChangesAreDetectedWithoutResourceIds() {
        assertNotEquals(ReelRecognition.recognize("tiktok",player("For You")).fingerprint,ReelRecognition.recognize("tiktok",player("For You").map { if(it.text=="A short clip") it.copy(text="Hello") else it }).fingerprint)
    }

    @Test fun newPlatformNormalFeedsAreNotPlayers() {
        listOf("kwai","moj","josh").forEach { id ->
            assertFalse(id,ReelRecognition.recognize(id,listOf(ReelNode(text="For You"),ReelNode(text="Like"),ReelNode(text="Share"))).recognized)
        }
    }

    @Test fun disclosureLabelsExcludeAdsAcrossAllPlatforms() {
        platforms.forEach {p->
            val ad=listOf(ReelNode(id=when(p.id){"instagram"->"clips_viewer";"youtube"->"reel_player";else->"video_pager"}),ReelNode(text="Sponsored"))
            assertFalse(p.id,ReelRecognition.recognize(p.id,ad).recognized)
        }
    }
    @Test fun adResourceBadgeExcludesHiddenDisclosureText() {
        assertEquals("advertisement",ReelRecognition.recognize("youtube",listOf(ReelNode(id="reel_player"),ReelNode(id="ad_badge"))).reason)
    }
    @Test fun CaptionMentioningAdsIsStillOrganic() {
        assertTrue(ReelRecognition.recognize("youtube",listOf(ReelNode(id="reel_player"),ReelNode(text="Why ads are so common"))).recognized)
    }

    @Test fun sameYoutubeChannelDoesNotHideDistinctVideoTitles() {
        val common=listOf(ReelNode(id="reel_player"),ReelNode(id="reel_channel_name",text="Same creator"))
        assertNull(ReelRecognition.recognize("youtube",common).fingerprint)
        assertNotEquals(ReelRecognition.recognize("youtube",common+ReelNode(id="reel_title",text="First short")).fingerprint,ReelRecognition.recognize("youtube",common+ReelNode(id="reel_title",text="Second short")).fingerprint)
    }

    @Test fun youtubeCaptionIdentityDoesNotIncludeChangingControlsAndEngagement() {
        val clip=listOf(ReelNode(id="reel_player"),ReelNode(description="A short caption",videoMetadata=true),ReelNode(description="Creator name",videoMetadata=true))
        val a=ReelRecognition.recognize("youtube",clip+ReelNode(description="Pause video")+ReelNode(description="Comments, 20"))
        val b=ReelRecognition.recognize("youtube",clip+ReelNode(description="Play video")+ReelNode(description="Comments, 21"))
        assertEquals(a.fingerprint,b.fingerprint)
        assertNotEquals(a.fingerprint,ReelRecognition.recognize("youtube",clip.map{if(it.description=="A short caption")it.copy(description="Another short caption")else it}).fingerprint)
    }
}
