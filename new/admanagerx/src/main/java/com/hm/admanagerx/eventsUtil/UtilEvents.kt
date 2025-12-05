package com.hm.admanagerx.eventsUtil

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

fun Context.logEvents(eventName: String,vararg messages:String?) {
    kotlin.runCatching {
        val bundle = Bundle().apply {
            messages.forEachIndexed { x, parm ->
                putString(if (x == 0) eventName else "eventName$x", parm)
                Log.d("TAG", "logEvents: $x , $parm")
            }
        }
        FirebaseAnalytics.getInstance(this).logEvent(eventName, bundle)
    }.onFailure { }
}

fun Context.setUserProperty(pair: Pair<String, String>) =
    FirebaseAnalytics.getInstance(this).setUserProperty(pair.first, pair.second)