package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test

class PlatformFilterTest {
    @Test fun selectedFilterIncludesChosenAppsEvenBeforeInstallation() {
        val selected=setOf("instagram","facebook_lite")
        assertEquals(setOf("instagram"),filterPlatforms(platforms,"",PlatformFilter.SELECTED,selected,setOf("facebook_lite")).map{it.id}.toSet())
    }
    @Test fun installedFilterShowsOnlyActualInstalledCatalogEntries() {
        assertEquals(listOf("tiktok"),filterPlatforms(platforms,"",PlatformFilter.INSTALLED,emptySet(),setOf("tiktok","unknown")).map{it.id})
    }
    @Test fun searchCombinesWithFilterAndIgnoresCaseAndOuterSpaces() {
        assertTrue(filterPlatforms(platforms," REELS ",PlatformFilter.SELECTED,setOf("facebook_lite"),emptySet()).isEmpty())
        assertTrue(filterPlatforms(platforms,"Instagram",PlatformFilter.INSTALLED,emptySet(),setOf("facebook_lite")).isEmpty())
    }
    @Test fun categoriesPartitionSupportedPlatformsWithoutAddingUnsupportedApps() {
        val social=filterPlatforms(platforms,"",PlatformFilter.SOCIAL,emptySet(),emptySet()).map{it.id}.toSet()
        val video=filterPlatforms(platforms,"",PlatformFilter.VIDEO,emptySet(),emptySet()).map{it.id}.toSet()
        assertTrue(social.intersect(video).isEmpty())
        assertEquals(platforms.filter{it.available}.map{it.id}.toSet(),social+video)
        assertTrue("instagram" in social);assertFalse("facebook_lite" in social)
    }
}
