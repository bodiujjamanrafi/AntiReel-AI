package com.rafi.antireelai

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MisoSpeechRenderingTest {
    @Test fun mouthMovesWhileTalkingEvenWhenIdleMotionIsOff() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        lateinit var view:CompanionView
        instrumentation.runOnMainSync {
            view=CompanionView(instrumentation.targetContext).apply{animated=false;talking=false;progress=1f}
            view.layout(0,0,320,320)
        }
        fun pixels(talking:Boolean):IntArray {
            val result=IntArray(320*320)
            instrumentation.runOnMainSync {
                view.talking=talking
                val bitmap=Bitmap.createBitmap(320,320,Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap));bitmap.getPixels(result,0,320,0,0,320,320);bitmap.recycle()
            }
            return result
        }
        val closed=pixels(false)
        val first=pixels(true)
        android.os.SystemClock.sleep(110)
        val second=pixels(true)
        assertTrue("Talking changes the mouth when body animation is off",closed.indices.count{closed[it]!=first[it]}>10)
        assertTrue("The speaking mouth continues moving",first.indices.count{first[it]!=second[it]}>5)
        assertArrayEquals("Stopping speech restores the static face",closed,pixels(false))
    }
}
