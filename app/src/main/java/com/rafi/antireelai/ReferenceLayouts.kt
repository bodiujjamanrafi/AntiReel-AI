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
@Composable internal fun ReferenceQuickAction(title:String,glyph:Int,tint:Color,modifier:Modifier=Modifier,onClick:()->Unit) {
    Column(modifier.heightIn(min=100.dp).clip(RoundedCornerShape(14.dp)).background(AppStyle.Surface).border(.7f.dp,AppStyle.Frame,RoundedCornerShape(14.dp))
        .clickable(role=Role.Button,onClick=onClick).padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        AppGlyph(glyph,tint,Modifier.size(27.dp))
        UiText(title,fontSize=15.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text,maxLines=1)
    }
}
