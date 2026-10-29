package com.rafi.antireelai

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Synthesizes only fixed onboarding instructions; never reads screen or user data. */
@RunWith(AndroidJUnit4::class)
class MisoNarrationExportTest {
    @Test fun exportOfflineMisoNarration() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val initialized=CountDownLatch(1)
        var status=TextToSpeech.ERROR
        lateinit var engine:TextToSpeech
        instrumentation.runOnMainSync {
            engine=TextToSpeech(instrumentation.targetContext){result->status=result;initialized.countDown()}
        }
        try {
            assertTrue(initialized.await(30,TimeUnit.SECONDS));assertEquals(TextToSpeech.SUCCESS,status)
            val voice=engine.voices.filter{!it.isNetworkConnectionRequired && it.locale.language=="en" && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)}.sortedBy{if(it.locale.country=="US")0 else 1}.firstOrNull()
            assertNotNull("An installed offline English voice is required to export Miso narration",voice)
            engine.voice=voice;engine.setPitch(1.15f);engine.setSpeechRate(1.05f)
            val messages=listOf(
                "Open Accessibility. Tap AntiReel AI, then turn on Use AntiReel AI.",
                "Read Android's notice. If you agree, tap Allow.",
                "The shortcut is optional. Keep the main service on."
            )
            for((index,message) in messages.withIndex()) {
                val done=CountDownLatch(1);var succeeded=false
                engine.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
                    override fun onStart(id:String?){}
                    override fun onDone(id:String?){succeeded=true;done.countDown()}
                    @Deprecated("Legacy callback") override fun onError(id:String?){done.countDown()}
                })
                val file=File(instrumentation.targetContext.cacheDir,"miso-guide-$index.wav")
                assertEquals(TextToSpeech.SUCCESS,engine.synthesizeToFile(message,null,file,"guide-$index"))
                assertTrue(done.await(45,TimeUnit.SECONDS));assertTrue(succeeded);assertTrue(file.length()>1000)
            }
        } finally {engine.shutdown()}
    }
}
