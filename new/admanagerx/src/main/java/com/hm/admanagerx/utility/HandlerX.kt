package com.hm.admanagerx.utility

import android.os.Handler


class HandlerX( delay: Long = 1000,runnable: Runnable? = null) {

    var mHandler: Handler? = null
    private var mRunnable: Runnable? = null

    init {
        this.mHandler = Handler()
        mRunnable = runnable
        mRunnable?.let {
            mHandler?.postDelayed(it, delay)
        }
    }

    fun destroyHandler() {
        mRunnable?.let {
            mHandler?.removeCallbacks(it)
        }
    }

}