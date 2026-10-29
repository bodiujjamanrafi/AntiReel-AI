package com.rafi.antireelai

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import android.graphics.Bitmap
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Reads the real UI without changing goals, permissions or recorded history. */
@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()

    @Test fun homeMetricsAndNavigationRemainReachable() {
        compose.waitUntil(timeoutMillis=10000){compose.onAllNodes(isRoot()).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("Today",useUnmergedTree=true).performClick()
        awaitText("Reels today")
        awaitText("Daily allowance")
        compose.onNodeWithText("Activity",useUnmergedTree=true).performClick()
        awaitText("Activity overview")
        compose.onNodeWithText("Platforms",useUnmergedTree=true).performClick()
        awaitText("Your apps.")
        compose.onNodeWithText("Today",useUnmergedTree=true).performClick()
        awaitText("Reels today")
        compose.onNodeWithText("Miso",useUnmergedTree=true).performClick()
        awaitText("Connect Miso")
        compose.onNodeWithText("How to connect").assertDoesNotExist()
        if(compose.onAllNodesWithText("Play").fetchSemanticsNodes().isNotEmpty())compose.onNodeWithText("Play").performClick()
        compose.onNodeWithText("Pause").assertExists()
        val agreement=compose.onNodeWithContentDescription("Agree to terms and privacy")
        val settings=compose.onAllNodesWithText("open Settings",substring=true).onLast()
        settings.assertIsDisplayed()
        if(runCatching{agreement.assertIsOn();true}.getOrDefault(false))agreement.performClick()
        settings.assertIsNotEnabled()
        compose.onNodeWithText("Terms",useUnmergedTree=true).performClick()
        awaitText("Terms of use")
        compose.runOnUiThread{compose.activity.onBackPressedDispatcher.onBackPressed()}
        awaitText("Connect Miso")
        compose.onNodeWithText("Services",useUnmergedTree=true).performClick()
        awaitText("Services & privacy")
        compose.onNodeWithText("‹ Back to Miso").performClick()
        awaitText("Connect Miso")
        agreement.performClick()
        settings.assertIsEnabled()
        compose.waitUntil(timeoutMillis=10000) {
            var active=false
            compose.runOnUiThread {
                val player=compose.activity.window.decorView.findViewWithTag<android.widget.VideoView>("miso_setup_player")
                active=player?.isPlaying==true && player.currentPosition>0
            }
            active
        }
        saveVideoFrame()
        compose.onNodeWithText("Full screen").performClick()
        awaitText("Done")
        compose.onAllNodesWithText("open Settings",substring=true).onLast().assertIsDisplayed()
        compose.onNodeWithText("Done").performClick()
        compose.onNodeWithContentDescription("Miso setup video with spoken directions and finger demonstration").assertIsDisplayed()
        saveOwnUi("miso-page-preview.png")
        compose.onNodeWithText("Settings",useUnmergedTree=true).performClick()
        compose.onNodeWithText("Miso & display").performClick()
        compose.onNode(hasContentDescription("Miso preview:",substring=true)).assertIsDisplayed()
        compose.onAllNodesWithText("Miso visibility").onFirst().performClick()
        compose.onNodeWithText("Preview only").assertDoesNotExist()
        saveOwnUi("miso-display-preview.png")
        compose.onNodeWithText("Today",useUnmergedTree=true).performClick()
    }
    private fun awaitText(text:String) {
        compose.waitUntil(timeoutMillis=10000) {
            runCatching{compose.onAllNodesWithText(text,substring=true,useUnmergedTree=true).fetchSemanticsNodes().isNotEmpty()}.getOrDefault(false)
        }
        compose.onNodeWithText(text,substring=true,useUnmergedTree=true).assertIsDisplayed()
    }
    private fun saveVideoFrame() {
        val bitmap=Bitmap.createBitmap(720,1280,Bitmap.Config.ARGB_8888)
        val copied=java.util.concurrent.CountDownLatch(1)
        var result=android.view.PixelCopy.ERROR_UNKNOWN
        compose.runOnUiThread {
            val player=compose.activity.window.decorView.findViewWithTag<android.widget.VideoView>("miso_setup_player")
            android.view.PixelCopy.request(player.holder.surface,bitmap,{code->result=code;copied.countDown()},android.os.Handler(android.os.Looper.getMainLooper()))
        }
        org.junit.Assert.assertTrue(copied.await(10,java.util.concurrent.TimeUnit.SECONDS))
        org.junit.Assert.assertEquals(android.view.PixelCopy.SUCCESS,result)
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir,"miso-video-frame.png").outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        bitmap.recycle()
    }
    private fun saveOwnUi(name:String) {
        val bitmap=compose.onRoot().captureToImage().asAndroidBitmap()
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir,name).outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
    }
}
