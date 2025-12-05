package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.SplashActivity


class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("none", "onReceive: NotificationReceiver")
        intent?.let {
            if (it.hasExtra("requestCode")) {
                context?.let { ctx ->
                    Log.e("none", "onReceive: ${it.getStringExtra("requestCode")}")
                    val notificationManager = NotificationManagerCompat.from(ctx)
                    val channelId = "scheduled_unique_channel_id"
                    val channelName = "Scheduled Notification Channel"
                    val importance = NotificationManager.IMPORTANCE_HIGH

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(channelId, channelName, importance)
                        channel.description = "To show scheduled notifications"
                        notificationManager.createNotificationChannel(channel)
                    }

                    val builder: NotificationCompat.Builder =
                        NotificationCompat.Builder(ctx, channelId)
                            .setContentTitle(ctx.getString(R.string.app_name))
                            .setContentText(getRandomContent())
                            .setStyle(NotificationCompat.BigTextStyle().bigText(getRandomContent()))
                            .setSmallIcon(R.drawable.icon_main_app_rounded)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setContentIntent(getSplashIntent(ctx))

                    if (ActivityCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationManager.notify(54865, builder.build())
                    }

                }

            }
        }
    }

    private fun getRandomContent(): String {
        val listOfContent = listOf(
            "You can easily convert documents from one language to another quickly and conveniently with the Translate All application.",
            "Just speak and our translator app will automatically convert your words into the target language.",
            "With the voice translator feature, you can speak and translate instantly, saving time and effort. This provides great convenience and flexibility when communicating with foreigners",
            "Experience the power of voice translation and explore the world with ease and confidence!",
            "With our phrase feature, you can easily look up the meanings and usage examples of thousands of words and phrases.",
            "With our translation history feature, you can easily review your previous translations, helping you track and remember your learning and communication progress.",
            "Expand your translation capabilities and master your language learning with our awesome translation history feature!",
            "Language translator for voice & text translation",
            "Make language learning a daily habit",
            "Maximize your translation potential with our app",
            "Select your favorite languages for quick access and personalized translation",
        )
        return listOfContent[(0..10).random()]
    }

    private fun getSplashIntent(mContext: Context): PendingIntent {
        val resultIntent = Intent(mContext, SplashActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(mContext)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        return stackBuilder.getPendingIntent(0, if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
    }

}