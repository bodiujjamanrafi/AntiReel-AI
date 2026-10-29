package com.rafi.antireelai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rafi.antireelai.tracking.LimitStage

/** Called only at persisted daily warning milestones, never on every reel or redraw. */
internal object GoalNotifications {
    private const val NEAR_CHANNEL="miso_boundary_near_v1"
    private const val LIMIT_CHANNEL="miso_boundary_limit_v1"
    @Suppress("MissingPermission")
    fun show(context:Context,stage:LimitStage,total:Int,goal:Int) {
        val store=TrackerStore(context)
        val language=store.prefs.getString("language","en") ?: "en"
        if(stage==LimitStage.NONE || !store.limitNotifications || !BreakReminders.allowed(context))return
        val near=stage==LimitStage.NEAR
        val manager=context.getSystemService(NotificationManager::class.java)
        listOf(Triple(NEAR_CHANNEL,"Approaching daily goal",R.raw.miso_near),Triple(LIMIT_CHANNEL,"Daily goal reached",R.raw.miso_limit)).forEach{(id,name,sound)->
            manager.createNotificationChannel(NotificationChannel(id,name,NotificationManager.IMPORTANCE_DEFAULT).apply{
                setSound(Uri.parse("android.resource://${context.packageName}/raw/${context.resources.getResourceEntryName(sound)}"),AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                enableVibration(false)
            })
        }
        val open=PendingIntent.getActivity(context,240,Intent(context,MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if(!near)NotificationManagerCompat.from(context).cancel(240)
        val title=when(stage){LimitStage.NEAR->localized(language,"You're close to your daily goal","দৈনিক সীমা প্রায় শেষ");LimitStage.REACHED->localized(language,"Your daily goal is reached","আজকের দৈনিক সীমা শেষ");else->localized(language,"Time to step away from the feed","ফিড থেকে বিরতি নেওয়ার সময়")}
        val detail=if(store.dailyMinutes>0)localized(language,"${durationLabel(store.totalUsage())} / ${store.dailyMinutes} min · $total / $goal reels. The first reached boundary ends the allowance.","${durationLabel(store.totalUsage())} / ${store.dailyMinutes} মিনিট · $total / $goal রিলস। যেকোনো সীমা আগে শেষ হলে বিরতি নাও।") else if(near)localized(language,"${(goal-total).coerceAtLeast(0)} reels remain. Miso recommends a break.","${(goal-total).coerceAtLeast(0)}টি রিলস বাকি। মিসো বিরতি নিতে বলছে।") else localized(language,"$total of $goal reels watched today. Open AntiReel to take a break.","আজ ${goal}টির মধ্যে ${total}টি রিলস দেখা হয়েছে। বিরতি নিতে অ্যান্টিরিল খোলো।")
        NotificationManagerCompat.from(context).notify(if(near)240 else 241,NotificationCompat.Builder(context,if(near)NEAR_CHANNEL else LIMIT_CHANNEL)
            .setSmallIcon(R.drawable.ic_miso_notification).setContentTitle(title).setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail)).setContentIntent(open).setAutoCancel(true)
            .setSilent(stage==LimitStage.EXCEEDED).setCategory(NotificationCompat.CATEGORY_REMINDER).build())
    }
}
