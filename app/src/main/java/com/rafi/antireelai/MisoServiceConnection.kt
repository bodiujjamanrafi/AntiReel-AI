package com.rafi.antireelai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Process-local proof that Android actually connected our service. Never persisted. */
internal object MisoServiceConnection {
    var connected by mutableStateOf(false)
        private set
    fun onConnected(){connected=true}
    fun onDisconnected(){connected=false}
}

internal fun canTrackReels(accessEnabled:Boolean,serviceConnected:Boolean,consented:Boolean,
    requested:Boolean,onBreak:Boolean,hasPlatforms:Boolean)=
    accessEnabled && serviceConnected && consented && requested && !onBreak && hasPlatforms
