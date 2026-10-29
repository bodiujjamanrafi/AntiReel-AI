package com.rafi.antireelai

import android.widget.VideoView
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** The player uses remaining space; access actions are outside it and never scroll away. */
@Composable internal fun MisoPage(store:TrackerStore,speaker:MisoSpeaker,accessEnabled:Boolean,running:Boolean,paused:Boolean,modifier:Modifier=Modifier,onAgree:()->Unit,onManage:()->Unit,onToggle:()->Unit,onPlatforms:()->Unit) {
    val language=LocalUiLanguage.current
    val connected=store.consent && accessEnabled && MisoServiceConnection.connected
    var document by rememberSaveable{mutableStateOf<String?>(null)}
    var agreed by rememberSaveable{mutableStateOf(true)}
    var disclosure by rememberSaveable{mutableStateOf(false)}
    var videoSpeaking by remember{mutableStateOf(false)}
    val position=remember{intArrayOf(0)}
    val playIntent=remember{booleanArrayOf(!connected)}
    BackHandler(enabled=document!=null){document=null}
    DisposableEffect(Unit){speaker.stop();onDispose{speaker.stop()}}
    if(document!=null) {
        MisoLegalPage(document!!,{document=null},modifier)
        return
    }
    val actions:@Composable ()->Unit={
        MisoAccessActions(connected,running,store.selected.isEmpty(),agreed,{agreed=it},{document=it},
            {disclosure=true},onManage,onToggle,onPlatforms)
    }
    if(disclosure)AccessibilityDisclosure(onAccept={disclosure=false;store.prefs.edit().putInt("setupTermsVersion",2).apply();onAgree()},onDecline={disclosure=false})
    Column(modifier,verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                UiText(if(connected)"Your Miso" else "Connect Miso",fontSize=27.sp,fontWeight=FontWeight.Bold,color=AppStyle.Text)
                UiText(if(connected)if(paused)"Connected · on a break" else if(running)"Connected · active" else "Connected · paused" else if(accessEnabled)"Waiting for Android to connect" else "Watch the guide, or open Settings now",fontSize=12.sp,color=AppStyle.Muted)
            }
            Mascot(store,store.total(),Modifier.size(64.dp),talking=speaker.speaking || (language!="bn" && videoSpeaking))
        }
        SetupVideo(Modifier.weight(1f).fillMaxWidth(),position,playIntent,{videoSpeaking=it},speaker,!store.prefs.getBoolean("onboardingMuted",false),actions)
        actions()
    }
}

@Composable private fun MisoAccessActions(connected:Boolean,running:Boolean,noPlatforms:Boolean,agreed:Boolean,onAgreement:(Boolean)->Unit,onDocument:(String)->Unit,onAgree:()->Unit,onManage:()->Unit,onToggle:()->Unit,onPlatforms:()->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
            TextButton(onClick={onDocument("Terms of use")},contentPadding=PaddingValues(horizontal=10.dp,vertical=4.dp)) {
                AppGlyph(11,AppStyle.Muted,Modifier.size(17.dp));Spacer(Modifier.width(5.dp));UiText("Terms",fontSize=12.sp,color=AppStyle.Muted)
            }
            TextButton(onClick={onDocument("Services & privacy")},contentPadding=PaddingValues(horizontal=10.dp,vertical=4.dp)) {
                AppGlyph(12,AppStyle.Muted,Modifier.size(17.dp));Spacer(Modifier.width(5.dp));UiText("Services",fontSize=12.sp,color=AppStyle.Muted)
            }
        }
        if(!connected) {
            UiText("Miso reads visible player labels and page changes in selected apps to estimate reels/time and display over them. At your limit it requests media pause, goes Home and shows a warning. Processing stays on-device; screen text is never saved or shared. You can decline or revoke access anytime.",fontSize=12.sp,lineHeight=17.sp,color=AppStyle.Muted)
        }
        if(!connected)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Checkbox(agreed,onCheckedChange=onAgreement,modifier=Modifier.semantics{contentDescription="Agree to terms and privacy"})
            UiText("I agree to the terms and service information",fontSize=12.sp,color=AppStyle.Text,modifier=Modifier.weight(1f))
        }
        if(connected) {
            OutlinedButton(onClick=if(noPlatforms)onPlatforms else onToggle,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(12.dp),border=androidx.compose.foundation.BorderStroke(1.dp,AppStyle.Frame)) {
                AppGlyph(if(noPlatforms)1 else if(running)22 else 14,AppStyle.Teal,Modifier.size(21.dp));Spacer(Modifier.width(10.dp))
                UiText(if(noPlatforms)"Choose your platforms" else if(running)"Pause Miso" else "Start Miso",fontWeight=FontWeight.SemiBold)
            }
        }
        Button(onClick=if(connected)onManage else onAgree,enabled=connected || agreed,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=AppStyle.Teal,contentColor=AppStyle.Background)) {
            AppGlyph(9,AppStyle.Background,Modifier.size(22.dp));Spacer(Modifier.width(10.dp))
            UiText(if(connected)"Accessibility · open Settings" else "Agree & open Settings",fontSize=15.sp,fontWeight=FontWeight.SemiBold)
        }
    }
}

private object SetupPosterCache { @Volatile var image:ImageBitmap?=null }

@Composable private fun SetupVideo(modifier:Modifier,position:IntArray,playIntent:BooleanArray,onSpeaking:(Boolean)->Unit,speaker:MisoSpeaker,voiceAllowed:Boolean,accessActions:@Composable ()->Unit) {
    val language=LocalUiLanguage.current
    var caption by remember{mutableStateOf("")}
    var initialized by remember{mutableStateOf(playIntent[0])}
    val context=LocalContext.current
    val poster by produceState(SetupPosterCache.image,initialized) {
        if(value==null && !initialized)value=withContext(Dispatchers.IO) {
            SetupPosterCache.image ?: android.graphics.BitmapFactory.decodeResource(context.resources,R.drawable.miso_setup_find,android.graphics.BitmapFactory.Options().apply{inSampleSize=2})?.asImageBitmap()?.also{SetupPosterCache.image=it}
        }
    }
    var fullscreen by remember{mutableStateOf(false)}
    var playing by remember{mutableStateOf(false)}
    var failed by remember{mutableStateOf(false)}
    val holder=remember{arrayOfNulls<VideoView>(1)}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(playing,voiceAllowed){onSpeaking(playing && voiceAllowed)}
    LaunchedEffect(playing,language,speaker.ready) {
        if(!playing){if(language=="bn")speaker.stop();return@LaunchedEffect}
        var previous=-1
        while(playing && language=="bn") {
            val view=holder[0]
            if(view!=null && view.duration>0) {
                val step=(view.currentPosition.toLong()*3/view.duration).toInt().coerceIn(0,2)
                if(step!=previous) {
                    previous=step
                    caption=listOf("অ্যাক্সেসিবিলিটিতে অ্যান্টিরিল বেছে নাও।","অনুমতি পড়ে সম্মত হলে অ্যালাও চাপো।","শর্টকাট ঐচ্ছিক। পরিষেবাটি চালু রাখো।")[step]
                    if(speaker.ready && voiceAllowed)speaker.say(caption,"bn")
                }
            }
            kotlinx.coroutines.delay(200)
        }
    }
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver{_,event->
            if(event==Lifecycle.Event.ON_PAUSE){holder[0]?.pause();playing=false}
            if(event==Lifecycle.Event.ON_RESUME && playIntent[0]){holder[0]?.start();playing=true}
        }
        lifecycle.addObserver(observer)
        onDispose{lifecycle.removeObserver(observer);onSpeaking(false)}
    }
    val player:@Composable (Modifier)->Unit={modifier->
        if(!initialized)Box(modifier.background(Color(0xFF10131B)),contentAlignment=Alignment.Center) {
            poster?.let{androidx.compose.foundation.Image(it,contentDescription=localized(language,"Miso setup video preview","মিসো সেটআপ ভিডিওর ছবি"),modifier=Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Fit)}
        } else AndroidView(factory={context->VideoView(context).apply {
            tag="miso_setup_player";holder[0]=this
            setVideoURI(android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.miso_setup}"))
            setOnPreparedListener{
                failed=false;seekTo(position[0]);it.isLooping=false;it.setVolume(if(language=="bn" || !voiceAllowed)0f else 1f,if(language=="bn" || !voiceAllowed)0f else 1f)
                if(playIntent[0] && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)){start();playing=true}
            }
            setOnCompletionListener{playing=false;playIntent[0]=false;position[0]=0}
            setOnErrorListener{_,_,_->failed=true;playing=false;true}
        }},onRelease={if(playIntent[0])position[0]=it.currentPosition;it.stopPlayback();if(holder[0]===it)holder[0]=null},modifier=modifier.semantics{contentDescription="Miso setup video with spoken directions and finger demonstration"})
    }
    fun toggle(){if(!initialized){playIntent[0]=true;initialized=true}else if(playing){holder[0]?.pause();playing=false;playIntent[0]=false}else{holder[0]?.start();playing=true;playIntent[0]=true}}
    fun expand(value:Boolean){position[0]=holder[0]?.currentPosition ?: position[0];holder[0]?.pause();fullscreen=value}
    Column(modifier,verticalArrangement=Arrangement.spacedBy(4.dp)) {
        if(!fullscreen)BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
            val width=minOf(maxWidth,maxHeight*9f/16f)
            player(Modifier.width(width).aspectRatio(9f/16f).background(Color(0xFF10131B)))
        } else Spacer(Modifier.weight(1f))
        if(language=="bn" && caption.isNotEmpty())UiText(caption,fontSize=12.sp,color=AppStyle.Text)
        if(language=="bn" && speaker.unavailable)UiText("বাংলা কণ্ঠের জন্য অ্যান্ড্রয়েডে একটি অফলাইন বাংলা কণ্ঠ ইনস্টল করো। নির্দেশনা পর্দায় দেখা যাবে।",fontSize=11.sp,color=AppStyle.Muted)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick=::toggle){AppGlyph(if(playing)22 else 14,AppStyle.Teal,Modifier.size(20.dp));Spacer(Modifier.width(6.dp));UiText(if(playing)"Pause" else "Play")}
            TextButton(onClick={initialized=true;position[0]=0;holder[0]?.seekTo(0);holder[0]?.start();playIntent[0]=true;playing=true}){UiText("Replay")}
            TextButton(onClick={expand(true)}){UiText("Full screen")}
        }
        if(failed)UiText("The video could not play. You can still open Settings below.",fontSize=12.sp,color=AppStyle.Muted)
    }
    if(fullscreen)Dialog(onDismissRequest={expand(false)},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Column(Modifier.fillMaxSize().background(AppStyle.Background).systemBarsPadding().padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                player(Modifier.width(minOf(maxWidth,maxHeight*9f/16f)).aspectRatio(9f/16f))
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                TextButton(onClick=::toggle){UiText(if(playing)"Pause" else "Play")}
                TextButton(onClick={expand(false)}){UiText("Done")}
            }
            accessActions()
        }
    }
}
