package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** All writes use a temporary preference file. No real app selections or permissions are changed. */
class AdvancedFeaturesTest {
    @get:Rule val ui=createAndroidComposeRule<ComponentActivity>()
    private val base get()=InstrumentationRegistry.getInstrumentation().targetContext
    private fun isolated(action:(TrackerStore,Context)->Unit) {
        val name="advanced-features-${System.nanoTime()}"
        val wrapper=object:ContextWrapper(base) {
            override fun getSharedPreferences(ignored:String,mode:Int):SharedPreferences=base.getSharedPreferences(name,mode)
        }
        try {action(TrackerStore(wrapper),wrapper)} finally {
            base.deleteSharedPreferences(name)
            ui.runOnUiThread{PlatformCatalog.refresh(TrackerStore(base))}
        }
    }
    @Test fun legacyYoutubeHistorySurvivesCustomAddRemoveAndRestart()=isolated {store,context->
        val today=LocalDate.now()
        store.prefs.edit().putStringSet("platforms",setOf("youtube","facebook"))
            .putInt("count:$today:youtube",9).putInt("count:$today:facebook",5)
            .putInt("count:$today:facebook_lite",2).commit()
        assertEquals(16,store.total())
        assertEquals(setOf("youtube","facebook"),store.selected)
        assertFalse(store.trackedPlatforms.any{it.id=="youtube"})
        assertTrue(store.addCustomPlatform(CustomPlatforms.YOUTUBE,"YouTube"))
        assertFalse(store.addCustomPlatform(CustomPlatforms.YOUTUBE,"Duplicate"))
        val restarted=TrackerStore(context)
        assertTrue(restarted.trackedPlatforms.any{it.id=="youtube" && it.experimental})
        restarted.removeCustomPlatform(restarted.catalog.first{it.id=="youtube"})
        assertFalse(TrackerStore(context).trackedPlatforms.any{it.id=="youtube"})
        assertEquals(9,store.count("youtube"));assertEquals(16,store.total())
        assertEquals(setOf("facebook"),store.selected)
    }
    @Test fun unsupportedCustomAppsArePersistentButNeverMonitored()=isolated {store,context->
        assertTrue(store.addCustomPlatform("example.video.app","Example video"))
        val app=store.catalog.last()
        store.selected=store.selected+app.id
        assertTrue(app.isCustom);assertNull(app.detectorId)
        assertFalse(store.trackedPlatforms.any{it.id==app.id})
        assertTrue(TrackerStore(context).catalog.any{it.id==app.id && it.available})
        store.removeCustomPlatform(app)
        val restored=TrackerStore(context)
        assertTrue(restored.catalog.any{it.id==app.id && !it.available})
        assertFalse(app.id in restored.selected)
    }
    @Test fun youtubeDialogRequiresAffirmativeConfirmation() {
        var show by mutableStateOf(true)
        var confirmed=0
        ui.setContent {MaterialTheme {if(show)CustomPlatformConfirmation(InstalledPlatformCandidate(CustomPlatforms.YOUTUBE,"YouTube"),{show=false},{confirmed++;show=false})}}
        ui.onNodeWithText("YouTube Shorts Tracking Limitation").assertIsDisplayed()
        ui.onNodeWithText(YOUTUBE_LIMITATION).assertIsDisplayed()
        ui.onNodeWithText("Cancel").performClick()
        ui.runOnIdle{assertEquals(0,confirmed);show=true}
        ui.onNodeWithText("Add anyway").performClick()
        ui.runOnIdle{assertEquals(1,confirmed)}
    }
    @Test fun installedAppPickerSearchAndYoutubeConfirmationWorkWithoutAutoEnabling()=isolated {store,_->
        org.junit.Assume.assumeTrue(base.packageManager.getLaunchIntentForPackage(CustomPlatforms.YOUTUBE)!=null)
        var show by mutableStateOf(true)
        ui.setContent {MaterialTheme {if(show)CustomPlatformPicker(store){show=false}}}
        ui.onNode(hasSetTextAction()).performTextReplacement("YouTube")
        ui.waitUntil(10000){ui.onAllNodes(hasText("YouTube") and hasClickAction() and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()}
        ui.onNode(hasText("YouTube") and hasClickAction() and !hasSetTextAction()).performClick()
        ui.onNodeWithText("YouTube Shorts Tracking Limitation").assertIsDisplayed()
        ui.runOnIdle{assertFalse(store.trackedPlatforms.any{it.id=="youtube"})}
        ui.onNodeWithText("Cancel").performClick()
        ui.onNode(hasText("YouTube") and hasClickAction() and !hasSetTextAction()).performClick()
        ui.onNodeWithText("Add anyway").performClick()
        ui.runOnIdle{assertTrue(store.trackedPlatforms.any{it.id=="youtube" && it.experimental})}
    }
    @Test fun customDailyGoalValidationAndPresetsWorkInBothThemes() {
        var dark by mutableStateOf(false)
        var value:Int?=null
        val oldDark=AppStyle.dark
        try {
            ui.setContent {AppStyle.dark=dark;MaterialTheme {GoalEditor(30,{}, {value=it})}}
            for(theme in listOf(false,true)) {
                ui.runOnIdle{dark=theme}
                val field=ui.onNode(hasSetTextAction())
                for(invalid in listOf("","0","-3","abc","2147483648")) {
                    field.performTextReplacement(invalid)
                    ui.onNodeWithText("Save daily goal").assertIsNotEnabled()
                }
                field.performTextReplacement("37")
                ui.onNodeWithText("Enter your own limit").assertIsDisplayed()
                ui.onNodeWithText("Save daily goal").assertIsEnabled().performClick()
                ui.runOnIdle{assertEquals(37,value)}
                ui.onNodeWithText("50").performClick()
                ui.onNodeWithText("Save daily goal").performClick()
                ui.runOnIdle{assertEquals(50,value)}
            }
        } finally {ui.runOnIdle{AppStyle.dark=oldDark}}
    }
    @Test fun exportCorrectedLauncherLayersAndEveryMisoPose() {
        val output=File(base.cacheDir,"advanced-brand-review").apply{mkdirs()}
        fun save(bitmap:Bitmap,name:String) {File(output,name).outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}}
        ui.runOnUiThread {
            val view=CompanionView(base).apply{animated=false;expressive=true}
            view.layout(0,0,344,344)
            val layer=Bitmap.createBitmap(432,432,Bitmap.Config.ARGB_8888)
            Canvas(layer).apply{translate(44f,44f);view.draw(this)}
            save(layer,"miso_launcher_foreground.png")
            for(size in listOf(48,72,96,144,192)) {
                val bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888)
                Canvas(bitmap).apply{drawColor(Color.rgb(81,33,59));drawBitmap(layer,null,android.graphics.Rect(0,0,size,size),android.graphics.Paint(3))}
                assertEquals(255,Color.alpha(bitmap.getPixel(0,0)))
                save(bitmap,"launcher-$size.png");bitmap.recycle()
            }
            runCatching {base.packageManager.getApplicationIcon("com.facebook.lite")}.getOrNull()?.let {lite->
                val bitmap=Bitmap.createBitmap(512,512,Bitmap.Config.ARGB_8888)
                lite.setBounds(0,0,512,512);lite.draw(Canvas(bitmap));save(bitmap,"facebook_lite.png");bitmap.recycle()
            }
            val icon=base.packageManager.getApplicationIcon(base.packageName)
            assertTrue(icon is AdaptiveIconDrawable)
            val adaptive=icon as AdaptiveIconDrawable
            val background=Bitmap.createBitmap(108,108,Bitmap.Config.ARGB_8888)
            adaptive.background.setBounds(0,0,108,108);adaptive.background.draw(Canvas(background))
            assertEquals(255,Color.alpha(background.getPixel(0,0)))
            assertEquals(255,Color.alpha(background.getPixel(54,54)));background.recycle()
            for(round in listOf(false,true)) {
                val masked=Bitmap.createBitmap(192,192,Bitmap.Config.ARGB_8888)
                val canvas=Canvas(masked)
                val path=android.graphics.Path().apply {
                    if(round)addCircle(96f,96f,96f,android.graphics.Path.Direction.CW)
                    else addRoundRect(0f,0f,192f,192f,44f,44f,android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(path);icon.setBounds(0,0,192,192);icon.draw(canvas)
                assertEquals(255,Color.alpha(masked.getPixel(96,96)))
                save(masked,"adaptive-${if(round)"circle" else "rounded"}.png");masked.recycle()
            }
            if(android.os.Build.VERSION.SDK_INT>=33)assertNotNull(adaptive.monochrome)
            for(pose in MisoPose.entries)for(progress in listOf(0f,1f)) {
                view.pose=pose;view.progress=progress
                val bitmap=Bitmap.createBitmap(344,344,Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap));save(bitmap,"miso-${pose.name}-$progress.png");bitmap.recycle()
            }
            layer.recycle()
        }
    }
}
