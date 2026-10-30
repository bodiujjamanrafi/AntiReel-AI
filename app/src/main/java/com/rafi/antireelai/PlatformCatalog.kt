package com.rafi.antireelai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Base64

enum class PlatformKind { SOCIAL, VIDEO }
data class Platform(
    val id:String,val name:String,val subtitle:String,val symbol:String,val packages:Set<String>,
    val kind:PlatformKind=PlatformKind.VIDEO,val detectorId:String?=id,
    val isCustom:Boolean=false,val available:Boolean=true,val experimental:Boolean=false
) {
    val trackingLabel:String get()=when {detectorId==null->"Unsupported tracking";experimental->"Experimental tracking";else->"Built-in detector"}
}

/** Historical order stays stable. Archived YouTube is never offered as a built-in choice. */
internal val historicalPlatformOrder=listOf(
    Platform("instagram","Instagram","Reels","◎",setOf("com.instagram.android","com.instagram.lite"),PlatformKind.SOCIAL),
    Platform("youtube","YouTube","Experimental Shorts tracking","▶",setOf(CustomPlatforms.YOUTUBE),isCustom=true,available=false,experimental=true),
    Platform("tiktok","TikTok","For You","♪",setOf("com.zhiliaoapp.musically","com.ss.android.ugc.trill","com.zhiliaoapp.musically.go")),
    Platform("facebook","Facebook","Reels","f",setOf("com.facebook.katana"),PlatformKind.SOCIAL),
    Platform("snapchat","Snapchat","Spotlight","S",setOf("com.snapchat.android"),PlatformKind.SOCIAL),
    Platform("likee","Likee","Short videos","♥",setOf("video.like")),
    Platform("snackvideo","SnackVideo","Short videos","▷",setOf("com.kwai.bulldog")),
    Platform("kwai","Kwai","Short videos","K",setOf("com.kwai.video")),
    Platform("moj","Moj","Reels","M",setOf("in.mohalla.video")),
    Platform("josh","Josh","Short videos","J",setOf("com.eterno.shortvideos","com.eterno.shortvideos.lite")),
    Platform("facebook_lite","Facebook Lite","Reels","f",setOf("com.facebook.lite"),PlatformKind.SOCIAL,detectorId=null,available=false)
)

internal data class CustomPlatformRecord(val packageName:String,val name:String,val visible:Boolean)
internal object CustomPlatforms {
    const val KEY="customPlatforms.v1"
    const val YOUTUBE="com.google.android.youtube"
    const val REMOVED_FACEBOOK_LITE="com.facebook.lite"
    private val packagePattern=Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+")
    fun validPackage(value:String)=value.length<=255 && packagePattern.matches(value)
    fun id(packageName:String)=if(packageName==YOUTUBE)"youtube" else "custom.$packageName"
    fun encode(records:List<CustomPlatformRecord>)=records.distinctBy{it.packageName}.joinToString("\n") {
        "${it.packageName}|${Base64.getUrlEncoder().withoutPadding().encodeToString(it.name.toByteArray(Charsets.UTF_8))}|${if(it.visible)1 else 0}"
    }
    fun decode(value:String):List<CustomPlatformRecord> = value.lineSequence().mapNotNull {line->
        val parts=line.split('|')
        if(parts.size!=3 || !validPackage(parts[0]) || parts[2] !in listOf("0","1"))null
        else runCatching{CustomPlatformRecord(parts[0],String(Base64.getUrlDecoder().decode(parts[1]),Charsets.UTF_8).take(80),parts[2]=="1")}.getOrNull()
    }.distinctBy{it.packageName}.toList()
    fun catalog(records:List<CustomPlatformRecord>):List<Platform> {
        val youtube=records.find{it.packageName==YOUTUBE}
        val base=historicalPlatformOrder.map{if(it.id=="youtube")it.copy(available=youtube?.visible==true) else it}
        val reserved=base.flatMap{it.packages}.toSet()
        return base+records.filter{it.packageName !in reserved}.map {
            Platform(id(it.packageName),it.name,"No validated short-video detector","",setOf(it.packageName),
                detectorId=null,isCustom=true,available=it.visible)
        }
    }
}

/** Published only on connection/setup or a catalog edit, never from a per-frame node scan. */
internal object PlatformCatalog {
    var entries by mutableStateOf(CustomPlatforms.catalog(emptyList()))
        private set
    fun refresh(store:TrackerStore) {if(entries!=store.catalog)entries=store.catalog}
}
val platforms:List<Platform> get()=PlatformCatalog.entries
