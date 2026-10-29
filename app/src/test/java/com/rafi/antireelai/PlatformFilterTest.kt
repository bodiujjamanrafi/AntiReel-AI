package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test

class PlatformFilterTest {
    @Test fun selectedFilterIncludesChosenAppsEvenBeforeInstallation() {
        val selected=setOf("instagram","youtube")
        assertEquals(selected,filterPlatforms(platforms,"",PlatformFilter.SELECTED,selected,setOf("youtube")).map{it.id}.toSet())
    }
    @Test fun installedFilterShowsOnlyActualInstalledCatalogEntries() {
        assertEquals(listOf("tiktok"),filterPlatforms(platforms,"",PlatformFilter.INSTALLED,emptySet(),setOf("tiktok","unknown")).map{it.id})
    }
    @Test fun searchCombinesWithFilterAndIgnoresCaseAndOuterSpaces() {
        assertEquals(listOf("youtube"),filterPlatforms(platforms," SHORTS ",PlatformFilter.SELECTED,setOf("youtube"),emptySet()).map{it.id})
        assertTrue(filterPlatforms(platforms,"Instagram",PlatformFilter.INSTALLED,emptySet(),setOf("youtube")).isEmpty())
    }
    @Test fun categoriesPartitionSupportedPlatformsWithoutAddingUnsupportedApps() {
        val social=filterPlatforms(platforms,"",PlatformFilter.SOCIAL,emptySet(),emptySet()).map{it.id}.toSet()
        val video=filterPlatforms(platforms,"",PlatformFilter.VIDEO,emptySet(),emptySet()).map{it.id}.toSet()
        assertTrue(social.intersect(video).isEmpty())
        assertEquals(platforms.map{it.id}.toSet(),social+video)
        assertTrue("instagram" in social);assertTrue("youtube" in video)
    }
}
