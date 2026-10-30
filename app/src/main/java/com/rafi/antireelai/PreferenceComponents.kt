package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay

@Composable internal fun ThemePicker(mode:String,onChange:(String)->Unit) {
    SettingsBody {
        UiText("Appearance",fontSize=20.sp,fontWeight=FontWeight.SemiBold)
        ThemeChoices(mode,LocalUiLanguage.current,onChange)
    }
}
@Composable private fun ActionDialog(onDismiss:()->Unit,content:@Composable ColumnScope.()->Unit) {
    Dialog(onDismissRequest=onDismiss) {
        Surface(shape=RoundedCornerShape(30.dp),color=AppStyle.Surface,contentColor=AppStyle.Text,border=BorderStroke(1.dp,AppStyle.Outline)) {Column(Modifier.fillMaxWidth().padding(24.dp),verticalArrangement=Arrangement.spacedBy(18.dp),content=content)}
    }
}
@Composable internal fun BreakPicker(onDismiss:()->Unit,onSelect:(Int)->Unit) {
    var minutes by remember {mutableIntStateOf(5)}
    ActionDialog(onDismiss) {
        AppGlyph(4,AppStyle.Accent,Modifier.size(34.dp))
        UiText("Make room for you",fontSize=26.sp,lineHeight=30.sp,fontWeight=FontWeight.Bold)
        UiText("Miso will pause counting and resume when your break ends, if your companion is on.",fontSize=14.sp,lineHeight=21.sp,color=AppStyle.Muted)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf(2,5,10).forEach {value->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).border(if(minutes==value)2.dp else 1.dp,if(minutes==value)AppStyle.Text else AppStyle.Frame,RoundedCornerShape(15.dp)).clickable{minutes=value}.padding(vertical=18.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    UiText("$value",fontSize=25.sp,fontWeight=FontWeight.Bold,color=AppStyle.Accent);UiText("minutes",fontSize=11.sp,color=AppStyle.Muted)
                }
            }
        }
        OutlinedButton(onClick={onSelect(minutes)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(15.dp)){UiText("Begin my break",fontWeight=FontWeight.SemiBold)}
        TextButton(onClick=onDismiss,modifier=Modifier.align(Alignment.CenterHorizontally)){UiText("Keep going")}
    }
}
internal fun parseDailyGoal(value:String):Int? {
    val text=value.trim()
    if(text.isEmpty() || text.length>10 || text.any{Character.digit(it,10)<0})return null
    return text.map{('0'.code+Character.digit(it,10)).toChar()}.joinToString("").toIntOrNull()?.takeIf{it>0}
}
@Composable internal fun GoalEditor(initial:Int,onDismiss:()->Unit,onSave:(Int)->Unit) {
    var input by remember(initial){mutableStateOf(initial.toString())}
    val goal=parseDailyGoal(input)
    ActionDialog(onDismiss) {
        AppGlyph(5,AppStyle.Accent,Modifier.size(34.dp))
        UiText("Your daily boundary",fontSize=26.sp,fontWeight=FontWeight.Bold)
        UiText("Choose any positive whole number of reels across your selected apps.",fontSize=14.sp,lineHeight=21.sp,color=AppStyle.Muted)
        DailyGoalField(input,{input=it})
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(20,30,50,100).forEach{value->FilterChip(selected=goal==value,onClick={input=value.toString()},label={UiText("$value",fontSize=12.sp)})}}
        UiText("Miso speaks near your limit, at the limit, and again when you cross it. The red glow follows your progress.",fontSize=13.sp,lineHeight=20.sp,color=AppStyle.Muted)
        OutlinedButton(onClick={goal?.let(onSave)},enabled=goal!=null,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(15.dp)){UiText("Save daily goal",fontWeight=FontWeight.SemiBold)}
        TextButton(onClick=onDismiss,modifier=Modifier.align(Alignment.CenterHorizontally)){UiText("Cancel")}
    }
}
@Composable internal fun BreakTimer(until:Long,onEnd:()->Unit) {
    var seconds by remember(until){mutableLongStateOf(((until-System.currentTimeMillis()+999)/1000).coerceAtLeast(0))}
    LaunchedEffect(until) {
        while(seconds>0){delay(1000);seconds=((until-System.currentTimeMillis()+999)/1000).coerceAtLeast(0)}
    }
    ActivityPanel {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(13.dp)) {
            AppGlyph(4,AppStyle.Accent,Modifier.size(32.dp))
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){UiText("Your breather",fontSize=18.sp,fontWeight=FontWeight.SemiBold);UiText("Counting is paused",fontSize=13.sp,color=AppStyle.Muted)}
            UiText("${seconds/60}:${(seconds%60).toString().padStart(2,'0')}",fontSize=27.sp,fontWeight=FontWeight.Bold,color=AppStyle.Accent)
        }
        UiText("A little space away from the feed.",fontSize=13.sp,color=AppStyle.Muted)
        OutlinedButton(onClick=onEnd,modifier=Modifier.fillMaxWidth()){UiText("End break")}
    }
}
