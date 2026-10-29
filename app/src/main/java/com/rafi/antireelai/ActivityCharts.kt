package com.rafi.antireelai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal data class ActivityDay(val date:LocalDate,val reels:List<Int>,val millis:List<Long>) {
    val totalReels=reels.sum()
    val totalMillis=millis.sum()
    companion object {
        fun read(date:LocalDate,values:Map<String,*>)=ActivityDay(date,platforms.map{(values["count:$date:${it.id}"] as? Int ?:0).coerceAtLeast(0)},platforms.map{(values["usage:$date:${it.id}"] as? Long ?:0L).coerceAtLeast(0)})
    }
}
internal fun comparisonLabel(current:Long,previous:Long):String = when {
    previous<=0 -> if(current<=0) "No recorded time yet" else "No previous-day baseline"
    current==previous -> "Same recorded time"
    current>previous -> "${durationLabel(current-previous)} more"
    else -> "${durationLabel(previous-current)} less"
}
internal fun durationLabel(ms:Long):String {
    val seconds=ms.coerceAtLeast(0)/1000
    return when {seconds>=3600->"${seconds/3600}h ${(seconds%3600)/60}m";seconds>=60->"${seconds/60}m ${seconds%60}s";else->"${seconds}s"}
}
internal fun usageStreak(today:LocalDate,active:(LocalDate)->Boolean):Int {
    var day=if(active(today)) today else today.minusDays(1)
    var days=0
    while(days<30 && active(day)) {days++;day=day.minusDays(1)}
    return days
}
@Composable internal fun ActivityPanel(content:@Composable ColumnScope.()->Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).border(1.dp,AppStyle.Frame,RoundedCornerShape(24.dp)).padding(19.dp),verticalArrangement=Arrangement.spacedBy(15.dp),content=content)
}
/** Windows are anchored to today, with the oldest partial window limited to retained data. */
internal fun historyWindow(today:LocalDate,selected:LocalDate,retainedDays:Int=30):List<LocalDate> {
    val offset=java.time.temporal.ChronoUnit.DAYS.between(selected,today).coerceIn(0,(retainedDays-1).coerceAtLeast(0).toLong()).toInt()
    val end=today.minusDays((offset/7*7).toLong())
    return (6 downTo 0).map{end.minusDays(it.toLong())}.filter{!it.isBefore(today.minusDays((retainedDays-1).toLong()))}
}
@Composable internal fun UsageWeekChart(history:List<ActivityDay>,day:LocalDate,onSelect:(LocalDate)->Unit) {
    val today=LocalDate.now()
    val days=remember(today,day){historyWindow(today,day)}
    var timeMode by rememberSaveable{mutableStateOf(true)}
    val byDate=remember(history){history.associateBy{it.date}}
    val maximum=days.maxOfOrNull{date->byDate[date]?.let{if(timeMode)it.totalMillis.toDouble() else it.totalReels.toDouble()} ?:0.0}?.coerceAtLeast(1.0) ?:1.0
    val earliest=history.minOfOrNull{it.date} ?:today
    ActivityPanel {
        UiText("Daily history",fontSize=20.sp,fontWeight=FontWeight.SemiBold)
        UiText("Daily totals across every platform",fontSize=12.sp,color=AppStyle.Muted)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={onSelect(days.first().minusDays(1).coerceAtLeast(earliest))},enabled=days.first()>earliest,modifier=Modifier.semantics{contentDescription="Previous seven days"}){UiText("‹",fontSize=28.sp)}
            UiText("${days.first().format(DateTimeFormatter.ofPattern("d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current)))} – ${days.last().format(DateTimeFormatter.ofPattern("d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current)))}",fontSize=13.sp,fontWeight=FontWeight.Medium,modifier=Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick={onSelect(days.last().plusDays(7).coerceAtMost(today))},enabled=days.last()<today,modifier=Modifier.semantics{contentDescription="Next seven days"}){UiText("›",fontSize=28.sp)}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {FilterChip(timeMode,{timeMode=true},label={UiText("Time")});FilterChip(!timeMode,{timeMode=false},label={UiText("Reels")})}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.Bottom) {
            days.forEach {date->
                val data=byDate[date];val count=data?.totalReels ?:0;val millis=data?.totalMillis ?:0L
                val value=if(timeMode)millis.toDouble() else count.toDouble();val active=date==day
                Column(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if(active)AppStyle.Teal.copy(alpha=.12f) else Color.Transparent).clickable{onSelect(date)}.semantics{selected=active;contentDescription="$date, $count reels, ${durationLabel(millis)}"}.padding(vertical=8.dp,horizontal=2.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                    UiText(if(timeMode)durationLabel(millis) else "$count",fontSize=9.sp,color=AppStyle.Muted,maxLines=1)
                    Box(Modifier.fillMaxWidth().height(95.dp),contentAlignment=Alignment.BottomCenter){if(value>0)Box(Modifier.widthIn(max=26.dp).fillMaxWidth().height((value/maximum*90).toFloat().coerceAtLeast(2f).dp).clip(RoundedCornerShape(7.dp)).background(if(active)AppStyle.Teal else AppStyle.Teal.copy(alpha=.4f)))}
                    UiText(date.format(DateTimeFormatter.ofPattern("EEEEE",java.util.Locale.forLanguageTag(LocalUiLanguage.current))),fontSize=11.sp,color=AppStyle.Text)
                    UiText("${date.dayOfMonth}",fontSize=10.sp,color=AppStyle.Muted)
                }
            }
            repeat(7-days.size){Spacer(Modifier.weight(1f))}
        }
        UiText("Tap a day to see its platform breakdown below. History is kept for 30 days.",fontSize=12.sp,lineHeight=18.sp,color=AppStyle.Muted)
    }
}
private val LightUsageColors=listOf(0xFF8050E7,0xFF1ABF98,0xFF288CB9,0xFFBC6A29,0xFF8054BD,0xFFBE3C70,0xFF607B32,0xFF2983B3,0xFFC44C39,0xFF626184).map {Color(it)}
private val DarkUsageColors=listOf(0xFFC0A2FF,0xFF71E1C2,0xFF86D5F1,0xFFF8BD80,0xFFD0AEF1,0xFFFFA3C5,0xFFBDD98A,0xFF93D5FF,0xFFFFA797,0xFFBEBBEB).map{Color(it)}
private val UsageColors get()=if(AppStyle.dark)DarkUsageColors else LightUsageColors
/** Labels use separated side lanes so tiny slices never overlap or disappear. */
internal data class UsageShare(val index:Int,val fraction:Double,val middleDegrees:Float)
internal fun usageShares(values:List<Double>):List<UsageShare> {
    val safe=values.map{if(it.isFinite())it.coerceAtLeast(0.0) else 0.0}
    val sum=safe.sum()
    if(sum<=0)return emptyList()
    var angle=-90f
    return safe.mapIndexedNotNull{i,value->
        val sweep=(value/sum*360).toFloat()
        val result=if(value>0)UsageShare(i,value/sum,angle+sweep/2) else null
        angle+=sweep;result
    }
}
private data class DonutLabel(val index:Int,val tip:androidx.compose.ui.geometry.Offset,val elbow:androidx.compose.ui.geometry.Offset,val edge:Float,val y:Float,val right:Boolean,val name:String,val percentage:String)
@Composable internal fun UsageDonut(data:ActivityDay) {
    var showTime by rememberSaveable {mutableStateOf(true)}
    val values=remember(data,showTime){platforms.indices.map {if(showTime)data.millis[it].toDouble() else data.reels[it].toDouble()}}
    val shares=remember(values){usageShares(values)}
    val sum=values.sum()
    val description=if(showTime) "Time in recognized short-video screens" else "Share of estimated reels"
    val colors=UsageColors
    val labelColor=AppStyle.Text
    val mutedColor=AppStyle.Muted
    val emptyColor=AppStyle.Frame
    val fontScale=androidx.compose.ui.platform.LocalDensity.current.fontScale.coerceAtMost(1.4f)
    val accessible=shares.joinToString("; "){"${platforms[it.index].name}: ${String.format(java.util.Locale.getDefault(),"%.1f%%",it.fraction*100)}, ${if(showTime)durationLabel(values[it.index].toLong()) else "${values[it.index].toInt()} reels"}"}
    ActivityPanel {
        UiText("Where your attention goes",fontSize=19.sp,fontWeight=FontWeight.SemiBold)
        UiText("${data.date.format(DateTimeFormatter.ofPattern("d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current)))} · recorded usage",fontSize=12.sp,color=AppStyle.Muted)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(showTime,{showTime=true},label={UiText("Time")});FilterChip(!showTime,{showTime=false},label={UiText("Reels")})}
        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
            Box(Modifier.fillMaxWidth().height(290.dp).semantics{contentDescription="$description. $accessible"}.drawWithCache {
                val center=androidx.compose.ui.geometry.Offset(size.width/2f,size.height/2f)
                val radius=size.width*.245f
                val thickness=19.dp.toPx()
                val origin=androidx.compose.ui.geometry.Offset(center.x-radius,center.y-radius)
                val extent=androidx.compose.ui.geometry.Size(radius*2,radius*2)
                val stroke=Stroke(thickness,cap=StrokeCap.Butt)
                val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize=10.sp.toPx().div(fontScale).times(fontScale.coerceAtMost(1.15f))
                    typeface=android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL)
                }
                val textPaint=android.text.TextPaint(paint)
                val labels=ArrayList<DonutLabel>(shares.size)
                for(right in listOf(false,true)) {
                    val lane=shares.filter{(kotlin.math.cos(Math.toRadians(it.middleDegrees.toDouble()))>=0)==right}
                        .sortedBy{kotlin.math.sin(Math.toRadians(it.middleDegrees.toDouble()))}
                    lane.forEachIndexed{row,share->
                        val angle=Math.toRadians(share.middleDegrees.toDouble())
                        val tip=androidx.compose.ui.geometry.Offset(center.x+kotlin.math.cos(angle).toFloat()*(radius+thickness/2),center.y+kotlin.math.sin(angle).toFloat()*(radius+thickness/2))
                        val y=if(lane.size==1)center.y else 28.dp.toPx()+row*(size.height-56.dp.toPx())/(lane.size-1)
                        val edge=if(right)size.width-3.dp.toPx() else 3.dp.toPx()
                        val elbow=if(right)size.width*.79f else size.width*.21f
                        labels.add(DonutLabel(share.index,tip,androidx.compose.ui.geometry.Offset(elbow,y),edge,y,right,
                            android.text.TextUtils.ellipsize(platforms[share.index].name,textPaint,size.width*.205f,android.text.TextUtils.TruncateAt.END).toString(),
                            String.format(java.util.Locale.getDefault(),"%.1f%%",share.fraction*100)))
                    }
                }
                onDrawBehind {
                    if(shares.isEmpty())drawArc(emptyColor.copy(alpha=.3f),0f,360f,false,origin,extent,style=stroke)
                    shares.forEach{share->
                        val sweep=(share.fraction*360).toFloat();val gap=minOf(2f,sweep*.15f)
                        drawArc(colors[share.index],share.middleDegrees-sweep/2+gap/2,sweep-gap,false,origin,extent,style=stroke)
                    }
                    labels.forEach{label->
                        drawLine(colors[label.index].copy(alpha=.65f),label.tip,label.elbow,1.dp.toPx())
                        drawCircle(colors[label.index],2.dp.toPx(),label.tip)
                        paint.textAlign=if(label.right)android.graphics.Paint.Align.RIGHT else android.graphics.Paint.Align.LEFT
                        paint.color=labelColor.toArgb()
                        drawContext.canvas.nativeCanvas.drawText(label.name,label.edge,label.y-5.dp.toPx(),paint)
                        paint.color=colors[label.index].toArgb()
                        drawContext.canvas.nativeCanvas.drawText(label.percentage,label.edge,label.y+10.dp.toPx(),paint)
                    }
                }
            })
            Column(Modifier.widthIn(max=130.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                UiText(if(showTime)durationLabel(sum.toLong()) else "${sum.toInt()}",fontSize=23.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,maxLines=1)
                UiText(if(sum==0.0)"No data yet" else if(showTime)"tracked time" else "estimated reels",fontSize=10.sp,color=AppStyle.Muted)
            }
        }
        UiText(if(sum==0.0)"Start Miso and watch a selected short-video feed. Your recorded usage appears here when you return." else "$description. Updated from Miso’s local records; this is not total app screen time.",fontSize=12.sp,lineHeight=18.sp,color=mutedColor)
    }
}

/** Every platform with recorded time appears; legend toggles never alter stored data. */
@Composable internal fun UsageTrendChart(history:List<ActivityDay>,reducedMotion:Boolean) {
    val series=history.take(7).reversed()
    var focusedDate by rememberSaveable {mutableStateOf(series.lastOrNull()?.date?.toString() ?: "")}
    val focused=series.indexOfFirst{it.date.toString()==focusedDate}.takeIf{it>=0} ?: (series.size-1).coerceAtLeast(0)
    val focusedDay=series.getOrNull(focused)
    val active=platforms.indices.filter {i->series.any{it.millis[i]>0}}
    var hidden by remember {mutableStateOf(setOf<Int>())}
    val shown=active.filter{it !in hidden}
    val top=series.maxOfOrNull{day->shown.maxOfOrNull{day.millis[it]} ?:0L}?.coerceAtLeast(1000L) ?:1000L
    ActivityPanel {
        UiText("Time across your week",fontSize=21.sp,fontWeight=FontWeight.SemiBold)
        UiText("Per-platform time · latest seven days",fontSize=12.sp,color=AppStyle.Muted)
        UiText("${durationLabel(series.sumOf{it.totalMillis})} total this week",fontSize=24.sp,fontWeight=FontWeight.Bold,color=AppStyle.Accent)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){UiText("${focusedDay?.date?.format(DateTimeFormatter.ofPattern("d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current))) ?: ""} · ${durationLabel(focusedDay?.totalMillis ?:0L)}",fontSize=14.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text);UiText("Tap the chart",fontSize=11.sp,color=AppStyle.Muted)}
        UiText("Per-app scale: 0 – ${durationLabel(top)}",fontSize=11.sp,color=AppStyle.Muted)
        Box(Modifier.fillMaxWidth().height(180.dp).pointerInput(series.map{it.date}){detectTapGestures{offset->if(size.width>0 && series.isNotEmpty()){val index=kotlin.math.round(offset.x/size.width*(series.size-1)).toInt().coerceIn(series.indices);focusedDate=series[index].date.toString()}}}.semantics {contentDescription=series.joinToString("; "){day->"${day.date}: "+active.joinToString{ i->"${platforms[i].name} ${durationLabel(day.millis[i])}"}}}.drawWithCache {
            val w=size.width;val h=size.height-5.dp.toPx()
            val lines=shown.map {index->
                val points=series.mapIndexed{i,day->androidx.compose.ui.geometry.Offset(w*i/(series.size-1).coerceAtLeast(1),h*(1f-day.millis[index].toFloat()/top))}
                val line=androidx.compose.ui.graphics.Path()
                points.forEachIndexed{i,p->if(i==0)line.moveTo(p.x,p.y)else{val previous=points[i-1];val middle=(previous.x+p.x)/2;line.cubicTo(middle,previous.y,middle,p.y,p.x,p.y)}}
                Triple(index,line,points)
            }
            val grid=AppStyle.Frame.copy(alpha=.5f);val accent=AppStyle.Teal.copy(alpha=.35f);val colors=UsageColors
            onDrawBehind {
                for(i in 0..4)drawLine(grid,androidx.compose.ui.geometry.Offset(0f,h*i/4),androidx.compose.ui.geometry.Offset(w,h*i/4),strokeWidth=.7f.dp.toPx())
                if(series.isNotEmpty())drawLine(accent,androidx.compose.ui.geometry.Offset(w*focused/(series.size-1).coerceAtLeast(1),0f),androidx.compose.ui.geometry.Offset(w*focused/(series.size-1).coerceAtLeast(1),h),strokeWidth=1.dp.toPx())
                lines.forEach{(index,path,points)->
                    drawPath(path,colors[index],style=Stroke(2.5f.dp.toPx(),cap=StrokeCap.Round))
                    points.forEachIndexed{i,p->drawCircle(colors[index],if(i==focused)4.5f.dp.toPx() else 2.5f.dp.toPx(),p)}
                }
            }
        })
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){series.forEach{day->TextButton(onClick={focusedDate=day.date.toString()},contentPadding=PaddingValues(0.dp),modifier=Modifier.weight(1f)){UiText(day.date.format(DateTimeFormatter.ofPattern("EEEEE",java.util.Locale.forLanguageTag(LocalUiLanguage.current))),fontSize=11.sp,color=if(day==focusedDay)AppStyle.Accent else AppStyle.Muted)}}}
        active.chunked(2).forEach {pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            pair.forEach {index->
                val visible=index !in hidden;val color=UsageColors[index]
                Column(Modifier.weight(1f).clip(RoundedCornerShape(15.dp)).clickable{hidden=if(visible)hidden+index else hidden-index}.semantics{selected=visible;contentDescription="${platforms[index].name}, ${if(visible)"line shown" else "line hidden"}, ${durationLabel(focusedDay?.millis?.get(index) ?:0L)}"}.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)){PlatformBrand(platforms[index],Modifier.size(22.dp));UiText(platforms[index].name,fontSize=12.sp,fontWeight=FontWeight.Medium,modifier=Modifier.weight(1f),maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis);Box(Modifier.size(6.dp).background(if(visible)color else AppStyle.Muted,CircleShape))}
                    UiText(durationLabel(focusedDay?.millis?.get(index) ?:0L),fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=if(visible)color else AppStyle.Muted)
                }
            }
            if(pair.size==1)Spacer(Modifier.weight(1f))
        }}
        if(active.isNotEmpty() && shown.isEmpty())UiText("Tap a platform above to show its line.",fontSize=13.sp,color=AppStyle.Muted)
        if(active.isEmpty()) UiText("Your first tracked video will start this chart. No sample data is used.",fontSize=13.sp,lineHeight=20.sp,color=AppStyle.Muted)
    }
}
