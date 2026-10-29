package com.rafi.antireelai.tracking

internal const val LIMIT_WARNING_FRACTION=.8f
internal const val LIMIT_CURTAIN_FRACTION=1f

/** Progressively obscure video, then fully cover it at the boundary. */
internal fun limitCoverOpacity(progress:Float):Float = when {
    !progress.isFinite() || progress<LIMIT_WARNING_FRACTION->0f
    progress>=LIMIT_CURTAIN_FRACTION->1f
    else->.48f+.40f*((progress-LIMIT_WARNING_FRACTION)/(LIMIT_CURTAIN_FRACTION-LIMIT_WARNING_FRACTION))
}

/** Count gating is independent of overlay visibility and near-limit tint preferences. */
internal fun dailyLimitReached(total:Int,goal:Int)=total>=goal.coerceAtLeast(1)
