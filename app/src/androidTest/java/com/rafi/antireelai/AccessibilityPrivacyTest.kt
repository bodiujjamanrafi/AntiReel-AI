package com.rafi.antireelai

import android.content.ComponentName
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import com.rafi.antireelai.tracking.ReelAccessibilityService
import com.rafi.antireelai.tracking.snapshotReelNodes
import org.junit.Assert.*
import org.junit.Test

class AccessibilityPrivacyTest {
    @Test fun cancelledInspectionReturnsNoScreenLabels() {
        val node=AccessibilityNodeInfo.obtain()
        try {
            node.isVisibleToUser=true;node.text="discarded when access is revoked"
            assertTrue(snapshotReelNodes(node,1080,2400){true}.isEmpty())
        } finally {node.recycle()}
    }
    @Test fun editablePasswordAndHiddenNodesAreExcluded() {
        val node=AccessibilityNodeInfo.obtain()
        try {
            node.isVisibleToUser=true
            node.text="ordinary visible player label"
            assertEquals("ordinary visible player label",snapshotReelNodes(node,1080,2400).single().text)
            node.isPassword=true
            assertTrue(snapshotReelNodes(node,1080,2400).isEmpty())
            node.isPassword=false;node.isEditable=true
            assertTrue(snapshotReelNodes(node,1080,2400).isEmpty())
            node.isEditable=false;node.isVisibleToUser=false
            assertTrue(snapshotReelNodes(node,1080,2400).isEmpty())
        } finally {node.recycle()}
    }
    @Test fun accessibilityEntryIsSystemGuardedAndAppHasNoNetworkPermission() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val pm=context.packageManager
        val service=pm.getServiceInfo(ComponentName(context,ReelAccessibilityService::class.java),0)
        assertEquals("android.permission.BIND_ACCESSIBILITY_SERVICE",service.permission)
        val permissions=pm.getPackageInfo(context.packageName,PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty().toSet()
        assertFalse("android.permission.INTERNET" in permissions)
        assertFalse("android.permission.READ_SMS" in permissions)
        assertFalse("android.permission.READ_CONTACTS" in permissions)
        assertFalse("android.permission.REQUEST_INSTALL_PACKAGES" in permissions)
    }

    @Test fun playbackProgressIsNotVideoIdentity() {
        val node=AccessibilityNodeInfo.obtain()
        try {
            node.isVisibleToUser=true;node.className="android.widget.SeekBar"
            node.contentDescription="Video progress, 0:12 of 0:30"
            assertTrue(snapshotReelNodes(node,1440,3120).isEmpty())
        }finally{node.recycle()}
    }

    @Test fun captionRegionExcludesTheEngagementRail() {
        val node=AccessibilityNodeInfo.obtain()
        try {
            node.isVisibleToUser=true;node.className="android.view.ViewGroup";node.contentDescription="Example caption"
            node.setBoundsInScreen(android.graphics.Rect(48,2698,1250,2751))
            assertTrue(snapshotReelNodes(node,1440,3120).single().videoMetadata)
            node.setBoundsInScreen(android.graphics.Rect(1250,2178,1440,2357))
            assertFalse(snapshotReelNodes(node,1440,3120).single().videoMetadata)
        }finally{node.recycle()}
    }
    @Test fun youtubeRecycledOffscreenNodesAreNotVisibleContent() {
        val node=AccessibilityNodeInfo.obtain()
        try {
            node.isVisibleToUser=true;node.className="android.view.ViewGroup";node.text="Offscreen recycled caption"
            node.setBoundsInScreen(android.graphics.Rect(40,2500,900,2580))
            assertTrue(snapshotReelNodes(node,1080,2400,viewportOnly=true).isEmpty())
            // The other platform snapshot behavior stays unchanged.
            assertEquals(1,snapshotReelNodes(node,1080,2400).size)
        }finally{node.recycle()}
    }
}
