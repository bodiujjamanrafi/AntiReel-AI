package com.rafi.antireelai

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private data class PageRoute(val tab:Int,val detail:String?) {
    val settings get()=tab==AppDestination.Settings.ordinal || detail=="platform-manager"
}

/** Initialize before the first draw: Settings fades without moving or resizing its content. */
@Composable internal fun PageMotion(tab:Int,detail:String?,reduced:Boolean,modifier:Modifier=Modifier,content:@Composable ()->Unit) {
    val route=PageRoute(tab,detail)
    val progress=remember(route,reduced){Animatable(if(reduced)1f else 0f)}
    LaunchedEffect(progress) {
        if(!reduced)progress.animateTo(1f,tween(if(route.settings)100 else 80,easing=CubicBezierEasing(.2f,0f,0f,1f)))
    }
    CompositionLocalProvider(LocalMotionReduced provides reduced) {
        Box(modifier.graphicsLayer {
            val value=progress.value
            alpha=.90f+.10f*value
            scaleX=1f
            scaleY=scaleX
        }){content()}
    }
}
