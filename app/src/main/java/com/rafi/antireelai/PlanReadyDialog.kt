package com.rafi.antireelai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView

@Composable internal fun PlanReadyDialog(name:String,minutes:Int,reels:Int,language:String,reducedMotion:Boolean,busy:Boolean,onReview:()->Unit,onConfirm:()->Unit) {
    fun tr(en:String,bn:String)=localized(language,en,bn)
    Dialog(onDismissRequest={if(!busy)onReview()},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(modifier=Modifier.padding(horizontal=24.dp).widthIn(max=400.dp).fillMaxWidth(),
            shape=RoundedCornerShape(24.dp),color=AppStyle.Surface,contentColor=AppStyle.Text,tonalElevation=0.dp) {
            Column(Modifier.heightIn(max=600.dp).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    AndroidView(factory={CompanionView(it)},update={it.animated=!reducedMotion;it.pose=MisoPose.CELEBRATING;it.contentDescription="Miso celebrates your plan"},modifier=Modifier.size(80.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text(tr("YOUR DAILY PLAN","তোমার দৈনিক পরিকল্পনা"),fontSize=11.sp,letterSpacing=.8.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Accent)
                        Text(tr("You're all set, $name!","সব প্রস্তুত, $name!"),fontSize=22.sp,lineHeight=28.sp,fontWeight=FontWeight.Bold)
                    }
                }
                Row(Modifier.fillMaxWidth().background(AppStyle.Background,RoundedCornerShape(16.dp)).padding(16.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    if(minutes>0)PlanMetric(minutes,tr("minutes daily","মিনিট প্রতিদিন"),24,Modifier.weight(1f))
                    PlanMetric(reels,tr("reels daily","রিলস প্রতিদিন"),23,Modifier.weight(1f))
                }
                Text(tr("Your daily boundaries are ready.","তোমার দৈনিক সীমা প্রস্তুত।"),fontSize=14.sp,lineHeight=20.sp,color=AppStyle.Muted)
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Button(onClick=onConfirm,enabled=!busy,shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)) {
                        Text(tr(if(busy)"Saving…" else "Start My Journey",if(busy)"সংরক্ষণ হচ্ছে…" else "আমার যাত্রা শুরু করি"),fontWeight=FontWeight.SemiBold)
                    }
                    TextButton(onClick=onReview,enabled=!busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(tr("Review my plan","পরিকল্পনাটি দেখি"))}
                }
            }
        }
    }
}

@Composable private fun PlanMetric(value:Int,label:String,glyph:Int,modifier:Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            AppGlyph(glyph,AppStyle.Accent,Modifier.size(20.dp))
            Text(value.toString(),fontSize=28.sp,fontWeight=FontWeight.Bold)
        }
        Text(label,fontSize=12.sp,color=AppStyle.Muted)
    }
}

