package com.rafi.antireelai

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.cos
import kotlin.math.sin

/** A finite celebration; all motion respects the existing reduced-motion preference. */
@Composable internal fun CelebratingMiso(reducedMotion:Boolean) {
    val progress=remember{Animatable(if(reducedMotion)1f else 0f)}
    LaunchedEffect(Unit){progress.animateTo(1f,tween(if(reducedMotion)0 else 800))}
    val colors=listOf(AppStyle.Teal,AppStyle.Gold,AppStyle.Accent,AppStyle.Blue)
    Box(Modifier.size(132.dp),contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val radius=size.minDimension*(.32f+progress.value*.14f)
            for(i in 0..11) {
                val angle=i*Math.PI/6
                val point=Offset(center.x+cos(angle).toFloat()*radius,center.y+sin(angle).toFloat()*radius)
                drawRoundRect(colors[i%colors.size].copy(alpha=.8f),point,Size(3.dp.toPx(),6.dp.toPx()))
            }
        }
        AndroidView(factory={CompanionView(it).apply{animated=!reducedMotion;pose=MisoPose.CELEBRATING;contentDescription="Miso celebrates your plan"}},modifier=Modifier.size(108.dp))
    }
}
