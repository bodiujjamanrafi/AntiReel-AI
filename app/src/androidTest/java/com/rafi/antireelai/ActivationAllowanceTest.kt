package com.rafi.antireelai

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ActivationAllowanceTest {
    @Test fun activationPauseAndSingleDailyRestart() {
        // Test APK context keeps the user's real settings and history untouched.
        val context=object:android.content.ContextWrapper(InstrumentationRegistry.getInstrumentation().context){
            override fun getApplicationContext():android.content.Context=this
        }
        val store=TrackerStore(context)
        store.prefs.edit().clear().commit()
        try {
            store.startAfterConnection();assertFalse(store.running)
            store.requestActivation();store.startAfterConnection();assertTrue(store.running)
            store.running=false;store.startAfterConnection();assertFalse(store.running)
            store.requestActivation();assertTrue(store.running)
            store.goal=2
            assertFalse(store.restartForToday())
            store.increment("instagram");store.increment("instagram")
            assertTrue(store.restartForToday());assertEquals(32,store.goal)
            assertEquals(2,store.total());assertFalse(store.restartForToday())
            store.goal=999;assertEquals(32,store.goal)
            repeat(55){store.increment("instagram")}
            assertEquals(32,store.total());assertFalse(store.restartForToday())
            store.prefs.edit().putString("restartDay",LocalDate.now().minusDays(1).toString()).commit()
            assertFalse(store.restartUsed);assertEquals(999,store.goal)
        } finally {store.prefs.edit().clear().commit()}
    }
}
