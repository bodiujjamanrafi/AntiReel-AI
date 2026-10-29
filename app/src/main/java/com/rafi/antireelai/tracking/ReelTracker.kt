package com.rafi.antireelai.tracking

/** Joins settled player identity with forward-page evidence in either arrival order. */
internal class ReelTracker {
    private val session=ReelSession()
    private val novel=NovelReelGate()
    private var candidate=false
    val pending:Boolean get()=session.pending
    fun observe(platform:String,frame:ReelFrame,pager:String?,compatibility:Boolean,position:Int?,direction:Int,transition:Long,now:Long,day:String,unindexedScroll:Boolean=false):Boolean {
        if(!frame.recognized)candidate=false
        if(session.observe(platform,frame,pager,compatibility,now))candidate=true
        if(candidate && !session.pending && novel.allow(platform,frame,position,direction,transition,day,unindexedScroll && now-transition in 0..1500)) {
            candidate=false
            return true
        }
        return false
    }
    fun reset(){session.reset();novel.resetPosition();candidate=false}
    fun clear(){reset();novel.clear()}
}
