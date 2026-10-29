package com.rafi.antireelai

/** Both targets are independent: the first reached boundary ends today's allowance. */
internal fun dailyAllowanceProgress(reels:Int,reelLimit:Int,recordedMillis:Long,minutes:Int):Float =
    maxOf(reels.coerceAtLeast(0).toFloat()/reelLimit.coerceAtLeast(1),
        if(minutes>0)recordedMillis.coerceAtLeast(0).toFloat()/(minutes.toLong()*60_000) else 0f)
internal fun dailyAllowanceReached(reels:Int,reelLimit:Int,recordedMillis:Long,minutes:Int)=
    reels>=reelLimit.coerceAtLeast(1) || (minutes>0 && recordedMillis>=minutes.toLong()*60_000)
