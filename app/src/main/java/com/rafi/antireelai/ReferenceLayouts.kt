package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun ReferencePanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().border(1.dp,AppStyle.Frame,RoundedCornerShape(22.dp)),content=content)
}
@Composable internal fun ReferenceHero(kicker:String,title:String,accent:String,description:String,compact:Boolean=false,miso:@Composable ()->Unit) {
    val language=LocalUiLanguage.current
    BoxWithConstraints(Modifier.fillMaxWidth().height(if(compact)188.dp else 218.dp)) {
        val art=if(maxWidth<340.dp)202.dp else 222.dp
        Box(Modifier.align(Alignment.BottomEnd).size(art),contentAlignment=Alignment.Center){miso()}
        Column(Modifier.align(Alignment.TopStart).padding(top=if(compact)12.dp else 18.dp),verticalArrangement=Arrangement.spacedBy(if(compact)10.dp else 12.dp)) {
            if(kicker.isNotBlank())UiText(kicker,fontSize=9.sp,letterSpacing=1.6.sp,fontWeight=FontWeight.Medium,color=AppStyle.Muted)
            androidx.compose.material3.Text(buildAnnotatedString{append(UiCopy.translate(title,language));if(accent.isNotEmpty()){append('\n');withStyle(SpanStyle(color=AppStyle.Accent)){append(UiCopy.translate(accent,language))}}},fontSize=if(compact)27.sp else 29.sp,lineHeight=33.sp,fontWeight=FontWeight.Bold,letterSpacing=(-.9).sp,color=AppStyle.Text,modifier=Modifier.fillMaxWidth(if(compact).62f else .83f))
            if(description.isNotBlank())UiText(description,fontSize=12.sp,lineHeight=19.sp,color=AppStyle.Muted,modifier=Modifier.fillMaxWidth(.57f))
        }
    }
}
@Composable internal fun ReferenceSectionTitle(title:String,action:String?=null,tint:Color=AppStyle.Accent,onAction:()->Unit={}) {
    Row(Modifier.fillMaxWidth().padding(vertical=2.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Box(Modifier.width(3.dp).height(14.dp).background(tint,CircleShape))
        UiText(title,modifier=Modifier.weight(1f),fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
        if(action!=null)TextButton(onClick=onAction,contentPadding=PaddingValues(horizontal=0.dp)){UiText(action,fontSize=11.sp,color=AppStyle.Accent)}
    }
}
/** Clip the interaction to the circle. No rectangular indication is drawn. */
@Composable internal fun ReferenceRoundAction(label:String,glyph:Int,tint:Color,enabled:Boolean=true,onClick:()->Unit) {
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val scale=animateFloatAsState(if(pressed).94f else 1f,tween(90),label="round action press")
    Column(Modifier.widthIn(min=72.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Box(Modifier.size(50.dp).graphicsLayer{scaleX=scale.value;scaleY=scale.value}.clip(CircleShape).background(tint.copy(alpha=if(enabled).12f else .03f)).border(1.dp,if(enabled)tint.copy(alpha=.5f) else AppStyle.Frame.copy(alpha=.4f),CircleShape)
            .clickable(interactionSource=interaction,indication=null,enabled=enabled,role=Role.Button,onClick=onClick),contentAlignment=Alignment.Center) {
            AppGlyph(glyph,if(enabled)tint else AppStyle.Muted,Modifier.size(24.dp))
        }
        UiText(label,fontSize=12.sp,color=if(enabled)AppStyle.Text else AppStyle.Muted,fontWeight=FontWeight.Medium,maxLines=1)
    }
}
internal enum class UtilityIcon { FOCUS, BREAK, GOAL, COUNTER }

@Composable internal fun ReferenceQuickAction(title:String,icon:UtilityIcon,tint:Color,modifier:Modifier=Modifier,onClick:()->Unit) {
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    val surface by androidx.compose.animation.animateColorAsState(
        if(pressed) androidx.compose.ui.graphics.lerp(AppStyle.Surface,tint,.10f) else AppStyle.Surface,
        tween(if(motionReduced())0 else 120),label="utility press")
    Row(modifier.heightIn(min=84.dp).clip(RoundedCornerShape(12.dp)).background(surface)
        .clickable(interactionSource=interaction,indication=androidx.compose.material3.ripple(),role=Role.Button,onClick=onClick)
        .padding(horizontal=12.dp,vertical=16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        UtilityVector(icon,tint,Modifier.size(24.dp))
        UiText(title,modifier=Modifier.weight(1f),fontSize=14.sp,lineHeight=19.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text)
    }
}

/** A cohesive 24dp vector set; every symbol uses the same optical box and 1.7dp stroke. */
@Composable private fun UtilityVector(icon:UtilityIcon,color:Color,modifier:Modifier) {
    Canvas(modifier) {
        val unit=size.minDimension/24f
        val stroke=androidx.compose.ui.graphics.drawscope.Stroke(1.7f*unit,cap=StrokeCap.Round,join=StrokeJoin.Round)
        fun line(x:Float,y:Float,x2:Float,y2:Float)=drawLine(color,Offset(x*unit,y*unit),Offset(x2*unit,y2*unit),stroke.width,StrokeCap.Round)
        when(icon) {
            UtilityIcon.FOCUS -> {
                val path=Path().apply {
                    moveTo(8*unit,3*unit);lineTo(3*unit,3*unit);lineTo(3*unit,8*unit)
                    moveTo(16*unit,3*unit);lineTo(21*unit,3*unit);lineTo(21*unit,8*unit)
                    moveTo(3*unit,16*unit);lineTo(3*unit,21*unit);lineTo(8*unit,21*unit)
                    moveTo(16*unit,21*unit);lineTo(21*unit,21*unit);lineTo(21*unit,16*unit)
                }
                drawPath(path,color,style=stroke);drawCircle(color,3*unit,Offset(12*unit,12*unit),style=stroke)
            }
            UtilityIcon.BREAK -> {
                val path=Path().apply {
                    moveTo(4*unit,8*unit);lineTo(17*unit,8*unit);lineTo(17*unit,14*unit)
                    quadraticTo(17*unit,18*unit,13*unit,18*unit);lineTo(8*unit,18*unit)
                    quadraticTo(4*unit,18*unit,4*unit,14*unit);close()
                    moveTo(17*unit,9*unit);cubicTo(23*unit,8*unit,23*unit,16*unit,17*unit,15*unit)
                }
                drawPath(path,color,style=stroke);line(3f,21f,19f,21f);line(8f,3f,8f,5f);line(13f,3f,13f,5f)
            }
            UtilityIcon.GOAL -> {
                drawCircle(color,8*unit,Offset(11*unit,13*unit),style=stroke)
                drawCircle(color,4*unit,Offset(11*unit,13*unit),style=stroke)
                line(11f,13f,21f,3f);line(16f,3f,21f,3f);line(21f,3f,21f,8f)
            }
            UtilityIcon.COUNTER -> {
                for(i in 0..2) {
                    val y=5f+7f*i;val x=if(i==1)15f else 9f
                    line(3f,y,x-3,y);line(x+3,y,21f,y)
                    drawCircle(color,2.5f*unit,Offset(x*unit,y*unit),style=stroke)
                }
            }
        }
    }
}
