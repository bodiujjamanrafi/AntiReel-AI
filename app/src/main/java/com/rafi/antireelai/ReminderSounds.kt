package com.rafi.antireelai

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun ReminderSounds() {
    val context=LocalContext.current
    var player by remember{mutableStateOf<MediaPlayer?>(null)}
    var playing by remember{mutableStateOf<Int?>(null)}
    DisposableEffect(Unit){onDispose{player?.release()}}
    SettingsGroup("Reminder sounds") {
        listOf("Break complete" to R.raw.miso_chime,"Approaching goal" to R.raw.miso_near,"Goal reached" to R.raw.miso_limit).forEachIndexed{index,(name,resource)->
            if(index>0)SettingsRule()
            SettingsRow(name,6,onClick={
                val stop=playing==resource
                player?.release();player=null;playing=null
                if(!stop)player=runCatching{MediaPlayer.create(context,resource,AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),0)?.apply{
                    setOnCompletionListener{completed->completed.release();if(player===completed){player=null;playing=null}};start();playing=resource
                }}.getOrNull()
            },trailing={AppGlyph(if(playing==resource)22 else 14,AppStyle.Accent,Modifier.size(24.dp).semantics{contentDescription=if(playing==resource)"Stop preview" else "Play preview"})})
        }
    }
}
