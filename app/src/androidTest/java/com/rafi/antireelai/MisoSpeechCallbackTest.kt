package com.rafi.antireelai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rafi.antireelai.tracking.LimitStage
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MisoSpeechCallbackTest {
    @Test fun spokenWarningStartsAndStopsTheNativeMouthWithoutScannerPolling() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val started=CountDownLatch(1);val stopped=CountDownLatch(1)
        lateinit var speaker:MisoSpeaker
        lateinit var view:CompanionView
        instrumentation.runOnMainSync {
            view=CompanionView(instrumentation.targetContext).apply{animated=false}
            speaker=MisoSpeaker(instrumentation.targetContext,onSpeakingChanged={talking->
                view.talking=talking
                if(talking)started.countDown() else stopped.countDown()
            })
        }
        try {
            var ready=false
            val until=android.os.SystemClock.uptimeMillis()+15000
            while(!ready && android.os.SystemClock.uptimeMillis()<until) {
                instrumentation.runOnMainSync{ready=speaker.ready}
                if(!ready)android.os.SystemClock.sleep(30)
            }
            assertTrue("Offline speech should initialize",ready)
            instrumentation.runOnMainSync{assertTrue(speaker.warn(LimitStage.REACHED,"render-test"))}
            assertTrue(started.await(10,TimeUnit.SECONDS))
            instrumentation.runOnMainSync{assertTrue(view.talking);speaker.stop();assertFalse(view.talking)}
            assertTrue(stopped.await(3,TimeUnit.SECONDS))
        } finally {instrumentation.runOnMainSync{speaker.close()}}
    }
}
