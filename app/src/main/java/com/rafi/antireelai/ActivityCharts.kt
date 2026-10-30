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

internal data class ActivityDay(val date:LocalDate,val reels:List<Int>,val millis:List<Long>,val platformIds:List<String> = platforms.map{it.id}) {
    private val positions=platformIds.withIndex().associate{it.value to it.index}
    fun reelsAt(index:Int)=reels.getOrElse(positions[platforms[index].id] ?: -1){0}
    fun millisAt(index:Int)=millis.getOrElse(positions[platforms[index].id] ?: -1){0L}
    fun alignedReels()=platforms.indices.map{reelsAt(it)}
    val totalReels=reels.sumOf{it.toLong()}.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    val totalMillis=millis.sum()
    companion object {
        fun read(date:LocalDate,values:Map<String,*>,catalog:List<Platform> = platforms)=ActivityDay(date,catalog.map{(values["count:$date:${it.id}"] as? Int ?:0).coerceAtLeast(0)},catalog.map{(values["usage:$date:${it.id}"] as? Long ?:0L).coerceAtLeast(0)},catalog.map{it.id})
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
/** Windows are anchored to today, with the oldest partial window limited to retained data. */
internal fun historyWindow(today:LocalDate,selected:LocalDate,retainedDays:Int=30):List<LocalDate> {
    val offset=java.time.temporal.ChronoUnit.DAYS.between(selected,today).coerceIn(0,(retainedDays-1).coerceAtLeast(0).toLong()).toInt()
    val end=today.minusDays((offset/7*7).toLong())
    return (6 downTo 0).map{end.minusDays(it.toLong())}.filter{!it.isBefore(today.minusDays((retainedDays-1).toLong()))}
}
/** Stable chronological order, independent of the caller's storage order. */
internal fun recentActivityDays(history:List<ActivityDay>) = history.sortedByDescending { it.date }.take(7).reversed()
internal fun activityDayOrEmpty(history:List<ActivityDay>,date:LocalDate) = history.find { it.date==date }
    ?: ActivityDay(date,List(platforms.size){0},List(platforms.size){0L})

@Composable internal fun UsageWeekChart(history:List<ActivityDay>,day:LocalDate,onSelect:(LocalDate)->Unit) {
    val today=LocalDate.now()
    val days=remember(today,day){historyWindow(today,day)}
    var timeMode by rememberSaveable { mutableStateOf(true) }
    val byDate=remember(history){history.associateBy{it.date}}
    val chartDays=remember(days,byDate){days.map { byDate[it] ?: ActivityDay(it,List(platforms.size){0},List(platforms.size){0L}) }}
    val earliest=today.minusDays(29)
    val locale=java.util.Locale.forLanguageTag(LocalUiLanguage.current)
    ActivityPanel {
        Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
            UiText("Daily history",fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
            UiText("Daily totals across every platform",fontSize=12.sp,color=AppStyle.Muted)
        }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={onSelect(days.first().minusDays(1).coerceAtLeast(earliest))},enabled=days.first()>earliest,
                modifier=Modifier.semantics{contentDescription="Previous seven days"}) {UiText("‹",fontSize=26.sp,color=if(days.first()>earliest)AppStyle.Text else AppStyle.Muted)}
            UiText("${days.first().format(DateTimeFormatter.ofPattern("d MMM",locale))} – ${days.last().format(DateTimeFormatter.ofPattern("d MMM",locale))}",
                fontSize=13.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text,modifier=Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick={onSelect(days.last().plusDays(7).coerceAtMost(today))},enabled=days.last()<today,
                modifier=Modifier.semantics{contentDescription="Next seven days"}) {UiText("›",fontSize=26.sp,color=if(days.last()<today)AppStyle.Text else AppStyle.Muted)}
        }
        MetricSelector(timeMode,{timeMode=it})
        AnalyticsBars(chartDays,timeMode,day,onSelect)
        UiText("Tap a day to see its platform breakdown below. History is kept for 30 days.",fontSize=12.sp,lineHeight=18.sp,color=AppStyle.Muted)
    }
}
private val LightUsageColors=listOf(0xFF8050E7,0xFF1ABF98,0xFF288CB9,0xFFBC6A29,0xFF8054BD,0xFFBE3C70,0xFF607B32,0xFF2983B3,0xFFC44C39,0xFF626184).map {Color(it)}
private val DarkUsageColors=listOf(0xFFC0A2FF,0xFF71E1C2,0xFF86D5F1,0xFFF8BD80,0xFFD0AEF1,0xFFFFA3C5,0xFFBDD98A,0xFF93D5FF,0xFFFFA797,0xFFBEBBEB).map{Color(it)}
private fun usageColor(index:Int):Color {
    val palette=if(AppStyle.dark)DarkUsageColors else LightUsageColors
    return palette.getOrElse(index) {
        val hue=(platforms[index].id.hashCode().ushr(1)%360).toFloat()
        Color.hsl(hue,.55f,if(AppStyle.dark).72f else .40f)
    }
}
private val UsageColors get()=platforms.indices.map{usageColor(it)}
/** Labels use separated side lanes so tiny slices never overlap or disappear. */
internal data class UsageShare(val index:Int,val fraction:Double,val middleDegrees:Float)
internal fun usageShares(values:List<Double>):List<UsageShare> {
    val safe=values.map{if(it.isFinite())it.coerceAtLeast(0.0) else 0.0}
    val maximum=safe.maxOrNull() ?:0.0
    if(maximum<=0)return emptyList()
    val normalized=safe.map{it/maximum}
    val sum=normalized.sum()
    var angle=-90f
    return normalized.mapIndexedNotNull{i,value->
        val sweep=(value/sum*360).toFloat()
        val result=if(value>0)UsageShare(i,value/sum,angle+sweep/2) else null
        angle+=sweep;result
    }
}
private data class DonutLabel(val index:Int,val tip:androidx.compose.ui.geometry.Offset,val elbow:androidx.compose.ui.geometry.Offset,val edge:Float,val y:Float,val right:Boolean,val name:String,val percentage:String)
@Composable internal fun UsageDonut(data:ActivityDay) {
    var showTime by rememberSaveable {mutableStateOf(true)}
    val values=remember(data,showTime,platforms){platforms.indices.map {if(showTime)data.millisAt(it).toDouble() else data.reelsAt(it).toDouble()}}
    val shares=remember(values){usageShares(values)}
    val sum=values.sum()
    val description=if(showTime) "Time in recognized short-video screens" else "Share of estimated reels"
    val colors=UsageColors
    val labelColor=AppStyle.Text
    val mutedColor=AppStyle.Muted
    val emptyColor=AnalyticsStyle.track
    val laneCount=shares.groupingBy{kotlin.math.cos(Math.toRadians(it.middleDegrees.toDouble()))>=0}.eachCount().values.maxOrNull() ?:0
    val fontScale=androidx.compose.ui.platform.LocalDensity.current.fontScale
    val chartHeight=if(shares.isEmpty())184.dp else maxOf(216f, laneCount*30f*fontScale+24).dp
    val accessible=shares.joinToString("; "){"${platforms[it.index].name}: ${String.format(java.util.Locale.getDefault(),"%.1f%%",it.fraction*100)}, ${if(showTime)durationLabel(values[it.index].toLong()) else "${values[it.index].toInt()} reels"}"}
    ActivityPanel {
        UiText("Where your attention goes",fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
        UiText("${data.date.format(DateTimeFormatter.ofPattern("d MMM",java.util.Locale.forLanguageTag(LocalUiLanguage.current)))} · recorded usage",fontSize=12.sp,color=AppStyle.Muted)
        MetricSelector(showTime,{showTime=it})
        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
            Box(Modifier.fillMaxWidth().height(chartHeight).semantics{contentDescription="$description. $accessible"}.drawWithCache {
                val center=androidx.compose.ui.geometry.Offset(size.width/2f,size.height/2f)
                val radius=minOf(74.dp.toPx(),size.width*.235f)
                val thickness=14.dp.toPx()
                val origin=androidx.compose.ui.geometry.Offset(center.x-radius,center.y-radius)
                val extent=androidx.compose.ui.geometry.Size(radius*2,radius*2)
                val stroke=Stroke(thickness,cap=StrokeCap.Butt)
                val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    textSize=10.sp.toPx()
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
                        val y=if(lane.size==1)center.y else 20.dp.toPx()+row*(size.height-40.dp.toPx())/(lane.size-1)
                        val edge=if(right)size.width-3.dp.toPx() else 3.dp.toPx()
                        val elbow=if(right)size.width*.79f else size.width*.21f
                        labels.add(DonutLabel(share.index,tip,androidx.compose.ui.geometry.Offset(elbow,y),edge,y,right,
                            android.text.TextUtils.ellipsize(platforms[share.index].name,textPaint,size.width*.205f,android.text.TextUtils.TruncateAt.END).toString(),
                            String.format(java.util.Locale.getDefault(),"%.1f%%",share.fraction*100)))
                    }
                }
                onDrawBehind {
                    if(shares.isEmpty())drawArc(emptyColor,0f,360f,false,origin,extent,style=stroke)
                    shares.forEach{share->
                        val sweep=(share.fraction*360).toFloat();val gap=if(shares.size==1)0f else minOf(2.5f,sweep*.15f)
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
            Column(Modifier.widthIn(max=108.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)) {
                UiText(if(showTime)durationLabel(sum.toLong()) else "${sum.toInt()}",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text,textAlign=androidx.compose.ui.text.style.TextAlign.Center,style=AnalyticsStyle.numbers)
                UiText(if(sum==0.0)"No data yet" else if(showTime)"tracked time" else "estimated reels",fontSize=10.sp,color=AppStyle.Muted)
            }
        }
        UiText(if(sum==0.0)"Start Miso and watch a selected short-video feed. Your recorded usage appears here when you return." else "$description. Updated from Miso’s local records; this is not total app screen time.",fontSize=12.sp,lineHeight=18.sp,color=mutedColor)
    }
}

/** One date selection drives the trend, daily summary and distribution. Legend toggles are local UI state. */
@Composable internal fun UsageTrendChart(
    history:List<ActivityDay>,reducedMotion:Boolean,day:LocalDate,
    onSelect:(LocalDate)->Unit
) {
    val series=remember(history){recentActivityDays(history)}
    val focused=series.indexOfFirst{it.date==day}
    val focusedDay=remember(history,day){activityDayOrEmpty(history,day)}
    val active=remember(series,platforms){platforms.indices.filter { i -> series.any{it.millisAt(i)>0} }}
    var hidden by rememberSaveable {mutableStateOf(emptyList<Int>())}
    val shown=active.filter{it !in hidden}
    val top=series.maxOfOrNull{item->shown.maxOfOrNull{item.millisAt(it)} ?:0L}?.coerceAtLeast(2000L) ?:2000L
    val locale=java.util.Locale.forLanguageTag(LocalUiLanguage.current)
    val colors=UsageColors
    val grid=AnalyticsStyle.grid
    val muted=AppStyle.Muted
    val selection=AppStyle.Accent.copy(alpha=.35f)
    val reduced=motionReduced(reducedMotion)
    ActivityPanel {
        Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
            UiText("Time across your week",fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=AppStyle.Text)
            UiText("Per-platform time · latest seven days",fontSize=12.sp,color=AppStyle.Muted)
        }
        UiText("${durationLabel(series.sumOf{it.totalMillis})} total this week",fontSize=24.sp,
            fontWeight=FontWeight.SemiBold,color=AppStyle.Accent,style=AnalyticsStyle.numbers)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            UiText("${day.format(DateTimeFormatter.ofPattern("d MMM",locale))} · ${durationLabel(focusedDay.totalMillis)}",
                modifier=Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Medium,color=AppStyle.Text,style=AnalyticsStyle.numbers)
            UiText("Tap the chart",fontSize=11.sp,color=AppStyle.Muted)
        }
        UiText("Per-app scale: 0 – ${durationLabel(top)}",fontSize=11.sp,color=AppStyle.Muted)
        val density=androidx.compose.ui.platform.LocalDensity.current
        val left=with(density){48.dp.toPx()}
        val right=with(density){8.dp.toPx()}
        Box(Modifier.fillMaxWidth().height(148.dp)
            .pointerInput(series,left,right){detectTapGestures{offset->
                if(series.isNotEmpty() && size.width>left+right) {
                    val position=(offset.x-left)/(size.width-left-right)
                    val index=kotlin.math.round(position*(series.size-1)).toInt().coerceIn(series.indices)
                    onSelect(series[index].date)
                }
            }}
            .semantics{contentDescription=series.joinToString("; "){item->"${item.date}: "+active.joinToString{ i->"${platforms[i].name} ${durationLabel(item.millisAt(i))}"}}}
            .drawWithCache {
                val topInset=8.dp.toPx();val bottom=size.height-8.dp.toPx()
                val width=(size.width-left-right).coerceAtLeast(1f);val height=bottom-topInset
                fun x(index:Int)=left+width*index/(series.size-1).coerceAtLeast(1)
                val lines=shown.map {index->
                    val points=series.mapIndexed{i,item->androidx.compose.ui.geometry.Offset(x(i),bottom-height*(item.millisAt(index).toDouble()/top).toFloat())}
                    // Straight segments preserve the recorded daily samples without inventing curved peaks.
                    val path=androidx.compose.ui.graphics.Path().apply {points.forEachIndexed{i,p->if(i==0)moveTo(p.x,p.y)else lineTo(p.x,p.y)}}
                    Triple(index,path,points)
                }
                val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color=muted.toArgb();textSize=9.sp.toPx();textAlign=android.graphics.Paint.Align.RIGHT
                }
                onDrawBehind {
                    for(i in 0..2) {
                        val y=topInset+height*i/2
                        drawLine(grid,androidx.compose.ui.geometry.Offset(left,y),androidx.compose.ui.geometry.Offset(size.width-right,y),1.dp.toPx())
                        val label=durationLabel((top.toDouble()*(2-i)/2).toLong())
                        drawContext.canvas.nativeCanvas.drawText(label,left-7.dp.toPx(),y-(paint.ascent()+paint.descent())/2,paint)
                    }
                    if(focused>=0)drawLine(selection,androidx.compose.ui.geometry.Offset(x(focused),topInset),androidx.compose.ui.geometry.Offset(x(focused),bottom),1.dp.toPx())
                    lines.forEach{(index,path,points)->
                        drawPath(path,colors[index],style=Stroke(2.dp.toPx(),cap=StrokeCap.Round))
                        points.forEachIndexed{i,p->if(series[i].millisAt(index)>0 || i==focused) {
                            drawCircle(AppStyle.Surface,if(i==focused)4.dp.toPx() else 2.5f.dp.toPx(),p)
                            drawCircle(colors[index],if(i==focused)3.dp.toPx() else 1.75f.dp.toPx(),p)
                        }}
                    }
                }
            })
        Row(Modifier.fillMaxWidth().padding(start=24.dp),horizontalArrangement=Arrangement.SpaceBetween) {
            series.forEach{item->
                TextButton(onClick={onSelect(item.date)},contentPadding=PaddingValues(0.dp),modifier=Modifier.weight(1f).heightIn(min=48.dp).semantics{selected=item.date==day;contentDescription="Select ${item.date}"}) {
                    UiText(item.date.format(DateTimeFormatter.ofPattern("EEEEE",locale)),fontSize=12.sp,
                        fontWeight=if(item.date==day)FontWeight.SemiBold else FontWeight.Normal,
                        color=if(item.date==day)AppStyle.Accent else AppStyle.Muted)
                }
            }
        }
        active.chunked(2).forEach{pair->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            pair.forEach{index->
                val visible=index !in hidden
                val color by androidx.compose.animation.animateColorAsState(if(visible)colors[index] else AppStyle.Muted,
                    androidx.compose.animation.core.tween(if(reduced)0 else 140),label="platform visibility")
                Row(Modifier.weight(1f).heightIn(min=48.dp).clip(RoundedCornerShape(8.dp))
                    .clickable{hidden=if(visible)hidden+index else hidden-index}
                    .semantics{selected=visible;contentDescription="${platforms[index].name}, ${if(visible)"line shown" else "line hidden"}, ${durationLabel(focusedDay.millisAt(index))}"}
                    .padding(vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    PlatformBrand(platforms[index],Modifier.size(20.dp))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                        UiText(platforms[index].name,fontSize=12.sp,color=AppStyle.Text,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis,maxLines=1)
                        UiText(durationLabel(focusedDay.millisAt(index)),fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=color,style=AnalyticsStyle.numbers)
                    }
                    Box(Modifier.size(6.dp).background(color,CircleShape))
                }
            }
            if(pair.size==1)Spacer(Modifier.weight(1f))
        }}
        if(active.isNotEmpty() && shown.isEmpty())UiText("Tap a platform above to show its line.",fontSize=12.sp,color=AppStyle.Muted)
        if(series.none{it.totalMillis>0})UiText("Your first tracked video will start this chart. No sample data is used.",fontSize=12.sp,lineHeight=18.sp,color=AppStyle.Muted)
    }
}
