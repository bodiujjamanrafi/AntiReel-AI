package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test
import java.net.URI

class LegalPolicyTest {
    @Test fun consentRequiresAnAffirmativeChoiceOnTheCurrentDisclosure() {
        assertFalse(AccessibilityConsent.current(false,AccessibilityConsent.VERSION))
        assertFalse(AccessibilityConsent.current(true,0))
        assertFalse(AccessibilityConsent.current(true,1))
        assertFalse(AccessibilityConsent.current(true,AccessibilityConsent.VERSION+1))
        assertTrue(AccessibilityConsent.current(true,AccessibilityConsent.VERSION))
    }
    @Test fun legalDestinationsAreFixedHttpsUrlsWithoutProfileOrTrackingParameters() {
        assertEquals(setOf("privacy","terms","security","support"),LegalDocument.entries.map{it.slug}.toSet())
        LegalDocument.entries.forEach{document->
            val url=URI(document.url)
            assertEquals("https",url.scheme);assertEquals("legal.imrafi.bond",url.host)
            assertEquals("/${document.slug}",url.path);assertNull(url.query);assertNull(url.userInfo)
            assertEquals("legal/${document.slug}.json",document.asset)
        }
        assertEquals("hello@imrafi.com",SUPPORT_EMAIL)
    }
    @Test fun disclosureExplainsObservationInterruptionLocalStorageAndRevocationInBothLanguages() {
        val english=AccessibilityConsent.disclosure("en")
        listOf("visible accessibility labels","scroll/page events","selected social apps","saved locally","media pause","Home","decline","revoke").forEach{assertTrue(it,english.contains(it))}
        assertTrue(AccessibilityConsent.disclosure("bn").contains("অ্যাক্সেসিবিলিটি"))
    }
}
