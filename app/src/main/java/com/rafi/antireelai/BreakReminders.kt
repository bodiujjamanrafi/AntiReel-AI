package com.rafi.antireelai

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/** Android owns the countdown display and alarm; there is no busy background timer. */
internal object BreakReminders {
    private const val TIMER_CHANNEL="miso_break_timer"
    private const val CHIME_CHANNEL="miso_break_chime_v1"
    private const val TIMER_ID=210
    private const val DONE_ID=211
    private const val REQUEST=212
    private const val END="com.rafi.antireelai.BREAK_END"
    fun allowed(context:Context)= (Build.VERSION.SDK_INT<33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) && NotificationManagerCompat.from(context).areNotificationsEnabled()
    private fun channels(context:Context) {
        val manager=context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(TIMER_CHANNEL,"Break countdown",NotificationManager.IMPORTANCE_LOW).apply {description="A quiet, live countdown while you take a break";setSound(null,null);enableVibration(false)})
        manager.createNotificationChannel(NotificationChannel(CHIME_CHANNEL,"Miso break chime",NotificationManager.IMPORTANCE_DEFAULT).apply {
            description="AntiReel's original chime when a break ends"
            setSound(Uri.parse("android.resource://${context.packageName}/raw/${context.resources.getResourceEntryName(R.raw.miso_chime)}"),AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            enableVibration(true)
        })
    }
    private fun alarm(context:Context,until:Long)=PendingIntent.getBroadcast(context,REQUEST,Intent(context,BreakReceiver::class.java).setAction(END).putExtra("until",until),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private fun open(context:Context)=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun schedule(context:Context,until:Long) {
        val alarmManager=context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(alarm(context,until))
        if(until<=System.currentTimeMillis()) {NotificationManagerCompat.from(context).cancel(TIMER_ID);return}
        NotificationManagerCompat.from(context).cancel(DONE_ID)
        // No exact-alarm permission is requested. Doze may defer the background reminder.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,until,alarm(context,until))
        showCurrent(context)
    }
    fun refresh(context:Context) {
        val until=TrackerStore(context).pauseUntil
        if(until>0 && until<=System.currentTimeMillis())finish(context,until) else showCurrent(context)
    }
    @Suppress("MissingPermission") fun showCurrent(context:Context) {
        val store=TrackerStore(context)
        if(!store.breakNotifications || !allowed(context)){NotificationManagerCompat.from(context).cancel(TIMER_ID);return}
        val until=store.pauseUntil
        if(until<=System.currentTimeMillis())return
        channels(context)
        NotificationManagerCompat.from(context).notify(TIMER_ID,NotificationCompat.Builder(context,TIMER_CHANNEL)
            .setSmallIcon(R.drawable.ic_miso_notification).setContentTitle(localized(store.prefs.getString("language","en") ?: "en","A little time for you","নিজের জন্য একটু সময়"))
            .setContentText(localized(store.prefs.getString("language","en") ?: "en","Miso is on a break. Your countdown is running.","মিসো বিরতিতে আছে। কাউন্টডাউন চলছে।"))
            .setWhen(until).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
            .setTimeoutAfter((until-System.currentTimeMillis()).coerceAtLeast(1L)).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open(context))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build())
    }
    @Suppress("MissingPermission") fun finish(context:Context,expected:Long=0) {
        val store=TrackerStore(context)
        val until=store.pauseUntil
        when(breakDue(until,expected,System.currentTimeMillis())) {
            BreakDue.INACTIVE,BreakDue.STALE->return
            BreakDue.WAITING->{schedule(context,until);return}
            BreakDue.COMPLETE->Unit
        }
        store.pauseUntil=0
        if(!store.breakNotifications || !allowed(context))return
        channels(context)
        NotificationManagerCompat.from(context).notify(DONE_ID,NotificationCompat.Builder(context,CHIME_CHANNEL)
            .setSmallIcon(R.drawable.ic_miso_notification).setContentTitle(localized(store.prefs.getString("language","en") ?: "en","Your breather is complete","তোমার বিরতি শেষ"))
            .setContentText(localized(store.prefs.getString("language","en") ?: "en",if(store.running) "Miso is ready to keep you company again." else "Take your time. Miso is here when you need a hand.",if(store.running)"মিসো আবার তোমার পাশে থাকতে প্রস্তুত।" else "সময় নাও। প্রয়োজন হলে মিসো পাশে আছে।"))
            .setAutoCancel(true).setContentIntent(open(context)).setCategory(NotificationCompat.CATEGORY_REMINDER).build())
    }
}
class BreakReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        if(intent.action==Intent.ACTION_BOOT_COMPLETED) {
            val until=TrackerStore(context).pauseUntil
            if(until>System.currentTimeMillis())BreakReminders.schedule(context,until)
            else if(until>0)BreakReminders.finish(context,until)
        } else BreakReminders.finish(context,intent.getLongExtra("until",0))
    }
}
