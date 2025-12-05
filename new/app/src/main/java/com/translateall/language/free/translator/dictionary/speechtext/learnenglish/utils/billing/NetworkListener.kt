package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.billing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NetworkListener(private val mListener: NetworkStatusListener?) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        mListener?.onNetworkStatusChange()
    }

    interface NetworkStatusListener {
        fun onNetworkStatusChange()
    }
}