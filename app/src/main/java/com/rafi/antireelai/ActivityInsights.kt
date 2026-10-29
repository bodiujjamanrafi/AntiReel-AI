package com.rafi.antireelai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class DailyInsight(val detail:String,val advice:String)
internal fun dailyInsight(day:ActivityDay,previous:ActivityDay?,goal:Int):DailyInsight {
    if(day.totalMillis==0L && day.totalReels==0)return DailyInsight("No activity recorded for this day yet.","Start Miso before opening a selected short-video app to build your picture.")
    val top=day.millis.indices.maxByOrNull{day.millis[it]}
    val share=top?.takeIf{day.totalMillis>0}?.let{day.millis[it].toDouble()/day.totalMillis}
    val lead=if(top!=null && share!=null) "${platforms[top].name} accounts for ${kotlin.math.round(share*100).toInt()}% of this day's recorded video time." else "${day.totalReels} reels recorded. No video-screen duration has been recorded for this day."
    val comparison=if(previous!=null && previous.totalMillis>0 && day.totalMillis>0) " ${comparisonLabel(day.totalMillis,previous.totalMillis)} than the previous full day${if(day.date==java.time.LocalDate.now()) "; today is still in progress" else ""}." else ""
    val advice=when {
        day.totalReels>=goal->"Your daily boundary is reached. Give yourself a break before opening another feed."
        day.totalReels.toLong()*5>=goal.toLong()*4->"You're close to your boundary. Save the remaining ${goal-day.totalReels} reels for a deliberate choice."
        top!=null && share!=null && share>=.6->"Before opening ${platforms[top].name} again, try a short break away from the screen."
        else->"A small pause between feeds can help you choose what deserves your next moment."
    }
    return DailyInsight(lead+comparison,advice)
}
@Composable internal fun ActivityInsight(day:ActivityDay,previous:ActivityDay?,goal:Int) {
    val insight=dailyInsight(day,previous,goal)
    ActivityPanel {
        UiText("A little perspective",fontSize=20.sp,fontWeight=FontWeight.SemiBold)
        UiText(insight.detail,fontSize=14.sp,lineHeight=22.sp,color=AppStyle.Muted)
        UiText(insight.advice,fontSize=14.sp,lineHeight=22.sp,color=AppStyle.Text,fontWeight=FontWeight.Medium)
    }
}
