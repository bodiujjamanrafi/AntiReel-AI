package com.rafi.antireelai

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Request a pause without toggling playback or changing the user's global media volume. */
internal fun pauseCurrentMedia(context:Context) {
    val audio=context.getSystemService(AudioManager::class.java)
    runCatching {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PAUSE))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PAUSE))
    }
}
/** A real foreground screen replaces the touch-through veil at the exact daily boundary. */
class LimitReachedActivity:ComponentActivity() {
    private lateinit var audio:AudioManager
    private lateinit var focus:AudioFocusRequest
    private var focusHeld=false
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState);enableEdgeToEdge()
        audio=getSystemService(AudioManager::class.java)
        focus=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setOnAudioFocusChangeListener{change->if(change==AudioManager.AUDIOFOCUS_LOSS)focusHeld=false}.build()
        setContent {
            val store=remember{TrackerStore(this)}
            val speaker=remember{MisoSpeaker(this)}
            LaunchedEffect(Unit){
                val next=java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                kotlinx.coroutines.delay((next-System.currentTimeMillis()).coerceAtLeast(1L));finish()
            }
            LaunchedEffect(speaker.ready){if(speaker.ready)speaker.stopAtLimit()}
            DisposableEffect(Unit){onDispose{speaker.close()}}
            BackHandler{leave()}
            CompositionLocalProvider(LocalUiLanguage provides (store.prefs.getString("language","en") ?: "en")) {
            MaterialTheme(colorScheme=darkColorScheme(primary=Color.White,onPrimary=Color(0xFF65142E))) {
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF8A1739),Color(0xFF360D20)))).safeDrawingPadding().padding(26.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(20.dp)) {
                        Spacer(Modifier.height(18.dp))
                        UiText("DAILY LIMIT REACHED",fontSize=12.sp,letterSpacing=1.5.sp,color=Color(0xFFFFD7E3),fontWeight=FontWeight.SemiBold)
                        AndroidView(factory={CompanionView(it)},update={it.progress=1f;it.activity=CompanionActivity.ACTIVE;it.animated=!store.reducedMotion;it.talking=speaker.speaking;it.contentDescription="Angry Miso: your daily reel limit is reached"},modifier=Modifier.size(220.dp))
                        UiText("That's enough\nscrolling for today.",fontSize=31.sp,lineHeight=37.sp,fontWeight=FontWeight.Bold,color=Color.White,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                        UiText(if(store.dailyMinutes>0) "${durationLabel(store.totalUsage())} / ${store.dailyMinutes} min · ${store.total()} / ${store.goal} reels" else "${store.total()} / ${store.goal} reels",fontSize=24.sp,color=Color.White,fontWeight=FontWeight.SemiBold)
                        UiText("Leave this feed and give yourself a break. Counting is stopped. Your daily allowance resets at midnight.",fontSize=16.sp,lineHeight=24.sp,color=Color(0xFFFFE6ED),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    Button(onClick={leave()},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=Color(0xFF65142E))){UiText("Leave the app",fontWeight=FontWeight.SemiBold)}
                    TextButton(onClick={startActivity(Intent(this@LimitReachedActivity,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){UiText("Open AntiReel",color=Color.White)}
                }
            }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val store=TrackerStore(this)
        if(!store.running || !store.allowanceReached()){finish();return}
        pauseCurrentMedia(this)
        // This Activity is visible: Android 15+ permits its audio-focus request here.
        focusHeld=runCatching{audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED}.getOrDefault(false)
    }
    override fun onWindowFocusChanged(hasFocus:Boolean){
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus && ::focus.isInitialized && !focusHeld)focusHeld=runCatching{audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED}.getOrDefault(false)
    }
    override fun onPause(){if(focusHeld){audio.abandonAudioFocusRequest(focus);focusHeld=false};super.onPause()}
    private fun leave(){pauseCurrentMedia(this);startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));finish()}
}
