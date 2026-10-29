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

@Composable internal fun PlanReadyDialog(name:String,minutes:Int,reels:Int,language:String,reducedMotion:Boolean,busy:Boolean,onReview:()->Unit,onConfirm:()->Unit) {
    fun tr(en:String,bn:String)=localized(language,en,bn)
    Dialog(onDismissRequest={if(!busy)onReview()}) {
        Surface(shape=RoundedCornerShape(30.dp),color=AppStyle.Surface,contentColor=AppStyle.Text,tonalElevation=0.dp) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)) {
                CelebratingMiso(reducedMotion)
                Text(tr("YOUR DAILY PLAN","তোমার দৈনিক পরিকল্পনা"),fontSize=11.sp,letterSpacing=1.5.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Accent)
                Text(tr("You're all set, $name!","সব প্রস্তুত, $name!"),fontSize=27.sp,lineHeight=34.sp,fontWeight=FontWeight.Bold)
                Row(Modifier.fillMaxWidth().background(AppStyle.Background,RoundedCornerShape(20.dp)).padding(18.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    if(minutes>0)PlanMetric(minutes,tr("minutes daily","মিনিট প্রতিদিন"),24,Modifier.weight(1f))
                    PlanMetric(reels,tr("reels daily","রিলস প্রতিদিন"),23,Modifier.weight(1f))
                }
                Text(tr("Your daily boundaries are ready.","তোমার দৈনিক সীমা প্রস্তুত।"),fontSize=14.sp,lineHeight=22.sp,color=AppStyle.Muted)
                Button(onClick=onConfirm,enabled=!busy,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {
                    Text(tr(if(busy)"Saving…" else "Start My Journey",if(busy)"সংরক্ষণ হচ্ছে…" else "আমার যাত্রা শুরু করি"),fontWeight=FontWeight.SemiBold)
                }
                TextButton(onClick=onReview,enabled=!busy){Text(tr("Review my plan","পরিকল্পনাটি দেখি"))}
            }
        }
    }
}
@Composable private fun PlanMetric(value:Int,label:String,glyph:Int,modifier:Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(5.dp)) {
        AppGlyph(glyph,AppStyle.Accent,Modifier.size(21.dp))
        Text(value.toString(),fontSize=30.sp,fontWeight=FontWeight.Bold)
        Text(label,fontSize=12.sp,color=AppStyle.Muted)
    }
}

