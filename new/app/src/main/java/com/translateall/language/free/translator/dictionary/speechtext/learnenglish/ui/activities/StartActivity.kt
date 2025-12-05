package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // To prevent launching another instance of app on clicking app icon
        if (!isTaskRoot()
            && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
            && getIntent().getAction() != null
            && getIntent().getAction().equals(Intent.ACTION_MAIN)
        ) {
            finish()
            return
        }

        val startIntent = Intent(this, SplashActivity::class.java)
        startActivity(startIntent)
        finish()
    }

    override fun onBackPressed() {}
}