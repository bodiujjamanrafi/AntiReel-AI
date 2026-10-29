package com.rafi.antireelai

import org.junit.Assert.*
import org.junit.Test

class UsageShareTest {
    @Test fun everyRecordedPlatformKeepsItsOwnSegmentAndPercentage() {
        val shares=usageShares(List(platforms.size){(it+1).toDouble()})
        assertEquals(platforms.indices.toList(),shares.map{it.index})
        assertEquals(1.0,shares.sumOf{it.fraction},.000001)
        assertTrue(shares.zipWithNext().all{(a,b)->a.middleDegrees<b.middleDegrees})
    }
    @Test fun missingOrInvalidUsageNeverCreatesFakeSlices() {
        assertTrue(usageShares(listOf(0.0,-2.0,Double.NaN,Double.POSITIVE_INFINITY)).isEmpty())
        val shares=usageShares(listOf(0.0,30.0,0.0,70.0))
        assertEquals(listOf(1,3),shares.map{it.index})
        assertEquals(.3,shares.first().fraction,.000001)
        assertEquals(.7,shares.last().fraction,.000001)
    }
    @Test fun newDurationChangesSharesWithoutChangingPlatformIdentity() {
        val before=usageShares(listOf(100.0,100.0))
        val after=usageShares(listOf(300.0,100.0))
        assertEquals(before.map{it.index},after.map{it.index})
        assertEquals(.75,after.first().fraction,.000001)
        assertNotEquals(before.first().middleDegrees,after.first().middleDegrees)
    }
}
