package com.rafi.antireelai.tracking

import kotlin.math.abs

/** Android AccessibilityRecord initializes unset scroll deltas to -1, not zero. */
internal fun scrollDeltaOrZero(delta:Int)=if(delta == -1)0 else delta
internal fun isHorizontalPlayerScroll(dx:Int,dy:Int):Boolean {
    val x=scrollDeltaOrZero(dx);val y=scrollDeltaOrZero(dy)
    return x!=0 && kotlin.math.abs(x)>kotlin.math.abs(y)
}

/** One source of page positions across indexed and pixel-only accessibility pagers.
 * Recycled indices (often zero in Shorts) must not suppress real pixel movement.
 */
internal class ReelPager {
    var position:Int?=null;private set
    var direction=0;private set
    var changedAt=0L;private set
    var scrolledAt=0L;private set
    var token:String?=null;private set
    private var index:Int?=null
    private var scrollY:Int?=null
    private var distance=0
    private var sequence=0
    var unindexedScroll=false;private set
    private var notificationSequence=0
    fun observe(from:Int,to:Int,y:Int,dy:Int,height:Int,now:Long,currentIndex:Int=-1,playerScrollNotification:Boolean=false) {
        if(now-scrolledAt>650)distance=0
        scrolledAt=now
        val reportedIndex=currentIndex.takeIf{it>=0} ?: from.takeIf{it>=0 && it==to}
        val prior=index
        if(reportedIndex!=null)index=reportedIndex
        val indexedDelta=if(reportedIndex!=null && prior!=null)reportedIndex-prior else 0
        val reportedDelta=scrollDeltaOrZero(dy)
        val pixelDelta=if(reportedDelta!=0)reportedDelta else if(y>=0 && scrollY!=null)y-scrollY!! else 0
        if(y>=0)scrollY=y
        if(indexedDelta!=0) {
            unindexedScroll=false
            direction=if(indexedDelta>0)1 else -1
            sequence+=indexedDelta
            distance=0
            commit(now)
            return
        }
        if(pixelDelta!=0){direction=if(pixelDelta>0)1 else -1;unindexedScroll=false}
        if(reportedIndex==null && pixelDelta==0 && playerScrollNotification) {
            // Some native Shorts pagers report no indices, offset or distance at all.
            // This is evidence of a player scroll, not evidence of its direction.
            direction=0;position=null;unindexedScroll=true
            changedAt=now;notificationSequence++;token="unindexed:$notificationSequence"
            return
        }
        distance+=pixelDelta
        if(height>0 && abs(distance)>=height*.58f && now-changedAt>=150) {
            sequence+=if(distance>0)1 else -1
            distance=0
            commit(now)
        } else if(playerScrollNotification && pixelDelta!=0) {
            // A short drag can fling an entire video page. Distance alone is not a
            // page identity; pair its direction with independently changed metadata.
            position=null;unindexedScroll=true;changedAt=now
            notificationSequence++;token="motion:$notificationSequence"
        }
    }
    private fun commit(now:Long){unindexedScroll=false;position=sequence;token="page:$sequence";changedAt=now}
    fun reset(){position=null;direction=0;changedAt=0;scrolledAt=0;token=null;index=null;scrollY=null;distance=0;sequence=0;unindexedScroll=false;notificationSequence=0}
}
