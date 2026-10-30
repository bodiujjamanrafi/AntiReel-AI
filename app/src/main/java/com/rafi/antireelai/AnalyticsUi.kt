package com.rafi.antireelai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Shared only by Today/Activity: existing palette, no new theme or rendering layer. */
internal object AnalyticsStyle {
    val radius = 18.dp
    val grid get() = AppStyle.Outline.copy(alpha = if (AppStyle.dark) .75f else .65f)
    val track get() = AppStyle.Muted.copy(alpha = .15f)
    val numbers: TextStyle @Composable get() = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
}

@Composable internal fun ActivityPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(AnalyticsStyle.radius))
            .background(AppStyle.Surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = content
    )
}

@Composable internal fun MetricSelector(time: Boolean, onSelect: (Boolean) -> Unit) {
    val reduced = motionReduced()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(AppStyle.Background).padding(3.dp).selectableGroup()
    ) {
        listOf(true to "Time", false to "Reels").forEach { (value, label) ->
            val active = time == value
            val surface by animateColorAsState(
                if (active) AppStyle.AccentSoft else AppStyle.Background,
                tween(if (reduced) 0 else 150), label = "metric selection"
            )
            Box(
                Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(8.dp))
                    .background(surface).selectable(active, role = Role.Tab, onClick = { onSelect(value) })
                    .padding(horizontal = 8.dp, vertical = 10.dp), contentAlignment = Alignment.Center
            ) {
                UiText(label, fontSize = 14.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) AppStyle.Accent else AppStyle.Muted)
            }
        }
    }
}

/** Compact extreme values only; accessibility descriptions retain the exact count. */
internal fun chartReelLabel(value: Int): String = when {
    value >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", value / 1_000_000.0).replace(".0M", "M")
    value >= 10_000 -> String.format(Locale.getDefault(), "%.1fk", value / 1_000.0).replace(".0k", "k")
    else -> value.toString()
}

/** All columns share a baseline. Positive heights are proportional; zero is a quiet baseline tick. */
@Composable internal fun AnalyticsBars(
    days: List<ActivityDay>, time: Boolean, selectedDay: LocalDate?,
    onSelect: (LocalDate) -> Unit, showDate: Boolean = true, plotHeight: Int = 88
) {
    val reduced = motionReduced()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val labelHeight = maxOf(36.dp, with(density) { 30.sp.toDp() })
    val values = remember(days, time) { days.map { if (time) it.totalMillis.toDouble() else it.totalReels.toDouble() } }
    val maximum = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val locale = Locale.forLanguageTag(LocalUiLanguage.current)
    val dayFormat = remember(locale) { DateTimeFormatter.ofPattern("EEEEE", locale) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.forEachIndexed { index, day ->
            val active = day.date == selectedDay
            val color by animateColorAsState(
                if (active) AppStyle.Accent else AppStyle.Accent.copy(alpha = .55f),
                tween(if (reduced) 0 else 150), label = "day selection"
            )
            val track = AnalyticsStyle.track
            val grid = AnalyticsStyle.grid
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable { onSelect(day.date) }
                    .semantics { selected = active; contentDescription = "${day.date}, ${day.totalReels} reels, ${durationLabel(day.totalMillis)}" }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(labelHeight), contentAlignment = Alignment.BottomCenter) {
                    UiText(if (time) durationLabel(day.totalMillis) else chartReelLabel(day.totalReels),
                        fontSize = 10.sp, lineHeight = 13.sp, color = if (active) AppStyle.Accent else AppStyle.Muted,
                        fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 2, style = AnalyticsStyle.numbers)
                }
                Canvas(Modifier.fillMaxWidth().height(plotHeight.dp)) {
                    val width = minOf(20.dp.toPx(), size.width * .65f)
                    val x = (size.width - width) / 2
                    val value = values[index]
                    drawLine(grid, Offset(0f, size.height - .5.dp.toPx()), Offset(size.width, size.height - .5.dp.toPx()), 1.dp.toPx())
                    val height = if (value > 0) (value / maximum * (size.height - 2.dp.toPx())).toFloat().coerceAtLeast(2.dp.toPx()) else 2.dp.toPx()
                    drawRoundRect(if (value > 0) color else track, Offset(x, size.height - height),
                        Size(width, height), CornerRadius(minOf(3.dp.toPx(), height / 2)))
                }
                UiText(day.date.format(dayFormat), fontSize = 12.sp,
                    color = if (active) AppStyle.Accent else AppStyle.Muted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                if (showDate) UiText("${day.date.dayOfMonth}", fontSize = 11.sp, color = AppStyle.Muted, style = AnalyticsStyle.numbers)
            }
        }
        repeat((7 - days.size).coerceAtLeast(0)) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable internal fun SelectedDaySummary(data: ActivityDay) {
    val locale = Locale.forLanguageTag(LocalUiLanguage.current)
    ActivityPanel {
        UiText(data.date.format(DateTimeFormatter.ofPattern("EEEE, d MMM", locale)),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppStyle.Text)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UiText("${data.totalReels}", fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                    color = AppStyle.Accent, style = AnalyticsStyle.numbers)
                UiText("estimated reels", fontSize = 12.sp, color = AppStyle.Muted)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                UiText(durationLabel(data.totalMillis), fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                    color = AppStyle.Accent, style = AnalyticsStyle.numbers)
                UiText("tracked video-screen time", fontSize = 12.sp, color = AppStyle.Muted)
            }
        }
    }
}
