package com.rafi.antireelai.tracking

/** YouTube recycles its player and frequently reports no index or scroll distance.
 * A player scroll opens a transition; independently settled content closes it.
 * Neither the recycled View ID nor the number of scroll notifications is a video ID.
 * State is main-thread confined; identities live in memory for this local day only.
 */
internal class YouTubeShortsTracker(private val settleMs:Long=80,private val capacity:Int=2048) {
    private val seen=LinkedHashSet<String>()
    private var day=""
    private var sequence=0L
    private var consumed=0L
    private var started=false
    private var direction=0
    private var position:Int?=null
    private var furthest:Int?=null
    private var candidate:String?=null
    private var candidateSince=0L
    private var candidateSequence=-1L
    private var session=0L
    var pending=false;private set
    var diagnostic="idle";private set
    val generation:Long get()=sequence

    fun scroll(direction:Int,position:Int?) {
        sequence++
        this.direction=direction;this.position=position
        candidate=null;pending=false
    }
    /** A snapshot dispatched before a scroll must not be paired with that scroll. */
    fun observe(frame:ReelFrame,now:Long,date:String,snapshotGeneration:Long=sequence,compatibility:Boolean=true):Boolean {
        if(day.isEmpty())day=date
        else if(day!=date){clear();day=date;return observe(frame,now,date,sequence,compatibility)}
        if(snapshotGeneration!=sequence){diagnostic="stale_snapshot";return false}
        if(!frame.recognized) {
            pending=false;candidate=null
            if(frame.reason=="advertisement") {
                // An ad consumes its transition, but must not reset the whole session.
                consumed=sequence;diagnostic="advertisement"
            } else {
                // A confirmed non-player tree is an exit, even if the next player
                // opens quickly from Search/Home. Seen identities survive this reset.
                resetSession()
                diagnostic="outside_player"
            }
            return false
        }
        if(started && sequence==consumed){pending=false;diagnostic="awaiting_scroll";return false}
        if(direction<0){pending=false;diagnostic="backward";return false}
        if(position!=null && furthest!=null && position!!<=furthest!!){pending=false;diagnostic="previous_page";return false}
        // Without metadata, only an independently indexed forward page is usable.
        // No timer, gesture or zero-metric notification invents a video identity.
        val identity=frame.fingerprint ?: if(compatibility && position!=null)"indexed:$session:$position" else null
        if(identity==null){pending=false;diagnostic="missing_identity";return false}
        if(identity in seen){pending=false;candidate=null;diagnostic="seen_video";return false}
        if(candidate!=identity || candidateSequence!=sequence) {
            candidate=identity;candidateSequence=sequence;candidateSince=now;pending=true
            diagnostic="settling";return false
        }
        if(now-candidateSince<settleMs){pending=true;return false}
        seen.add(identity)
        while(seen.size>capacity)seen.remove(seen.first())
        if(position!=null)furthest=maxOf(furthest ?: position!!,position!!)
        consumed=sequence;started=true;pending=false;diagnostic="counted"
        return true
    }
    fun resetSession() {
        sequence++;session++;consumed=sequence;started=false;direction=0;position=null;furthest=null
        candidate=null;candidateSequence=-1;pending=false;diagnostic="idle"
    }
    fun clear(){seen.clear();resetSession()}
}
