package com.subefu.statik.recevier

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.subefu.statik.R
import com.subefu.statik.screen.MainActivity
import com.subefu.statik.utils.Constant
import java.sql.Date

class NotificationReceiver : BroadcastReceiver() {

    var chanelId: String = "0209"
    var title: String = "Hello"
    var message: String = ""
    var fullMessage: String = ""

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        checkOnNotification(context)
        checkCompleteChannel(notificationManager)
        checkVisibilityChannel(context, notificationManager)

        val intent = Intent(context, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, chanelId)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(resultPendingIntent)
            .build()

        notificationManager.notify(1, notification)

        val config = context.getSharedPreferences(Constant.CONFIG, 0)
        if(config.getString(Constant.NOTIFY_ENABLE, "false") == "true"){
            setNotificationRecevier(context, java.util.Date().time + 24 * 60 * 60 * 1000)
        }
    }

    fun checkOnNotification(context: Context){
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
            }
            context.startActivity(intent)
        }
    }
    fun checkCompleteChannel(manager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = manager.getNotificationChannel(chanelId)
            if(chan == null){
                val channel = NotificationChannel(chanelId, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
                channel.description = "Напоминания об оценке дня"
                channel.enableLights(true)
                channel.lightColor = Color.GREEN
                channel.enableVibration(true)
                manager.createNotificationChannel(channel)
            }
        }
    }
    fun checkVisibilityChannel(context: Context, notificationManager: NotificationManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelStatus = notificationManager.getNotificationChannel("0209").importance

            if (channelStatus == NotificationManager.IMPORTANCE_NONE) {
                val intentSet = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                intentSet.putExtra(Settings.EXTRA_CHANNEL_ID, chanelId)
                intentSet.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intentSet)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setNotificationRecevier(context: Context, timeInMillis: Long){
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}