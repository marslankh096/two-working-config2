package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets.MyAlertDialog

class ButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        //        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
        if (notificationId == 1) {
            val i = Intent(context.applicationContext, MyAlertDialog::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
            val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(closeIntent)
        }
    }
}