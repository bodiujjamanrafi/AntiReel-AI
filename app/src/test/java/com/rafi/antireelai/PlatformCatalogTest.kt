package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlatformCatalogTest {
    @Test fun removedPlatformsKeepIndependentHistoryButCannotTrack() {
        val catalog=CustomPlatforms.catalog(emptyList())
        assertFalse(catalog.first{it.id=="youtube"}.available)
        assertEquals(setOf("com.facebook.katana"),catalog.first{it.id=="facebook"}.packages)
        val lite=catalog.first{it.id=="facebook_lite"}
        assertEquals(setOf("com.facebook.lite"),lite.packages)
        assertFalse(lite.available);assertNull(lite.detectorId)
        val today=LocalDate.now()
        val data=ActivityDay.read(today,mapOf("count:$today:youtube" to 4,"count:$today:facebook" to 2,"count:$today:facebook_lite" to 3),catalog)
        assertEquals(9,data.totalReels)
    }
    @Test fun customCodecPreservesNamesOrderAndRemovedHistoryMetadata() {
        val records=listOf(CustomPlatformRecord("example.video.app","ভিডিও | App",true),CustomPlatformRecord(CustomPlatforms.YOUTUBE,"YouTube",false))
        assertEquals(records,CustomPlatforms.decode(CustomPlatforms.encode(records)))
        val catalog=CustomPlatforms.catalog(records)
        assertNull(catalog.last().detectorId)
        assertTrue(catalog.last().available)
        assertFalse(catalog.first{it.id=="youtube"}.available)
    }
    @Test fun customYoutubeNeedsAnExplicitActiveRecordAndIsAlwaysExperimental() {
        val catalog=CustomPlatforms.catalog(listOf(CustomPlatformRecord(CustomPlatforms.YOUTUBE,"YouTube",true)))
        val youtube=catalog.single{it.id=="youtube"}
        assertTrue(youtube.available);assertTrue(youtube.experimental);assertTrue(youtube.isCustom)
        assertEquals("youtube",youtube.detectorId)
    }
    @Test fun malformedAndDuplicateCustomPackagesAreRejected() {
        assertFalse(CustomPlatforms.validPackage("../bad"))
        assertTrue(CustomPlatforms.decode("bad|oops|1\nexample.valid|%%%|1").isEmpty())
        val records=listOf(CustomPlatformRecord("com.facebook.lite","Duplicate",true),CustomPlatformRecord("example.video.app","One",true),CustomPlatformRecord("example.video.app","Two",true))
        val decoded=CustomPlatforms.decode(CustomPlatforms.encode(records))
        assertEquals(1,decoded.count{it.packageName=="example.video.app"})
        assertEquals(1,CustomPlatforms.catalog(decoded).count{"com.facebook.lite" in it.packages})
    }
    @Test fun dailyGoalRejectsUnsafeValuesAndAcceptsCustomNumbers() {
        listOf("","0","-1","+3","1.5","cat","2147483648","999999999999999999").forEach{assertNull(it,parseDailyGoal(it))}
        assertEquals(37,parseDailyGoal("37"));assertEquals(Int.MAX_VALUE,parseDailyGoal("2147483647"))
        assertEquals(50,parseDailyGoal("৫০"))
    }
}
