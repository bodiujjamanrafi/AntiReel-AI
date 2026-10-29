package com.rafi.antireelai

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rafi.antireelai.tracking.LimitStage

internal fun warningMessage(stage:LimitStage):String = when(stage) {
    LimitStage.NEAR->"Hey, you're getting close to your daily reel limit. Please take a little break. Your time matters."
    LimitStage.REACHED->"You've reached your daily reel limit. Let's stop here and give your attention a breather."
    LimitStage.EXCEEDED->"Please stop scrolling now. You've crossed your daily reel limit. Put your phone down and take a break. I'm looking out for you."
    LimitStage.NONE->""
}
/** Speech stays on device. A warning is acknowledged only after playback completes. */
internal class MisoSpeaker(context:Context,private val onSpeakingChanged:(Boolean)->Unit={},private val onWarningFinished:(String)->Unit={}):TextToSpeech.OnInitListener {
    private val preferences=context.applicationContext.getSharedPreferences("antireel",Context.MODE_PRIVATE)
    var ready by mutableStateOf(false);private set
    var unavailable by mutableStateOf(false);private set
    var speaking by mutableStateOf(false);private set
    private val main=Handler(Looper.getMainLooper())
    private var closed=false
    private var activeUtterance:String?=null
    val pendingWarning:String? get()=activeUtterance?.takeIf{it.startsWith("miso-limit:")}?.removePrefix("miso-limit:")
    private var engine:TextToSpeech?=TextToSpeech(context.applicationContext,this)
    override fun onInit(status:Int) {
        main.post {
            if(closed)return@post
            val tts=engine ?:return@post
            if(status!=TextToSpeech.SUCCESS){unavailable=true;return@post}
            val preferred=preferences.getString("language","en") ?: "en"
            val voice=runCatching{tts.voices?.filter{!it.isNetworkConnectionRequired && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)}?.sortedBy{if(it.locale.language==preferred)0 else if(it.locale.language=="en")1 else 2}?.firstOrNull()}.getOrNull()
            if(voice==null || tts.setVoice(voice)!=TextToSpeech.SUCCESS){unavailable=true;return@post}
            tts.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            tts.setOnUtteranceProgressListener(object:UtteranceProgressListener() {
                override fun onStart(id:String?){main.post{if(!closed && id==activeUtterance)updateSpeaking(true)}}
                override fun onDone(id:String?){main.post{if(!closed && id==activeUtterance){val warning=pendingWarning;activeUtterance=null;updateSpeaking(false);if(warning!=null)onWarningFinished(warning)}}}
                override fun onStop(id:String?,interrupted:Boolean){main.post{if(id==activeUtterance){activeUtterance=null;updateSpeaking(false)}}}
                @Deprecated("Legacy engine callback") override fun onError(id:String?){main.post{if(id==activeUtterance){activeUtterance=null;updateSpeaking(false);unavailable=true}}}
            })
            ready=true
        }
    }
    private fun updateSpeaking(value:Boolean) {
        if(speaking==value)return
        speaking=value
        onSpeakingChanged(value)
    }
    fun say(message:String,language:String,id:String="miso-dialogue-${System.nanoTime()}"):Boolean {
        val tts=engine ?:return false
        val voice=tts.voices?.firstOrNull{it.locale.language==language && !it.isNetworkConnectionRequired && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)}
        if(voice==null){unavailable=true;return false}
        if(tts.setVoice(voice)!=TextToSpeech.SUCCESS){unavailable=true;return false}
        unavailable=false
        return speak(message,id,false)
    }
    private fun speak(message:String,id:String,firm:Boolean):Boolean {
        if(!ready || closed || message.isEmpty())return false
        engine?.setPitch(if(firm)1.02f else 1.15f);engine?.setSpeechRate(if(firm).96f else .92f)
        updateSpeaking(false)
        activeUtterance=id
        val queued=engine?.speak(message,TextToSpeech.QUEUE_FLUSH,null,id)==TextToSpeech.SUCCESS
        if(!queued)activeUtterance=null
        return queued
    }
    fun guide(step:Int)=speak(listOf("Open your phone's accessibility settings. Find Downloaded apps or Installed services, then choose AntiReel.","Turn on the switch called Use AntiReel. Android will ask you to confirm this access.","Read the permission notice and tap Allow if you agree. Miso starts automatically. You can pause in the app.","The AntiReel AI shortcut is optional. It adds an accessibility button or shortcut. Keep Use AntiReel switched on for tracking.")[step.coerceIn(0,3)],"miso-setup-$step",false)
    fun stopAtLimit():Boolean {
        if(preferences.getBoolean("onboardingMuted",false))return false
        val lang=preferences.getString("language","en") ?: "en"
        return say(localized(lang,"Your daily allowance is reached. Please leave this feed and take a break.","আজকের দৈনিক সীমা শেষ। রিলস বন্ধ করে একটু বিরতি নাও।"),lang)
    }
    fun greet():Boolean {
        if(preferences.getBoolean("onboardingMuted",false))return false
        val lang=preferences.getString("language","en") ?: "en"
        return say(localized(lang,"Hi, I'm Miso. I'll help you keep your scrolling boundary in view.","হাই, আমি মিসো। তোমার স্ক্রল করার সীমা মনে রাখতে সাহায্য করব।"),lang)
    }
    fun warn(stage:LimitStage,token:String):Boolean {
        if(preferences.getBoolean("onboardingMuted",false))return false
        val lang=preferences.getString("language","en") ?: "en"
        val style=preferences.getString("reminderStyle","0")
        val message=if(lang=="bn")if(stage==LimitStage.NEAR)"তোমার দৈনিক সীমা প্রায় শেষ। একটু বিরতি নাও।" else "আজকের দৈনিক সীমা শেষ। এখন রিলস বন্ধ করে বিরতি নাও।" else when(style){
            "1"->if(stage==LimitStage.NEAR)"Keep your goal in sight. You're close to today's limit." else "You've reached today's boundary. Make room for what matters to you."
            "2"->if(stage==LimitStage.NEAR)"Your daily limit is close. Prepare to stop." else "Daily limit reached. Stop watching reels now."
            "3"->if(stage==LimitStage.NEAR)"Almost at your limit." else "Daily limit reached."
            else->if(preferences.getInt("dailyMinutes",0)>0)if(stage==LimitStage.NEAR)"You are close to your daily allowance. Please prepare to take a break." else "Your daily allowance is reached. Please stop scrolling and take a break." else warningMessage(stage)
        }
        return say(message,lang,"miso-limit:$token")
    }
    fun stop(){activeUtterance=null;engine?.stop();updateSpeaking(false)}
    fun close(){closed=true;stop();engine?.shutdown();engine=null;main.removeCallbacksAndMessages(null)}
}
