package com.rafi.antireelai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MisoRenderingTest {
    @Test fun logoAndHeroRenderOnBothThemeBackgroundsAcrossNewViews() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            for(background in listOf(Color.rgb(247,244,248),Color.rgb(8,10,16))) {
                // Repeated instances exercise the shared cache and the small header size.
                for(size in listOf(108,666,108,666)) {
                    val view=CompanionView(instrumentation.targetContext).apply{animated=false}
                    view.layout(0,0,size,size)
                    val bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
                    val canvas=Canvas(bitmap);canvas.drawColor(background);view.draw(canvas)
                    val pixels=IntArray(size*size);bitmap.getPixels(pixels,0,size,0,0,size,size)
                    assertTrue("Miso must be visible at size $size on $background",pixels.count{it!=background}>size*size/10)
                    bitmap.recycle()
                }
            }
        }
    }
}
