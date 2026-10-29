package com.rafi.antireelai.tracking

/** Transient accessibility evidence only. Never persist node text or identifiers. */
data class ReelNode(
    val id: String = "", val text: String = "", val description: String = "",
    val selected: Boolean = false, val tallPager: Boolean = false, val videoMetadata:Boolean=false
)
data class ReelFrame(val recognized: Boolean, val fingerprint: String?, val reason: String)

object ReelRecognition {
    private val metadataKeys=listOf("caption", "username", "author", "channel_name", "reel_title", "video_title", "desc", "description")
    private val videoKeys=listOf("caption","reel_title","video_title","video_description","reel_description")
    private val authorKeys=listOf("username","author","channel_name")
    private val adLabels=setOf("sponsored","promoted","advertisement","ad","paid partnership","স্পনসরড","বিজ্ঞাপন","প্রচারিত")
    private val adIds=listOf("ad_badge","ad_label","sponsored_label","promoted_label")
    private val feedTabs=setOf("for you","following","trending","আপনার জন্য")
    private val chromeLabels=listOf("back", "more", "pause", "play", "mute", "unmute", "not interested", "report", "menu", "camera", "create", "subscriptions", "library")
    private val engagementCount=Regex("^[\\d.,]+\\s*[kmb]?\\s+(likes?|comments?|shares?|views?|followers?|লাইক|মন্তব্য).*$", RegexOption.IGNORE_CASE)
    private val whitespace=Regex("\\s+")
    private val like = listOf("like", "পছন্দ", "লাইক", "dislike", "love", "点赞")
    private val share = listOf("share", "send", "শেয়ার", "শেয়ার", "পাঠান", "分享")
    private val comment = listOf("comment", "comments", "মন্তব্য", "কমেন্ট", "评论")
    private val controls = like + share + comment + listOf("subscribe", "follow", "অনুসরণ", "সাবস্ক্রাইব", "remix", "save", "সেভ", "sound", "original audio", "use audio")
    private val tabs = setOf("home", "shorts", "reels", "for you", "following", "friends", "spotlight", "discover", "inbox", "profile", "search", "live", "হোম", "রিলস", "আপনার জন্য")
    private val playerIds = mapOf(
        "instagram" to listOf("clips_viewer", "clips_video", "clips_pager", "reel_viewer"),
        "youtube" to listOf("reel_player", "shorts_player", "reel_watch", "reel_recycler", "shorts_container"),
        "facebook" to listOf("reels_viewer", "reel_player", "reels_video", "reels_pager", "reels_fullscreen"),
        "tiktok" to listOf("feed_view_pager", "feed_viewpager", "video_viewpager", "feed_pager"),
        "snapchat" to listOf("spotlight_player", "spotlight_view", "spotlight_pager"),
        "likee" to listOf("video_viewpager", "video_pager", "short_video_player"),
        "kwai" to listOf("photo_view_pager", "video_viewpager", "video_pager", "feed_view_pager"),
        "moj" to listOf("video_view_pager", "video_pager", "reels_player"),
        "josh" to listOf("video_viewpager", "video_pager", "feed_view_pager"),
        "snackvideo" to listOf("photo_view_pager", "video_viewpager", "video_pager", "feed_view_pager")
    )
    private fun matches(label: String, terms: List<String>) = terms.any { term ->
        label == term || label.startsWith("$term ") || label.endsWith(" $term") || label.startsWith("$term,")
    }
    fun recognize(platform: String, nodes: List<ReelNode>): ReelFrame {
        val labels=ArrayList<String>(nodes.size*2)
        for(node in nodes) {
            val text=node.text.trim().lowercase();val description=node.description.trim().lowercase()
            if(text.isNotEmpty())labels.add(text)
            if(description.isNotEmpty())labels.add(description)
        }
        val ids = nodes.map { it.id.lowercase() }
        val selected = nodes.filter { it.selected }.flatMap { listOf(it.text.lowercase().substringBefore(",").trim(), it.description.lowercase().substringBefore(",").trim()) }
        val tall = nodes.any { it.tallPager }
        val social = labels.any { matches(it, like) } && (labels.any { matches(it, share) } || labels.any { matches(it, comment) })
        val strongId = ids.any { id -> playerIds[platform].orEmpty().any { it in id } }
        val tab = when(platform) {
            "instagram", "facebook" -> selected.any { it == "reels" || it == "রিলস" } || labels.any { it.startsWith("reel by ") || it.startsWith("next reel") }
            "youtube" -> selected.any { it == "shorts" } || labels.any { it == "search shorts" || it == "pause short" }
            "snapchat" -> selected.any { it == "spotlight" } || labels.any { it == "spotlight video" }
            else -> selected.any { it in feedTabs } || labels.any { it == "for you" || it == "আপনার জন্য" }
        }
        // Match standalone disclosure labels/resource IDs, not captions mentioning ads.
        val advertisement=labels.any {it in adLabels || it.startsWith("sponsored,") || it.startsWith("paid partnership with ")} || ids.any {id->adIds.any{it in id}}
        if(advertisement) return ReelFrame(false,null,"advertisement")
        val recognized = strongId || (tall && tab && social)
        if(!recognized) return ReelFrame(false, null, "waiting_player")
        fun contentWhere(include:(Int)->Boolean):List<String> {
            val values=ArrayList<String>()
            for(i in nodes.indices)if(include(i)) {
                val node=nodes[i]
                if(isContent(node.text))values.add(node.text)
                if(isContent(node.description))values.add(node.description)
            }
            return values
        }
        val metadata=contentWhere{i->metadataKeys.any{it in ids[i]}}
        val videoMetadata=contentWhere{i->videoKeys.any{it in ids[i]}}
        val selectedContent=when {
            platform=="youtube" && nodes.any{it.videoMetadata}->contentWhere{nodes[it].videoMetadata}
            videoMetadata.isNotEmpty()->videoMetadata
            metadata.isNotEmpty()->contentWhere{i->authorKeys.none{it in ids[i]}}
            else->contentWhere{true}
        }
        val content = selectedContent.map { it.trim().replace(whitespace, " ") }.distinct().sorted()
        return ReelFrame(true, content.takeIf { it.isNotEmpty() }?.joinToString("|")?.hashCode()?.toString(), if(content.isEmpty()) "player_ready" else "metadata_ready")
    }
    internal fun isContent(value: String): Boolean {
        val text=value.trim(); val lower=text.lowercase()
        if(text.length < 2 || text.length > 1500 || lower in tabs || matches(lower, controls)) return false
        if(matches(lower,chromeLabels) || lower.startsWith("next video") || lower.startsWith("previous video")) return false
        if(text.all { it.isDigit() || it in " .,:%/KMkm千万" }) return false
        // Exclude changing playback timestamps and engagement labels, including number-first labels.
        if(engagementCount.matches(text)) return false
        return true
    }
}

/** Metadata and pager updates for one transition merge before committing a count. */
class ReelSession(private val settleMs: Long = 100) {
    private var platform = ""
    private var metadata: String? = null
    private var page: String? = null
    private var version = 0
    private var committed = -1
    private var changedAt = 0L
    private var lastSeen = 0L
    private var transitionAt = Long.MIN_VALUE / 2
    private var awaiting = false
    private var transitionKind = 0
    private var evidence=false
    fun reset() { platform=""; metadata=null; page=null; version=0; committed=-1; awaiting=false; transitionKind=0;evidence=false }
    fun observe(id: String, frame: ReelFrame, pager: String?, compatibility: Boolean, now: Long): Boolean {
        if(platform != id) { reset(); platform=id; changedAt=now; lastSeen=now }
        if(!frame.recognized) {
            evidence=false
            if(now-lastSeen > 900) reset()
            return false
        }
        lastSeen=now
        val hasEvidence=frame.fingerprint != null || compatibility
        evidence=hasEvidence
        if(!hasEvidence) return false
        val nextMetadata=frame.fingerprint
        val metadataChanged=metadata != null && nextMetadata != null && metadata != nextMetadata
        val pageChanged=compatibility && page != null && pager != null && pager != page
        if(metadataChanged || pageChanged) {
            val kind=if(metadataChanged && pageChanged) 3 else if(pageChanged) 2 else 1
            val paired=(kind==1 && transitionKind==2 || kind==2 && transitionKind==1) && now-transitionAt<=450
            if(!paired) {
                version++; changedAt=now; transitionAt=now; awaiting=true; transitionKind=kind
            } else {
                transitionKind=3
                if(awaiting) changedAt=now
            }
        }
        if(nextMetadata != null) metadata=nextMetadata
        if(pager != null) page=pager
        if(committed != version && now-changedAt >= settleMs) {
            committed=version; awaiting=false; return true
        }
        return false
    }
    val pending: Boolean get() = platform.isNotEmpty() && evidence && committed != version
}
