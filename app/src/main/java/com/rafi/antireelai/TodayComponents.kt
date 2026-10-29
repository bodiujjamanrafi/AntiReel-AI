package com.rafi.antireelai

import android.graphics.BitmapFactory
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

private val BrandCache=ConcurrentHashMap<String,ImageBitmap>()
@Composable internal fun PlatformBrand(platform:Platform,modifier:Modifier=Modifier) {
    val context=LocalContext.current
    val bitmap by produceState(BrandCache[platform.id],platform.id) {
        value=withContext(Dispatchers.IO) {
            BrandCache[platform.id] ?: (platform.packages.firstNotNullOfOrNull {pkg->runCatching{context.packageManager.getApplicationIcon(pkg).toBitmap(96,96).asImageBitmap()}.getOrNull()}
                ?: runCatching {context.assets.open("platform-icons/${platform.id}.png").use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=4})?.asImageBitmap()}}.getOrNull())?.also{BrandCache[platform.id]=it}
        }
    }
    val icon=bitmap
    Box(modifier,contentAlignment=Alignment.Center) {
        if(icon!=null)Image(icon,contentDescription=platform.name,modifier=Modifier.fillMaxSize())
        else if(platform.id=="tiktok")TikTokBrandIcon(Modifier.fillMaxSize())
        else UiText(platform.name.take(1),color=AppStyle.Accent,fontWeight=FontWeight.Bold)
    }
}

@Composable internal fun TodayHeadline(miso:@Composable ()->Unit) {
    ReferenceHero("","Your attention,","reclaimed.","",miso=miso)
}
@Composable internal fun TodayMetrics(total:Int,goal:Int,usageMillis:Long=0,minutes:Int=0) {
    val used=total.toFloat()/goal.coerceAtLeast(1)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(13.dp)) {
        CircularMetric("Reels today","$total","Estimated views",used,AppStyle.Teal,Modifier.weight(1f),23)
        CircularMetric("Daily allowance",if(minutes>0)durationLabel((minutes*60000L-usageMillis).coerceAtLeast(0)) else "${(goal-total).coerceAtLeast(0)}",if(minutes>0)"or ${(goal-total).coerceAtLeast(0)} reels left" else "remaining of $goal",1f-dailyAllowanceProgress(total,goal,usageMillis,minutes),AppStyle.Blue,Modifier.weight(1f),24)
    }
}
@Composable private fun CircularMetric(label:String,value:String,detail:String,progress:Float,tint:Color,modifier:Modifier,glyph:Int) {
    Column(modifier.padding(horizontal=10.dp,vertical=16.dp)
        .semantics{contentDescription="$label, $value, $detail"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            AppGlyph(glyph,tint,Modifier.size(18.dp))
            UiText(label,fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,maxLines=1)
        }
        Box(Modifier.sizeIn(maxWidth=136.dp).fillMaxWidth().aspectRatio(1f),contentAlignment=Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val inset=7.dp.toPx();val extent=androidx.compose.ui.geometry.Size(size.width-inset*2,size.height-inset*2)
                val stroke=Stroke(7.dp.toPx(),cap=StrokeCap.Round)
                drawArc(tint.copy(alpha=.12f),-90f,360f,false,Offset(inset,inset),extent,style=stroke)
                if(progress>0)drawArc(tint,-90f,360f*progress.coerceIn(0f,1f),false,Offset(inset,inset),extent,style=stroke)
                for(i in 0..11) {
                    val angle=i*Math.PI/6
                    val r=size.minDimension*.36f
                    drawCircle(tint.copy(alpha=.22f),1.dp.toPx(),Offset(center.x+kotlin.math.cos(angle).toFloat()*r,center.y+kotlin.math.sin(angle).toFloat()*r))
                }
            }
            UiText(value,fontSize=when{value.length>5->24.sp;value.length>3->32.sp;else->42.sp},fontWeight=FontWeight.Bold,color=AppStyle.Text,letterSpacing=(-1).sp,maxLines=1)
        }
        UiText(detail,fontSize=12.sp,color=AppStyle.Muted,maxLines=1)
    }
}
@Composable internal fun SelectedApps(selected:List<Platform>,counts:List<Int>,onManage:()->Unit,onOpen:(Platform)->Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        ReferenceSectionTitle("Selected apps","Manage  →",onAction=onManage)
        if(selected.isEmpty())OutlinedButton(onClick=onManage,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),shape=RoundedCornerShape(12.dp),border=BorderStroke(1.dp,AppStyle.Frame),colors=ButtonDefaults.outlinedButtonColors(contentColor=AppStyle.Text)) {
            AppGlyph(1,AppStyle.Teal,Modifier.size(23.dp));Spacer(Modifier.width(12.dp));UiText("Choose your platforms",fontWeight=FontWeight.SemiBold,fontSize=15.sp)
        }
        else Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
            selected.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    row.forEach {p->
                        val count=counts.getOrElse(platforms.indexOf(p)){0}
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable{onOpen(p)}.padding(vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            PlatformBrand(p,Modifier.size(46.dp))
                            UiText(p.name,fontSize=12.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            UiText("$count reels",fontSize=11.sp,color=AppStyle.Muted,maxLines=1)
                        }
                    }
                    repeat(4-row.size){Spacer(Modifier.weight(1f))}
                }
            }
        }
    }
}
/** Seven real local days; each cell opens the matching Activity breakdown. */
@Composable internal fun RecentDays(history:List<ActivityDay>,onDay:(java.time.LocalDate)->Unit) {
    val days=history.take(7).reversed()
    val maximum=days.maxOfOrNull{it.totalReels}?.coerceAtLeast(1) ?:1
    Column(verticalArrangement=Arrangement.spacedBy(14.dp)) {
        ReferenceSectionTitle("Your week, at a glance")
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            UiText("${days.sumOf{it.totalReels}} reels",fontSize=22.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
            UiText(durationLabel(days.sumOf{it.totalMillis}),fontSize=15.sp,color=AppStyle.Muted)
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            days.forEach {day->
                val today=day.date==java.time.LocalDate.now()
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable{onDay(day.date)}.semantics{contentDescription="${day.date}, ${day.totalReels} reels, ${durationLabel(day.totalMillis)}. View activity."}.padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(9.dp)) {
                    UiText("${day.totalReels}",fontSize=12.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text,maxLines=1)
                    Box(Modifier.height(64.dp).fillMaxWidth(),contentAlignment=Alignment.BottomCenter) {
                        Box(Modifier.width(17.dp).fillMaxHeight().background(AppStyle.Frame.copy(alpha=.15f),RoundedCornerShape(6.dp)))
                        if(day.totalReels>0)Box(Modifier.width(17.dp).height((4+60f*day.totalReels/maximum).dp).background(if(today)AppStyle.Teal else AppStyle.Teal.copy(alpha=.4f),RoundedCornerShape(6.dp)))
                    }
                    UiText(day.date.format(java.time.format.DateTimeFormatter.ofPattern("EEEEE",java.util.Locale.forLanguageTag(LocalUiLanguage.current))),fontSize=12.sp,fontWeight=if(today)FontWeight.Bold else FontWeight.Normal,color=if(today)AppStyle.Teal else AppStyle.Muted)
                }
            }
        }
        if(days.isEmpty())UiText("Your recorded days will appear here.",fontSize=13.sp,color=AppStyle.Muted)
    }
}
