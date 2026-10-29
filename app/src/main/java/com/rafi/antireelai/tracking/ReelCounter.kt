package com.rafi.antireelai.tracking

/** Deduplicates stable player identities; tolerates briefly missing accessibility metadata. */
class ReelCounter(private val dwellMs: Long = 120, private val missingGraceMs: Long = 650) {
    private var candidate = ""
    private var since = 0L
    private var lastSeen = 0L
    private var counted = false
    val pending: Boolean get() = candidate.isNotEmpty() && !counted
    fun reset() { candidate = ""; since = 0; lastSeen = 0; counted = false }
    fun observe(identity: String?, now: Long): Boolean {
        if (identity.isNullOrBlank()) {
            if(now-lastSeen > missingGraceMs) reset()
            return false
        }
        lastSeen=now
        if (candidate != identity) { candidate = identity; since = now; counted = false; return false }
        if (!counted && now - since >= dwellMs) { counted = true; return true }
        return false
    }
}
