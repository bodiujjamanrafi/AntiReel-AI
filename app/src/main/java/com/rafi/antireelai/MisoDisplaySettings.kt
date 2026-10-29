package com.rafi.antireelai

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView

private enum class DisplayControl(val title:String,val icon:Int) {
    SIZE("Size",8), POSITION("Position",17), MISO("Miso visibility",19), COUNTER("Number visibility",23)
}

/** Native preview stays mounted; only the small slider panel crossfades. No preference writes during drags. */
@Composable internal fun MisoDisplaySettings(store:TrackerStore,talking:Boolean,tap:()->Unit,revision:Int=0) {
    @Suppress("UNUSED_VARIABLE") val refreshed=revision
    var control by rememberSaveable{mutableIntStateOf(0)}
    var size by remember(store.overlaySize){mutableFloatStateOf(store.overlaySize.toFloat())}
    var top by remember(store.overlayTop){mutableFloatStateOf(store.overlayTop.toFloat())}
    var opacity by remember(store.opacity){mutableFloatStateOf(store.opacity)}
    var numberOpacity by remember(store.counterOpacity){mutableFloatStateOf(store.counterOpacity)}
    val reduced=motionReduced(store.reducedMotion)
    val active=DisplayControl.entries.getOrElse(control){DisplayControl.SIZE}
    val fontScale=LocalDensity.current.fontScale
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
        SettingsGroup("Live preview") {
            Box(Modifier.fillMaxWidth().height((208+((fontScale-1f).coerceAtLeast(0f)*48)).dp).semantics{contentDescription="Miso preview: ${size.toInt()} dp, ${top.toInt()} dp from top, ${(opacity*100).toInt()} percent visible"}) {
                Column(Modifier.align(Alignment.Center).graphicsLayer{translationY=((top-100f)*.16f).dp.toPx()},horizontalAlignment=Alignment.CenterHorizontally) {
                    // Fixed layout dimensions avoid remeasuring AndroidView while the size thumb moves.
                    Box(Modifier.size(128.dp),contentAlignment=Alignment.Center) {
                        AndroidView(factory={CompanionView(it)},update={
                            it.animated=!reduced;it.expressive=store.expressions;it.progress=store.total().toFloat()/store.goal.coerceAtLeast(1)
                            it.activity=CompanionActivity.ACTIVE;it.talking=talking
                        },modifier=Modifier.size(128.dp).graphicsLayer{scaleX=size/128f*1.14f;scaleY=scaleX;alpha=opacity})
                    }
                    UiText(if(store.remainingMode)"${(store.goal-store.total()).coerceAtLeast(0)}" else "${store.total()}",fontSize=24.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,modifier=Modifier.graphicsLayer{alpha=numberOpacity})
                    if(store.showPlatform)UiText(platforms.firstOrNull{it.id in store.selected}?.name ?: "Selected app",fontSize=12.sp,color=AppStyle.Muted,modifier=Modifier.graphicsLayer{alpha=numberOpacity})
                }
            }
        }
        Column(Modifier.fillMaxWidth().selectableGroup(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            DisplayControl.entries.chunked(2).forEach {row->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    row.forEach{item->DisplayCategory(item,item==active,reduced,Modifier.weight(1f).fillMaxHeight()){control=item.ordinal;tap()}}
                }
            }
        }
        SettingsGroup {
            Crossfade(targetState=active,animationSpec=tween(if(reduced)0 else 160),label="display slider panel") {item->
                val value=when(item){DisplayControl.SIZE->size;DisplayControl.POSITION->top;DisplayControl.MISO->opacity;DisplayControl.COUNTER->numberOpacity}
                val range=when(item){DisplayControl.SIZE->80f..128f;DisplayControl.POSITION->20f..180f;DisplayControl.MISO->.25f..1f;DisplayControl.COUNTER->.5f..1f}
                SettingsInset {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        SettingsText("Current value")
                        Spacer(Modifier.weight(1f))
                        UiText(if(item==DisplayControl.SIZE || item==DisplayControl.POSITION)"${value.toInt()} dp" else "${(value*100).toInt()}%",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Accent)
                    }
                    DisplaySlider(value,range,item.title,{next->when(item){DisplayControl.SIZE->size=next;DisplayControl.POSITION->top=next;DisplayControl.MISO->opacity=next;DisplayControl.COUNTER->numberOpacity=next}}){
                        when(item){DisplayControl.SIZE->store.overlaySize=size.toInt();DisplayControl.POSITION->store.overlayTop=top.toInt();DisplayControl.MISO->store.opacity=opacity;DisplayControl.COUNTER->store.counterOpacity=numberOpacity};tap()
                    }
                }
            }
        }
        SettingsGroup("Expressions & motion") {
            SettingsPreferenceSwitch(store,"expressions","Expressions","Happy below the limit; angry at your limit.",8){tap();store.expressions=it}
            SettingsRule()
            SettingsPreferenceSwitch(store,"reducedMotion","Reduced motion","Pause Miso’s idle movement in the preview and app.",18,default=false){tap();store.reducedMotion=it}
            SettingsRule()
            SettingsPreferenceSwitch(store,"showPlatform","Platform name","Show the selected app beneath the number.",1){tap();store.showPlatform=it}
            SettingsRule()
            SettingsPreferenceSwitch(store,"remainingMode","Remaining reels","Show your remaining allowance instead of watched reels.",23,default=false){tap();store.remainingMode=it}
        }
    }
}
@Composable private fun DisplayCategory(item:DisplayControl,selected:Boolean,reduced:Boolean,modifier:Modifier,onClick:()->Unit) {
    val indicator=animateFloatAsState(if(selected)1f else 0f,tween(if(reduced)0 else 160),label="category indicator")
    val tint by animateColorAsState(if(selected)AppStyle.Accent else AppStyle.Muted,tween(if(reduced)0 else 160),label="category color")
    val accent=AppStyle.Accent
    Row(modifier.heightIn(min=56.dp).clip(RoundedCornerShape(10.dp)).selectable(selected,role=Role.RadioButton,onClick=onClick)
        .drawBehind{drawLine(accent.copy(alpha=indicator.value),Offset(12.dp.toPx(),size.height-2.dp.toPx()),Offset(12.dp.toPx()+24.dp.toPx()*indicator.value,size.height-2.dp.toPx()),2.dp.toPx(),StrokeCap.Round)}
        .padding(horizontal=12.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        AppGlyph(item.icon,tint,Modifier.size(20.dp))
        UiText(item.title,fontSize=14.sp,lineHeight=20.sp,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Normal,color=if(selected)AppStyle.Text else AppStyle.Muted,modifier=Modifier.weight(1f))
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DisplaySlider(value:Float,range:ClosedFloatingPointRange<Float>,label:String,onValue:(Float)->Unit,onFinish:()->Unit) {
    val accent=AppStyle.Accent
    val inactive=settingsDividerColor(AppStyle.dark)
    val surface=AppStyle.Surface
    val rtl=LocalLayoutDirection.current==LayoutDirection.Rtl
    Slider(value=value,onValueChange=onValue,onValueChangeFinished=onFinish,valueRange=range,
        modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("miso-display-slider").semantics{contentDescription=label},
        thumb={Box(Modifier.size(18.dp).background(accent,CircleShape).border(2.dp,surface,CircleShape))},
        track={state->Canvas(Modifier.fillMaxWidth().height(4.dp)) {
            val fraction=((state.value-range.start)/(range.endInclusive-range.start)).coerceIn(0f,1f)
            val start=if(rtl)size.width else 0f
            val end=if(rtl)0f else size.width
            drawLine(inactive,Offset(start,size.height/2),Offset(end,size.height/2),4.dp.toPx(),StrokeCap.Round)
            drawLine(accent,Offset(start,size.height/2),Offset(start+(end-start)*fraction,size.height/2),4.dp.toPx(),StrokeCap.Round)
        }})
}
