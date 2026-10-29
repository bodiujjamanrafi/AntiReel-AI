package com.rafi.antireelai.tracking

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/** Bounded, in-memory player evidence. Input and password subtrees are never read. */
internal fun snapshotReelNodes(root: AccessibilityNodeInfo, screenWidth:Int, screenHeight:Int,viewportOnly:Boolean=false,cancelled:()->Boolean={false}): List<ReelNode> {
        val nodes=ArrayList<ReelNode>(180)
        val rect=Rect()
        var visited=0
        fun walk(n: AccessibilityNodeInfo, depth: Int) {
            if(cancelled() || Thread.currentThread().isInterrupted || visited++ >= 650 || depth>35 || !n.isVisibleToUser || n.isPassword || n.isEditable) return
            n.getBoundsInScreen(rect)
            // Recycled YouTube pages may still report isVisibleToUser offscreen.
            if(viewportOnly && !rect.isEmpty && (rect.bottom<=0 || rect.top>=screenHeight || rect.right<=0 || rect.left>=screenWidth))return
            val klass=n.className?.toString().orEmpty().lowercase()
            if(klass.contains("seekbar"))return // Playback progress changes do not identify a new video.
            val id=n.viewIdResourceName.orEmpty().substringAfter(":id/")
            if(id=="reel_a11y_controls" || id=="reel_time_bar")return
            // YouTube's resource-free caption/creator rows occupy the lower-left
            // content area. Exclude the right engagement rail and bottom navigation.
            val videoMetadata=rect.left>=0 && rect.left<screenWidth*.16f && rect.right<screenWidth*.91f &&
                rect.width()>screenWidth*.1f && rect.top>screenHeight*.6f && rect.bottom<screenHeight*.92f &&
                rect.height()<screenHeight*.2f && !klass.contains("button") && !klass.contains("imageview")
            val tall=(n.isScrollable || klass.contains("pager") || klass.contains("recycler") || (id.contains("reel") || id.contains("shorts") || id.contains("clips"))) && rect.height()>screenHeight*.57f && rect.width()>screenWidth*.72f
            nodes.add(ReelNode(id,n.text?.toString().orEmpty().take(1500),n.contentDescription?.toString().orEmpty().take(1500),n.isSelected || n.isChecked,tall,videoMetadata))
            for(i in 0 until n.childCount) {
                if(visited>=650) break
                n.getChild(i)?.let { child -> try { walk(child,depth+1) } finally { child.recycle() } }
            }
        }
        walk(root,0)
        return nodes
    }
