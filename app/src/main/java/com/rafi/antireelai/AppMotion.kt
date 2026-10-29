package com.rafi.antireelai

import android.animation.ValueAnimator
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

internal val LocalMotionReduced=staticCompositionLocalOf{false}

/** Android scales Compose animations; this also removes transforms when animations are disabled. */
@Composable internal fun motionReduced(preference:Boolean=false):Boolean {
    var systemDisabled by remember{mutableStateOf(!ValueAnimator.areAnimatorsEnabled())}
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_RESUME)systemDisabled=!ValueAnimator.areAnimatorsEnabled()}
        lifecycle.addObserver(observer)
        onDispose{lifecycle.removeObserver(observer)}
    }
    return preference || LocalMotionReduced.current || systemDisabled
}
