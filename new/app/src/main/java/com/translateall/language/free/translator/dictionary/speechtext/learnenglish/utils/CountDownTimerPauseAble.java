package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils;

import android.os.CountDownTimer;

public abstract class CountDownTimerPauseAble {
    private final long countDownInterval;
    private long millisRemaining;

    private CountDownTimer countDownTimer = null;

    private boolean isPaused = true;

    public CountDownTimerPauseAble(long millisInFuture, long countDownInterval) {
        super();
        this.countDownInterval = countDownInterval;
        this.millisRemaining = millisInFuture;
    }

    private void createCountDownTimer() {
        countDownTimer = new CountDownTimer(millisRemaining, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                CountDownTimerPauseAble.this.onTick(millisUntilFinished);

            }

            @Override
            public void onFinish() {
                CountDownTimerPauseAble.this.onFinish();

            }
        };
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();

    /**
     * Cancel the countdown.
     */
    public final void cancel() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        this.millisRemaining = 0;
    }

    /**
     * Start or Resume the countdown.
     *
     * @return CountDownTimerPausable current instance
     */
    public synchronized final CountDownTimerPauseAble start() {
        if (isPaused) {
            createCountDownTimer();
            countDownTimer.start();
            isPaused = false;
        }
        return this;
    }

    /**
     * Pauses the CountDownTimerPausable, so it could be resumed(start)
     * later from the same point where it was paused.
     */
    public void pause() throws IllegalStateException {
        if (isPaused == false) {
            countDownTimer.cancel();
        } else {
            throw new IllegalStateException("CountDownTimerPausable is already in pause state, start counter before pausing it.");
        }
        isPaused = true;
    }

    public boolean isPaused() {
        return isPaused;
    }

}
