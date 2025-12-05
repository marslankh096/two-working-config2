package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.SplashActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INPUT_TYPE_KEY
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.INPUT_TYPE_KEY_TEXT

class ClipboardNotificationManager(private val mContext: Context) {
    private val mNotificationManager: NotificationManager
    fun prepareNotification(): Notification {
        // handle build version above android oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null
        ) {
            val name: CharSequence = mContext.getString(R.string.text_name_notification)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance)
            channel.enableVibration(false)
            mNotificationManager.createNotificationChannel(channel)
        }
        val remoteViews = RemoteViews(mContext.packageName, R.layout.custom_notification)
        /*val buttonIntent = Intent(mContext, ButtonReceiver::class.java)
        buttonIntent.putExtra("notificationId", 1)

//Create the PendingIntent
        val btPendingIntent = PendingIntent.getBroadcast(mContext, 0, buttonIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.iv_cross, btPendingIntent)*/


        // notification builder
        val notificationBuilder: NotificationCompat.Builder
        notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(
                mContext,
                FOREGROUND_CHANNEL_ID
            )
        } else {
            NotificationCompat.Builder(mContext)
        }
        notificationBuilder
            .setContent(remoteViews)
            .setSmallIcon(R.drawable.fav_icon)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(getHomeIntent())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        return notificationBuilder.build()
    }

    companion object {
        private const val FOREGROUND_CHANNEL_ID = "foreground_channel_id"
    }

    init {
        mNotificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun getHomeIntent(): PendingIntent {

        val resultIntent = Intent(mContext, SplashActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(mContext)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}