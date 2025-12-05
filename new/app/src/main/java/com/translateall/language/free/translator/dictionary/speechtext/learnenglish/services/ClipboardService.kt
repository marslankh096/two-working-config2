package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Patterns
import androidx.core.app.NotificationCompat
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.clipboard.ClipboardActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.CLIP_BOARD_SERVICE

class ClipboardService : Service() {
    var copiedWord: String? = null
    var clipBoardData: String? = null
    private var mCM: ClipboardManager? = null
    var mOldClip: String? = null
    var isclip = true

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initClipboard()
    }

    private fun initClipboard() {
        if (getFirstBoolean(CLIP_BOARD_SERVICE) && isBelowAndroidTen()) {
            try {
                mCM = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = ClipData.newPlainText("", "")
                if (data != null) {
                    mCM!!.setPrimaryClip(data)
                }
                if (onPrimaryClipChangedListener != null) mCM!!.addPrimaryClipChangedListener(
                    onPrimaryClipChangedListener
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        if (Constants.ACTION.START_ACTION == intent.action) {
            val clipboardNotificationManager = ClipboardNotificationManager(this)
            val notification = clipboardNotificationManager.prepareNotification()
            startForeground(2, notification)

        } else {
            stopForeground(true)
            putBoolean(Constants.STICKY_SERVICE, false)
            stopSelf()
        }


        return START_STICKY
    }

 /*   override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        stopSelf()
    }
*/

    override fun onDestroy() {
        super.onDestroy()
        removedClipboardListener()
    }

    private val onPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener? =
        ClipboardManager.OnPrimaryClipChangedListener {
            if (mCM != null) {
                val clipDescription = mCM!!.primaryClipDescription
                if (clipDescription != null) {
                    clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    if (mCM!!.hasPrimaryClip() && mCM!!.primaryClip != null) {
                        val mClip = mCM!!.primaryClip
                        var newClip = ""
                        if (mClip != null) {
                            newClip = mClip.toString()
                        }
                        if (newClip != mOldClip && !newClip.contains("NULL") && newClip != null) {
                            mOldClip = newClip
                            if (isclip) {
                                if (!getBoolean("is_from_app")) {
                                    checkText()
                                } else {
                                    putBoolean("is_from_app", false)
                                }
                            }
                        } else {
                            showToast(getString(R.string.copy_another_data), 1)
                        }
                    }
                }
            } else {
                showToast(getString(R.string.please_copy_again), 1)
            }
        }

    private fun checkText() {
        if (mCM != null) {
            val charSequence = mCM!!.text
            if (charSequence != null) {
                copiedWord = charSequence.toString().trim { it <= ' ' }
            }
            if (copiedWord != null && !copiedWord!!.isEmpty()) {
                if (!isValidUrl(copiedWord!!)) {
                    showTranslationDialog()

                } else {
                    showToast(getString(R.string.please_copy_valid_input_word), 1)
                }
            }
        } else {
            showToast(getString(R.string.select_valid_input_word), 1)
        }
    }

    private fun showTranslationDialog() {
        clipBoardData = copiedWord
        val clipboardIntent = Intent(this, ClipboardActivity::class.java)
        clipboardIntent.putExtra("from", "service")
        clipboardIntent.putExtra("clip_board_data", clipBoardData)
        clipboardIntent.addFlags(268435456)
        clipboardIntent.addFlags(8388608)
//        startActivity(clipboardIntent)

        try {
            PendingIntent.getActivity(this, 0, clipboardIntent, 134217728).send()
        } catch (unused: PendingIntent.CanceledException) {
            startActivity(clipboardIntent)
        }
    }

    private fun isValidUrl(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.toLowerCase())
        return m.matches()
    }

    fun removedClipboardListener() {
        if (mCM != null && onPrimaryClipChangedListener != null) {
            mCM!!.removePrimaryClipChangedListener(onPrimaryClipChangedListener)
        }
    }


}