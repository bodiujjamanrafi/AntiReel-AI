package com.rafi.antireelai.tracking

/** Conservative forward-only counting. Coordinates live only for a continuous player session.
 * Fingerprints are one-way hashes, scoped to the local day and never written to disk.
 */
class NovelReelGate(private val capacity:Int=2048) {
    private val seen=LinkedHashSet<String>()
    private var date=""
    private var platform=""
    private var furthest:Int?=null
    private var lastToken:Long=Long.MIN_VALUE
    private var started=false
    fun resetPosition() { platform="";furthest=null;lastToken=Long.MIN_VALUE;started=false }
    fun clear() {seen.clear();resetPosition()}
    fun allow(id:String,frame:ReelFrame,position:Int?,direction:Int,transition:Long,day:String,unindexedScroll:Boolean=false):Boolean {
        if(day!=date){clear();date=day}
        if(!frame.recognized)return false
        if(platform!=id){resetPosition();platform=id}
        val prior=furthest
        if(direction<0)return false
        if(started && ((direction<=0 && !unindexedScroll) || transition==lastToken))return false
        if(prior!=null && position!=null && position<=prior)return false
        if(unindexedScroll && frame.fingerprint==null)return false
        val key=frame.fingerprint?.let{"$id:$it"}
        if(key!=null && !seen.add(key))return false
        // No labels and no page evidence: the player cannot identify a new reel reliably.
        if(key==null && position==null)return false
        // Commit position only with a count. An old caption can survive a new page;
        // rejecting it must not permanently consume that page before its title arrives.
        if(position!=null)furthest=maxOf(prior ?:position,position)
        lastToken=transition;started=true
        while(seen.size>capacity)seen.remove(seen.first())
        return true
    }
}

/** Thresholds shared by reminders and the persistent, nonblocking limit glow. */
enum class LimitStage { NONE, NEAR, REACHED, EXCEEDED;
    companion object {fun at(total:Int,goal:Int)=when {
        total>goal.coerceAtLeast(1)->EXCEEDED
        total==goal.coerceAtLeast(1)->REACHED
        total.toLong()*5>=goal.coerceAtLeast(1).toLong()*4->NEAR
        else->NONE
    }}
}
