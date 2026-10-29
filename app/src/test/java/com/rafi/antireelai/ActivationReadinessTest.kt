package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test

class ActivationReadinessTest {
    @Test fun activeRequiresPermissionActualServiceConsentSelectionAndNoBreak() {
        for(mask in 0 until 64) {
            val enabled=mask and 1!=0
            val bound=mask and 2!=0
            val consent=mask and 4!=0
            val requested=mask and 8!=0
            val paused=mask and 16!=0
            val selected=mask and 32!=0
            val active=canTrackReels(enabled,bound,consent,requested,paused,selected)
            assertEquals("Only fully ready state can be active: $mask",mask==47,active)
        }
    }
    @Test fun persistedStartRequestCannotSubstituteForAndroidConnection() {
        assertFalse(canTrackReels(false,false,true,true,false,true))
        assertFalse(canTrackReels(true,false,true,true,false,true))
        assertFalse(canTrackReels(false,true,true,true,false,true))
        assertTrue(canTrackReels(true,true,true,true,false,true))
    }
}
