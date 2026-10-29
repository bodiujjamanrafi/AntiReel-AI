package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test

class PreferenceUiUpdatesTest {
    @Test fun usageTransactionRetainsBothDomainsAndDrainsOnce() {
        val q=PreferenceUiUpdates()
        listOf("usage:today:youtube","count:today:youtube","lastUsageUpdate").forEach{assertTrue(q.collect(it))}
        assertEquals(PreferenceUiUpdates.ACTIVITY or PreferenceUiUpdates.SETTINGS,q.drain())
        assertEquals(0,q.drain())
    }
    @Test fun settingsChangesDoNotRebuildHistory() {
        val q=PreferenceUiUpdates();q.collect("themeMode");q.collect("overlaySize")
        assertEquals(PreferenceUiUpdates.SETTINGS,q.drain())
    }
    @Test fun bookkeepingCannotScheduleUiRefresh() {
        val q=PreferenceUiUpdates()
        listOf(null,"status:youtube","nudged:today","refreshUsageRequest").forEach{assertFalse(q.collect(it))}
        assertEquals(0,q.drain())
    }
    @Test fun clearHistoryStillRefreshesGraphsAndMetrics() {
        val q=PreferenceUiUpdates();q.collect("activityEpoch")
        assertEquals(3,q.drain())
    }
    @Test fun onlyActivityNeedsThirtyDaysAndHiddenScreensNeedNone() {
        assertEquals(7,historyDaysForScreen(0));assertEquals(7,historyDaysForScreen(1))
        assertEquals(30,historyDaysForScreen(2));assertEquals(0,historyDaysForScreen(3));assertEquals(0,historyDaysForScreen(4))
    }
}
