package com.rafi.antireelai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Miso is the control: no frame, halo, ripple or independent animation loop. */
@Composable internal fun SessionControl(running:Boolean,paused:Boolean,reducedMotion:Boolean,onToggle:()->Unit,miso:@Composable ()->Unit) {
    val action=if(paused) "Resume" else if(running) "Pause" else "Start"
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(98.dp).clickable(interactionSource=remember{MutableInteractionSource()},indication=null,role=Role.Button,onClickLabel="$action companion",onClick=onToggle).semantics{contentDescription="$action Miso";stateDescription=if(running) "Companion active" else "Companion paused"},contentAlignment=Alignment.Center){miso()}
        UiText(action,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Control)
    }
}
