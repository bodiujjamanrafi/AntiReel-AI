package com.rafi.antireelai

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Export the exact native character, without capturing any device screen or user data. */
@RunWith(AndroidJUnit4::class)
class MisoAssetExportTest {
    @Test fun exportNativeMisoForLauncher() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val view=CompanionView(instrumentation.targetContext).apply{animated=false;progress=0f}
            view.layout(0,0,648,648)
            val bitmap=Bitmap.createBitmap(648,648,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File(instrumentation.targetContext.cacheDir,"miso-native.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
    }
}
