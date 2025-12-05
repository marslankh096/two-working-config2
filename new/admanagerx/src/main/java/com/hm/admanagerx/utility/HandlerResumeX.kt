package com.hm.admanagerx.utility

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

class HandlerResumeX(
    var delay: Long,
    var onLoadingUpdate: ((Int) -> Unit)? = null,
    var onLoadingEnd: (() -> Unit)? = null,

) {

    private var animator: ValueAnimator? = null

    init {
        animator = ValueAnimator.ofInt(0, delay.toInt()).apply {

            duration = delay
            addUpdateListener {
                onLoadingUpdate?.invoke(it.animatedValue as Int)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    onLoadingEnd?.invoke()
                }
            })
        }
    }


    fun pause() = animator?.pause()
    fun resume() = animator?.resume()
    fun start() = animator?.start()
    fun cancel() = animator?.cancel()
    fun destroy() = animator?.removeAllUpdateListeners()


}