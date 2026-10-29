package com.rafi.antireelai

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LegalConsentTest {
    @get:Rule val compose=createComposeRule()
    @Test fun disclosureDeclineIsNotConsentAndAcceptIsExplicit() {
        var accepted=0;var declined=0
        compose.setContent {AccessibilityDisclosure({accepted++},{declined++})}
        compose.onNodeWithText("Not now").performClick()
        compose.runOnIdle{assertEquals(0,accepted);assertEquals(1,declined)}
        compose.onNodeWithText("I agree · open Settings").performClick()
        compose.runOnIdle{assertEquals(1,accepted)}
    }
    @Test fun oldConsentCannotStartTrackingAndRenewalPreservesHistory() {
        val base=InstrumentationRegistry.getInstrumentation().context
        val isolated=object:ContextWrapper(base){
            override fun getApplicationContext():Context=this
            override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences("legal-consent-test",mode)
        }
        val store=TrackerStore(isolated)
        store.prefs.edit().clear().putBoolean("consent",true).putBoolean("running",false)
            .putInt("goal",37).putInt("count:2026-10-29:youtube",8).commit()
        try {
            assertFalse(store.consent);store.startAfterConnection();assertFalse(store.running)
            store.requestActivation();assertTrue(store.consent);assertTrue(store.running)
            assertEquals(37,store.goal);assertEquals(8,store.prefs.getInt("count:2026-10-29:youtube",0))
            store.consent=false;assertFalse(store.consent)
        } finally {store.prefs.edit().clear().commit()}
    }
    @Test fun browserAndSupportIntentsContainNoUserData() {
        LegalDocument.entries.forEach{doc->
            val intent=ExternalLinks.documentIntent(doc)
            assertEquals(Intent.ACTION_VIEW,intent.action);assertEquals(doc.url,intent.dataString)
            assertNull(intent.extras)
        }
        val email=ExternalLinks.supportIntent()
        assertEquals(Intent.ACTION_SENDTO,email.action);assertEquals("mailto",email.data?.scheme)
        assertTrue(email.dataString!!.startsWith("mailto:hello@imrafi.com?subject="))
        assertNull(email.extras)
    }
}
