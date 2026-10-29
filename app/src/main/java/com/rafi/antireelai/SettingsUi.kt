package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Scoped to Settings: no global palette or shared Home component is changed. */
@Composable internal fun SettingsTheme(content:@Composable ()->Unit) {
    val colors=MaterialTheme.colorScheme.copy(primary=AppStyle.Accent,onPrimary=if(AppStyle.dark)AppStyle.Background else androidx.compose.ui.graphics.Color.White,
        surface=AppStyle.Surface,onSurface=AppStyle.Text,onSurfaceVariant=AppStyle.Muted,background=AppStyle.Background,onBackground=AppStyle.Text,
        outline=AppStyle.Frame,secondaryContainer=AppStyle.AccentSoft,onSecondaryContainer=AppStyle.Text,
        error=settingsErrorColor(AppStyle.dark),onError=if(AppStyle.dark)AppStyle.Background else Color.White)
    MaterialTheme(colorScheme=colors,content=content)
}
@Composable internal fun SettingsPageHeader(title:String,onBack:()->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        IconButton(onClick=onBack,modifier=Modifier.size(48.dp).semantics{contentDescription="Back to Settings"}){val tint=AppStyle.Text
            Canvas(Modifier.size(24.dp)) {
                drawLine(tint,Offset(size.width*.62f,size.height*.22f),Offset(size.width*.34f,size.height*.5f),2.dp.toPx(),StrokeCap.Round)
                drawLine(tint,Offset(size.width*.34f,size.height*.5f),Offset(size.width*.62f,size.height*.78f),2.dp.toPx(),StrokeCap.Round)
            }}
        UiText(title,fontSize=22.sp,lineHeight=28.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,modifier=Modifier.weight(1f).semantics{heading()})
    }
}
@Composable internal fun SettingsGroup(title:String?=null,content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if(title!=null)UiText(title,fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,modifier=Modifier.padding(start=4.dp).semantics{heading()})
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AppStyle.Surface),content=content)
    }
}
@Composable internal fun SettingsInset(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
internal fun settingsErrorColor(dark:Boolean)=if(dark)Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
internal fun settingsDividerColor(dark:Boolean)=if(dark)Color(0xFF45424C) else Color(0xFFDDD6E0)
@Composable internal fun SettingsRule() {HorizontalDivider(Modifier.padding(horizontal=16.dp),thickness=1.dp,color=settingsDividerColor(AppStyle.dark))}
@Composable internal fun SettingsText(text:String) {UiText(text,fontSize=14.sp,lineHeight=21.sp,color=AppStyle.Muted)}
@Composable internal fun SettingsRow(title:String,glyph:Int,detail:String?=null,onClick:(()->Unit)?=null,trailing:@Composable ()->Unit={if(onClick!=null)UiText("›",fontSize=24.sp,color=AppStyle.Muted)}) {
    Row(Modifier.fillMaxWidth().then(if(onClick!=null)Modifier.clickable(role=Role.Button,onClick=onClick) else Modifier).heightIn(min=56.dp).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        AppGlyph(glyph,AppStyle.Accent,Modifier.size(22.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            UiText(title,fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text)
            if(!detail.isNullOrBlank())SettingsText(detail)
        }
        trailing()
    }
}
@Composable internal fun SettingsSwitch(title:String,detail:String,glyph:Int,checked:Boolean,modifier:Modifier=Modifier,onChange:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().then(modifier).toggleable(checked,role=Role.Switch,onValueChange=onChange).heightIn(min=64.dp).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
        AppGlyph(glyph,AppStyle.Accent,Modifier.size(22.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            UiText(title,fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text)
            SettingsText(detail)
        }
        SettingsSwitchThumb(checked)
    }
}
/** Standard switch proportions with a deterministic, draw-phase 150ms thumb transition. */
@Composable private fun SettingsSwitchThumb(checked:Boolean) {
    val reduced=LocalMotionReduced.current || !android.animation.ValueAnimator.areAnimatorsEnabled()
    val progress=animateFloatAsState(if(checked)1f else 0f,tween(if(reduced)0 else 150),label="setting switch")
    val accent=AppStyle.Accent
    val surface=AppStyle.Surface
    val muted=AppStyle.Muted
    val onAccent=MaterialTheme.colorScheme.onPrimary
    val line=settingsDividerColor(AppStyle.dark)
    val rtl=androidx.compose.ui.platform.LocalLayoutDirection.current==androidx.compose.ui.unit.LayoutDirection.Rtl
    Canvas(Modifier.size(52.dp,32.dp)) {
        val value=progress.value
        val track=androidx.compose.ui.graphics.lerp(surface,accent,value)
        drawRoundRect(track,cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height/2))
        drawRoundRect(androidx.compose.ui.graphics.lerp(line,accent,value),cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height/2),style=androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
        val fraction=if(rtl)1f-value else value
        drawCircle(androidx.compose.ui.graphics.lerp(muted,onAccent,value),(9f+3f*value).dp.toPx(),Offset(16.dp.toPx()+20.dp.toPx()*fraction,size.height/2))
    }
}
@Composable internal fun SettingsAction(label:String,onClick:()->Unit,enabled:Boolean=true,primary:Boolean=false,destructive:Boolean=false) {
    val modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)
    val shape=RoundedCornerShape(12.dp)
    if(primary)Button(onClick=onClick,enabled=enabled,modifier=modifier,shape=shape){UiText(label,fontSize=15.sp,fontWeight=FontWeight.SemiBold)}
    else TextButton(onClick=onClick,enabled=enabled,modifier=modifier.background(if(destructive)MaterialTheme.colorScheme.error.copy(alpha=.07f) else AppStyle.AccentSoft.copy(alpha=.4f),shape),shape=shape,colors=ButtonDefaults.textButtonColors(contentColor=if(destructive)MaterialTheme.colorScheme.error else AppStyle.Accent)){UiText(label,fontSize=15.sp,fontWeight=FontWeight.Medium)}
}
@Composable internal fun SettingsStats(first:Triple<String,String,Int>,second:Triple<String,String,Int>) {
    if(LocalDensity.current.fontScale>1.4f)Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        SettingsStat(first,Modifier.fillMaxWidth());SettingsStat(second,Modifier.fillMaxWidth())
    } else Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        SettingsStat(first,Modifier.weight(1f).fillMaxHeight());SettingsStat(second,Modifier.weight(1f).fillMaxHeight())
    }
}
@Composable private fun SettingsStat(data:Triple<String,String,Int>,modifier:Modifier) {
    val (value,label,glyph)=data
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(AppStyle.Surface).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        AppGlyph(glyph,AppStyle.Accent,Modifier.size(22.dp))
        UiText(value,fontSize=24.sp,lineHeight=30.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
        SettingsText(label)
    }
}
@Composable internal fun SettingsFaq(title:String,glyph:Int=11,content:@Composable ColumnScope.()->Unit) {
    var expanded by rememberSaveable(title){mutableStateOf(false)}
    Column {
        SettingsRow(title,glyph,onClick={expanded=!expanded},trailing={UiText(if(expanded)"−" else "+",fontSize=22.sp,color=AppStyle.Muted,modifier=Modifier.semantics{stateDescription=if(expanded)"Expanded" else "Collapsed"})})
        val reduced=LocalMotionReduced.current
        AnimatedVisibility(expanded,enter=if(reduced)EnterTransition.None else expandVertically(tween(200))+fadeIn(tween(160)),exit=if(reduced)ExitTransition.None else shrinkVertically(tween(180))+fadeOut(tween(120))) {SettingsInset(content)}
    }
}
