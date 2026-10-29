package com.rafi.antireelai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** Bounded and scrollable so setup controls remain reachable at large font sizes. */
@Composable private fun FocusDialog(title:String,subtitle:String,onDismiss:()->Unit,footer:@Composable ColumnScope.()->Unit={},content:@Composable ColumnScope.()->Unit) {
    Dialog(onDismissRequest=onDismiss) {
        Surface(shape=RoundedCornerShape(26.dp),color=AppStyle.Surface,contentColor=AppStyle.Text,border=BorderStroke(1.dp,AppStyle.Frame)) {
            Column(Modifier.fillMaxWidth().heightIn(max=680.dp).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    AppGlyph(5,AppStyle.Teal,Modifier.size(28.dp))
                    UiText(title,fontSize=24.sp,lineHeight=29.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
                    IconButton(onClick=onDismiss,modifier=Modifier.size(44.dp).semantics{contentDescription="Close $title"}){UiText("×",fontSize=27.sp,color=AppStyle.Muted)}
                }
                Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    UiText(subtitle,fontSize=14.sp,lineHeight=21.sp,color=AppStyle.Muted)
                    content()
                }
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(5.dp),content=footer)
            }
        }
    }
}
@Composable internal fun FocusModeDialog(running:Boolean,paused:Boolean,setupRequired:Boolean,selectedCount:Int,total:Int,goal:Int,onDismiss:()->Unit,onToggle:()->Unit,onBreak:()->Unit) {
    val primary=when{running->"Pause Miso";selectedCount==0->"Choose platforms";setupRequired->"Set up Miso";paused->"Resume Miso";else->"Start Miso"}
    FocusDialog("Focus mode","Keep your daily boundary in view while you use your selected platforms.",onDismiss) {
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            FocusMetric("Watched today","$total",Modifier.weight(1f))
            FocusMetric("Remaining","${(goal-total).coerceAtLeast(0)}",Modifier.weight(1f))
        }
        LinearProgressIndicator(progress={total.toFloat().div(goal.coerceAtLeast(1)).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(4.dp),color=AppStyle.Teal,trackColor=AppStyle.Frame.copy(alpha=.25f))
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            AppGlyph(if(running)20 else 21,AppStyle.Teal,Modifier.size(21.dp))
            UiText("${if(running)"Miso is active" else if(paused)"On a break" else "Standby"} · $selectedCount selected",fontSize=13.sp,color=AppStyle.Text)
        }
        Button(onClick=onToggle,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=AppStyle.Teal,contentColor=AppStyle.Background)) {
            AppGlyph(if(running)22 else 14,AppStyle.Background,Modifier.size(21.dp));Spacer(Modifier.width(10.dp));UiText(primary,fontSize=15.sp,fontWeight=FontWeight.SemiBold)
        }
        if(!paused)OutlinedButton(onClick=onBreak,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(15.dp),border=BorderStroke(1.dp,AppStyle.Frame),colors=ButtonDefaults.outlinedButtonColors(contentColor=AppStyle.Text)){UiText("Take a break")}
    }
}
@Composable private fun FocusMetric(label:String,value:String,modifier:Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        UiText(label,fontSize=12.sp,color=AppStyle.Muted)
        UiText(value,fontSize=34.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,maxLines=1)
    }
}
