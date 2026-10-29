package com.rafi.antireelai.tracking

import java.security.MessageDigest

/** Prefer exposed video URLs, then title metadata, then visible caption-region text.
 * Node uniqueId/resource IDs identify recycled views, not YouTube videos.
 */
internal object YouTubeShortsRecognition {
    private val videoUrl=Regex("(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/shorts/|youtu\\.be/)([A-Za-z0-9_-]{11})(?:[^A-Za-z0-9_-]|$)")
    private val titleIds=listOf("reel_title","video_title","shorts_title","reel_description","video_description")
    private val authorIds=listOf("channel_name","author","username","channel_bar")
    private val whitespace=Regex("\\s+")
    fun recognize(nodes:List<ReelNode>):ReelFrame {
        val base=ReelRecognition.recognize("youtube",nodes)
        if(!base.recognized)return base
        val urls=nodes.flatMap{listOf(it.text,it.description)}.mapNotNull{videoUrl.find(it)?.groupValues?.get(1)}.distinct()
        // Multiple URLs can describe offscreen/preloaded content; do not pick one arbitrarily.
        val exposedId=urls.singleOrNull()
        if(exposedId!=null)return ReelFrame(true,digest("video:$exposedId"),"youtube_video_id")
        val titles=nodes.filter{n->titleIds.any{it in n.id.lowercase()}}
        val region=nodes.filter{it.videoMetadata && authorIds.none{key->key in it.id.lowercase()}}
        val content=(if(titles.isNotEmpty())titles else region).flatMap{listOf(it.text,it.description)}
            .filter{ReelRecognition.isContent(it)}.map{it.trim().replace(whitespace," ")}.distinct().sorted()
        if(content.isEmpty())return ReelFrame(true,null,"youtube_missing_identity")
        val authors=nodes.filter{n->authorIds.any{it in n.id.lowercase()}}.flatMap{listOf(it.text,it.description)}
            .filter{ReelRecognition.isContent(it)}.map{it.trim().replace(whitespace," ")}.distinct().sorted()
        return ReelFrame(true,digest((content+authors).joinToString("\u0000")),"youtube_metadata")
    }
    private fun digest(value:String):String {
        val bytes=MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        val hex="0123456789abcdef"
        return buildString(64){for(byte in bytes){val n=byte.toInt() and 255;append(hex[n ushr 4]);append(hex[n and 15])}}
    }
}
