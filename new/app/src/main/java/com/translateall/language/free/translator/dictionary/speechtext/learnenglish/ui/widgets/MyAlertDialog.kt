package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils.onStopClipBoard
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.putBoolean
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.stopClipboardService

class MyAlertDialog : AppCompatActivity() {
    var tv: TextView? = null
    var positiveButton: TextView? = null
    var negativeButton: TextView? = null
    var relativeLayout: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE) //hide activity title
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)

//        AdmobClassic admobUtils = new AdmobClassic(this);
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#BA0D0E0E")))
        setContentView(R.layout.noti_exit)
        tv = findViewById(R.id.text_dialog)
        positiveButton = findViewById(R.id.yes_txt)
        negativeButton = findViewById(R.id.no_txt)
        relativeLayout = findViewById(R.id.ad_layout_dialog)
        tv?.setText("Do you want to remove the search box in the notification bar?")
        negativeButton?.setOnClickListener { finish() }
        positiveButton?.setOnClickListener {
            putBoolean(Constants.CLIP_BOARD_SERVICE, false)
            stopClipboardService()

            // get this function in setting screen to toggle switch
            onStopClipBoard?.invoke(false)
            finish()
        }
    }
}