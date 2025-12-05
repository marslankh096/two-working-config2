package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hm.admanagerx.isAppOpenAdShow
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.TranslateApplication
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.dictionary.WordResponse
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.models.PhraseLanguages
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.services.ClipboardService
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.camera.CameraActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.conversation.ConversationActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.favorites.FavoriteActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.fullscreen.FullScreenActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inApp.InAppActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.inputscreen.InputActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.language.AppLanguageActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.generic.LanguageSelectionActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.languageSelection.ocr.LanguageSelectionOCR
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.MainActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.onboarding.OnboardingActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.settings.SettingsActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.*
import com.willy.ratingbar.ScaleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * message : to show toast message
 * length:
 * 0 for Toast.LENGTH_SHORT
 * 1 for Toast.LENGTH_LONG
 */
fun Context.showToast(message: String, length: Int) {
    val toast = Toast.makeText(this, message, length)
    toast.show()
}

fun Context.isPremium(): Boolean {
    return getTinyDb().getBoolean(IS_PREMIUM)
}

fun Context.getBoolean(value: String): Boolean {
    return getTinyDb().getBoolean(value)
}

fun Context.getFirstBoolean(value: String): Boolean {
    return getTinyDb().getTrueBoolean(value)
}

fun Context.putBoolean(key: String, value: Boolean) {
    getTinyDb().putBoolean(key, value)
}

fun Context.getString(value: String): String {
    return getTinyDb().getString(value)
}

fun Context.putString(key: String, value: String) {
    getTinyDb().putString(key, value)
}

fun Context.getPrefInt(key: String): Int {
    return getTinyDb().getInt(key)
}

fun Context.getLangInt(key: String): Int {
    return getTinyDb().getSpinnerInt(key)
}

fun Context.putInt(key: String, value: Int) {
    getTinyDb().putInt(key, value)
}

fun Context.getTinyDb(): TinyDB {
    return TinyDB.getInstance(this)
}

fun Activity.launchMain(transition: Boolean = false, isSplashToHome: Boolean = false) {
    TinyDB.getInstance(this).setOnboarding(false)
    (this.application as TranslateApplication).backFromOnboarding = false
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtra("splashToMain", isSplashToHome)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    if (transition) {
        overridePendingTransition(0, 0)
    }
}

fun Activity.launchLangFromSplash(transition: Boolean = true) {
    val intent = Intent(this, AppLanguageActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    intent.putExtra("isFromSplash", true)
    startActivity(intent)
    if (transition) {
        overridePendingTransition(0, 0)
    }
}

fun Activity.launchOnboardingFromSplash(transition: Boolean = true) {
    val intent = Intent(this, OnboardingActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    intent.putExtra("isFromSplash", true)
    startActivity(intent)
    if (transition) {
        overridePendingTransition(0, 0)
    }
}

fun Activity.launchFavorite(type: String) {
    startActivityForResult(Intent(this, FavoriteActivity::class.java).apply {
        putExtra("from", type)
    }, REQUEST_CODE_HISTORY_FAVORITE)
}

fun Activity.launchSettings() {
    startActivityForResult(
        Intent(this, SettingsActivity::class.java),
        REQUEST_CODE_HISTORY_FAVORITE
    )
}

fun Activity.launchInApp() {
    startActivity(Intent(this, InAppActivity::class.java))
}

fun Activity.launchLanguageScreen(type: String, source: String) {
    val intent = Intent(this, LanguageSelectionActivity::class.java)
    intent.putExtra("language_type", type)
    intent.putExtra("language_source", source)
    startActivityForResult(intent, REQUEST_CODE_LANG_SELECTOR)
//    overridePendingTransition(R.anim.transit_bottom_top, R.anim.transit_none)

}


fun Activity.launchLanguageScreen(type: String) {
    val intent = Intent(this, LanguageSelectionOCR::class.java)
    intent.putExtra("language_type", type)
    startActivityForResult(intent, REQUEST_CODE_LANG_SELECTOR)
//    overridePendingTransition(R.anim.transit_bottom_top, R.anim.transit_none)

}

fun Activity.launchCameraScreen() {
    startActivityForResult(
        Intent(this, CameraActivity::class.java),
        REQUEST_CODE_OCR
    )
}

fun Activity.launchConversationScreen() {
    startActivityForResult(
        Intent(this, ConversationActivity::class.java),
        REQUEST_CODE_CONVERSATION
    )
}

fun Activity.launchInputScreen(inputType: String? = null, fromDocs: String? = null) {
    val intent = Intent(this, InputActivity::class.java)
    inputType?.let {
        intent.putExtra("has_data", "yes")
    } ?: run {
        intent.putExtra("has_data", "no")
    }
    fromDocs?.let {
        intent.putExtra("from_docs", true)
    } ?: run {
        intent.putExtra("from_docs", false)
    }
    intent.putExtra(INPUT_TYPE_KEY, inputType)
    startActivityForResult(intent, REQUEST_CODE_INPUT)
}

fun AppCompatActivity.getBottomDialogs(
    layout: Int,
    title: String = "",
    detail: String = "",
    onCancel: (() -> Unit),
    onDelete: (() -> Unit)
): Dialog {
    val exitDialog = Dialog(this, R.style.exitDialog)
    exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull<Window>(exitDialog.window)
        .setBackgroundDrawableResource(android.R.color.transparent)
    exitDialog.setContentView(layout)
    exitDialog.setCancelable(true)

    if (title.isNotEmpty()) {
        exitDialog.findViewById<TextView>(R.id.title_dialog)?.text = title
    }

    if (detail.isNotEmpty()) {
        exitDialog.findViewById<TextView>(R.id.text_dialog)?.text = detail
    }

    exitDialog.findViewById<Button>(R.id.btn_yes)?.setOnClickListener {
        onDelete.invoke()
    }

    exitDialog.findViewById<Button>(R.id.btn_no)?.setOnClickListener {
        onCancel.invoke()
    }

    val window: Window? = exitDialog.window
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(exitDialog.window?.attributes)
    val wlp = window!!.attributes
    wlp.gravity = Gravity.BOTTOM
    wlp.dimAmount = 0.7f
    window.attributes = wlp
    exitDialog.setCanceledOnTouchOutside(true)

    return exitDialog
}

fun AppCompatActivity.getIntroDialogs(
    title: String = "",
    detail: String = "",
    buttonText: String = "",
    onClick: (() -> Unit),
    onDismiss: (() -> Unit)
): Dialog {
    val exitDialog = Dialog(this, R.style.exitDialog)
    exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull<Window>(exitDialog.window)
        .setBackgroundDrawableResource(android.R.color.transparent)
    exitDialog.setContentView(R.layout.dlg_intro)
    exitDialog.setCancelable(true)

    exitDialog.findViewById<TextView>(R.id.titleTV)?.text = title
    exitDialog.findViewById<TextView>(R.id.detailTV)?.text = detail
    exitDialog.findViewById<TextView>(R.id.actionTV)?.text = buttonText

    exitDialog.findViewById<TextView>(R.id.actionTV)?.setOnClickListener {
        onClick.invoke()
    }

    exitDialog.findViewById<ImageView>(R.id.closeIV)?.setOnClickListener {
        onDismiss.invoke()
    }

    val window: Window? = exitDialog.window
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(exitDialog.window?.attributes)
    val wlp = window!!.attributes
    wlp.gravity = Gravity.CENTER
    wlp.dimAmount = 0.7f
    window.attributes = wlp
    exitDialog.setCanceledOnTouchOutside(true)

    return exitDialog
}

fun getLanguageObject(
    languageCode: String,
    languageName: String,
    langMean: String,
    countryCode: String,
): LanguageModel {
    return LanguageModel(languageCode, languageName, langMean, countryCode)
}

var mLangList: ArrayList<LanguageModel>? = null


fun fetchAllLanguages(): ArrayList<LanguageModel> {


    val langList = ArrayList<LanguageModel>()
    if (!mLangList.isNullOrEmpty()) {
        return mLangList!!
    } else {

        langList.add(getLanguageObject("af", "Afrikaans", "Afrikaans", "ZA"))
        langList.add(getLanguageObject("sq", "Albanian", "shqiptar", "AL"))
        langList.add(getLanguageObject("am", "Amharic", "አማርኛ", "ET"))
        langList.add(getLanguageObject("ar", "Arabic", "العربية", "SA"))
        langList.add(getLanguageObject("hy", "Armenian", "հայերեն", "AM"))
        langList.add(getLanguageObject("az", "Azerbaijani", "Azərbaycan", "AZ"))

        langList.add(getLanguageObject("eu", "Basque", "Euskal", "ES"))
        langList.add(getLanguageObject("be", "Belarusian", "Беларус", "BY"))
        langList.add(getLanguageObject("bn", "Bengali", "বাংলা", "BD"))
        langList.add(getLanguageObject("bs", "Bosnian", "bosanski", "BA"))
        langList.add(getLanguageObject("bg", "Bulgarian", "Български", "BG"))

        langList.add(getLanguageObject("ca", "Catalan", "Català", "ES"))
        langList.add(getLanguageObject("ceb", "Cebuano", "Cebuano", "PH"))
        langList.add(getLanguageObject("zh", "Chinese", "中文", "CN"))
        langList.add(getLanguageObject("co", "Corsican", "Corsu", "FR"))
        langList.add(getLanguageObject("hr", "Croatian", "Hrvatski", "HR"))
        langList.add(getLanguageObject("cs", "Czech", "Čeština", "CZ"))

        langList.add(getLanguageObject("da", "Danish", "Dansk", "DK"))
        langList.add(getLanguageObject("nl", "Dutch", "Nederlands", "NL"))

        langList.add(getLanguageObject("en", "English", "English", "US"))
        langList.add(getLanguageObject("eo", "Esperanto", "Esperanto", "AAE")) // espranto flag
        langList.add(getLanguageObject("et", "Estonian", "Eesti", "EE"))

        langList.add(getLanguageObject("fi", "Finnish", "Suomi", "FI"))
        langList.add(getLanguageObject("fr", "French", "Français", "FR"))
        langList.add(getLanguageObject("fy", "Frisian", "Frysk", "DE"))
        langList.add(getLanguageObject("tl", "Filipino", "Pilipino", "PH"))


        langList.add(getLanguageObject("gl", "Galician", "Galego", "ES"))
        langList.add(getLanguageObject("ka", "Georgian", "ქართული", "GE"))
        langList.add(getLanguageObject("de", "German", "Deutsch", "DE"))
        langList.add(getLanguageObject("el", "Greek", "Ελληνικά", "GR"))
        langList.add(getLanguageObject("gu", "Gujarati", "ગુજરાતી", "IN"))

        langList.add(getLanguageObject("ht", "Haitian Creole", "Haitian Creole Version", "HT"))
        langList.add(getLanguageObject("ha", "Hausa", "Hausa", "NG"))
        langList.add(getLanguageObject("haw", "Hawaiian", "ʻ .lelo Hawaiʻi", "AAH")) // hawai flag
        langList.add(getLanguageObject("he", "Hebrew", "עברית", "IL"))
        langList.add(getLanguageObject("hi", "Hindi", "हिंदी", "IN"))
        langList.add(getLanguageObject("hmn", "Hmong", "Hmong", "CN"))
        langList.add(getLanguageObject("hu", "Hungarian", "Magyar", "HU"))

        langList.add(getLanguageObject("is", "Icelandic", "Íslensku", "IS"))
        langList.add(getLanguageObject("ig", "Igbo", "Ndi Igbo", "NG"))
        langList.add(getLanguageObject("id", "Indonesian", "Indonesia", "ID"))
        langList.add(getLanguageObject("ga", "Irish", "Gaeilge", "IE"))
        langList.add(getLanguageObject("it", "Italian", "Italiano", "IT"))
        langList.add(getLanguageObject("ja", "Japanese", "日本語", "JP"))
        langList.add(getLanguageObject("jw", "Javanese", "Basa jawa", "ID"))
        langList.add(getLanguageObject("kn", "Kannada", "ಕನ್ನಡ", "IN"))
        langList.add(getLanguageObject("kk", "Kazakh", "Қазақ", "KZ"))
        langList.add(getLanguageObject("km", "Khmer", "ខខ្មែរ។", "TH"))
        langList.add(getLanguageObject("ko", "Korean", "한국어", "KR"))
        langList.add(getLanguageObject("ku", "Kurdish", "Kurdî", "IQ"))
        langList.add(getLanguageObject("ky", "Kyrgyz", "Кыргызча", "IQ"))

        langList.add(getLanguageObject("lo", "Lao", "ລາວ", "TH"))
        langList.add(getLanguageObject("la", "Latin", "Latine", "IT"))
        langList.add(getLanguageObject("lv", "Latvian", "Latviešu valoda", "LV"))
        langList.add(getLanguageObject("lt", "Lithuanian", "Lietuvių", "LT"))
        langList.add(getLanguageObject("lb", "Luxembourgish", "Lëtzebuergesch", "LU"))
        langList.add(getLanguageObject("mk", "Macedonian", "Македонски", "MK"))
        langList.add(getLanguageObject("mg", "Malagasy", "Malagasy", "MG"))
        langList.add(getLanguageObject("ms", "Malay", "Bahasa Melayu", "MY"))
        langList.add(getLanguageObject("ml", "Malayalam", "മലയാളം", "IN"))
        langList.add(getLanguageObject("mt", "Maltese", "Il-Malti", "MT"))
        langList.add(getLanguageObject("mi", "Maori", "Maori", "NZ"))
        langList.add(getLanguageObject("mr", "Marathi", "मराठी", "IN"))
        langList.add(getLanguageObject("mn", "Mongolian", "Монгол", "MN"))
        langList.add(getLanguageObject("my", "Myanmar", "မြန်မာ", "MM"))

        langList.add(getLanguageObject("ne", "Nepali", "नेपाली", "NP"))
        langList.add(getLanguageObject("nb", "Norwegian", "Norsk", "NO"))
        langList.add(getLanguageObject("ny", "Nyanja", "Nyanja", "MW"))
        langList.add(getLanguageObject("ps", "Pashto", "پښتو", "PK"))
        langList.add(getLanguageObject("fa", "Persian", "فارسی", "IR"))
        langList.add(getLanguageObject("pl", "Polish", "Polski", "PL"))
        langList.add(getLanguageObject("pt", "Portuguese", "Português", "PT"))
        langList.add(getLanguageObject("pa", "Punjabi", "ਪੰਜਾਬੀ", "IN"))
        langList.add(getLanguageObject("ro", "Romanian", "Română", "RO"))
        langList.add(getLanguageObject("ru", "Russian", "Pусский", "RU"))

        langList.add(getLanguageObject("gd", "Scots Gaelic", "Gàidhlig na h-Alba", "GB"))
        langList.add(getLanguageObject("sr", "Serbian", "Српски", "RS"))
        langList.add(getLanguageObject("sn", "Shona", "Shona", "ZW"))
        langList.add(getLanguageObject("sd", "Sindhi", "سنڌي", "PK"))
        langList.add(getLanguageObject("sk", "Slovak", "Slovenský", "SK"))
        langList.add(getLanguageObject("sl", "Slovenian", "Slovenščina", "SL"))
        langList.add(getLanguageObject("so", "Somali", "Soomaali", "SO"))
        langList.add(getLanguageObject("es", "Spanish", "Español", "ES"))
        langList.add(getLanguageObject("su", "Sundanese", "Urang Sunda", "ID"))
        langList.add(getLanguageObject("sw", "Swahili", "Kiswahili", "CD"))
        langList.add(getLanguageObject("sv", "Swedish", "Svenska", "SE"))

        langList.add(getLanguageObject("tg", "Tajik", "Точик", "TJ"))
        langList.add(getLanguageObject("ta", "Tamil", "தமிழ்", "IN"))
        langList.add(getLanguageObject("te", "Telugu", "తెలుగు", "IN"))
        langList.add(getLanguageObject("th", "Thai", "ไทย", "TH"))
        langList.add(getLanguageObject("tr", "Turkish", "Türk", "TR"))

        langList.add(getLanguageObject("uk", "Ukrainian", "Українська", "UA"))
        langList.add(getLanguageObject("ur", "Urdu", "اردو", "PK"))
        langList.add(getLanguageObject("uz", "Uzbek", "O'zbek", "UZ"))
        langList.add(getLanguageObject("vi", "Vietnamese", "Tiếng Việt", "VN"))
        langList.add(getLanguageObject("cy", "Welsh", "Cymraeg", "GB"))
        langList.add(getLanguageObject("xh", "Xhosa", "isiXhosa", "ZA"))
        langList.add(getLanguageObject("yi", "Yiddish", "יידיש", "DE"))
        langList.add(getLanguageObject("yo", "Yoruba", "Yoruba", "NG"))

        langList.add(getLanguageObject("zu", "Zulu", "IsiZulu", "ZA"))
        langList.sortWith { o1, o2 ->
            o1.languageName.compareTo(
                o2.languageName,
                ignoreCase = true
            )
        }

        mLangList = langList
        return langList

    }


}


fun Context.launchClipboardService() {
    kotlin.runCatching {
        val startIntent = Intent(this, getClipboardClass())
        startIntent.action = ACTION.START_ACTION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
    }
}


fun Context.stopClipboardService() {
    val stopIntent = Intent(this, getClipboardClass())
    stopIntent.action = ACTION.STOP_ACTION
    startService(stopIntent)
}

fun getClipboardClass(): Class<ClipboardService> {
    return ClipboardService::class.java
}


fun Context.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun isBelowAndroidTen(): Boolean {

    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}

fun Activity.shareWithFriends() {
//    TinyDB.getInstance(this).putBoolean("isOutside", true)
    isAppOpenAdShow(false)
    val packageName: String = packageName
    val appLink = "https://play.google.com/store/apps/details?id=$packageName"
    val appUri = Uri.parse(appLink)
    val shareBody = "Use one of the best Translator & Dictionary"
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareData = "$shareBody \n $appUri"
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareData)
    startActivity(
        Intent.createChooser(
            sharingIntent,
            resources.getString(R.string.share_using)
        )
    )


}

fun isDoubleClick(): Boolean {
    if (SystemClock.elapsedRealtime() - mLastClickTime < 300) {
        return false
    }
    mLastClickTime = SystemClock.elapsedRealtime()
    return true
}

fun isDoubleClickForCopy(): Boolean {
    if (SystemClock.elapsedRealtime() - mLastClickTime < 2500) {
        return false
    }
    mLastClickTime = SystemClock.elapsedRealtime()
    return true
}

fun Activity.shareWord(word: String) {
    if (isDoubleClick()) {
//        TinyDB.getInstance(this).putBoolean("isOutside", true)
        isAppOpenAdShow(false)
        val packageName = applicationContext.packageName
        val appLink = "https://play.google.com/store/apps/details?id=$packageName"
        val appUri = Uri.parse(appLink)

        val shareBody = "Use one of the best All Language Translator"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, word /*+ "\n" + appUri*/)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                resources.getString(R.string.share_using)
            )
        )

    }

}

fun openPrivacyPolicy(context: Context, url: String) {
    context.isAppOpenAdShow(false)
    try {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
    }
}

fun isAppInstalled(packageName: String, activity: Activity): Boolean {
    val pm = activity.packageManager
    return try {
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        pm.getApplicationInfo(packageName, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        false
    }

}


fun getLocale(targetLang: String): Locale {
    return when (targetLang) {
        "tl" -> {
            Locale("fil", "PH")
        }

        "id" -> {
            Locale("id", "ID")
        }

        "en" -> {
            Locale("en", "US")
        }

        "sq" -> {
            Locale("alb", "AL")
        }

        "fr" -> {
            Locale.FRANCE
        }

        "zh" -> {
            Locale.CHINA
        }

        "ur" -> {
            Locale("ur-PK")
        }

        "ar" -> {
            Locale("ar-SA")
        }

        "hr" -> {
            Locale("en", "UK")
        }

        "bs" -> {
            Locale("alb", "AL")
        }

        "sw" -> {
            Locale("swc", "CD")
        }

        "cy" -> {
            Locale("en", "UK")
        }

        else -> {
            Locale(targetLang)
        }
    }


}


fun Activity.sendEmail(
    to: Array<String>?,
    subject: String, @Suppress("SameParameterValue") body: String,
) {
    isAppOpenAdShow(false)
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    if (to != null && to.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_EMAIL, to)
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)

    this.launchIntent(Intent.createChooser(intent, "Send email..."))
}

private fun Activity.launchIntent(intent: Intent) {
    try {
        rawLaunchIntent(intent)
    } catch (ignored: ActivityNotFoundException) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Message intent failed!")
        builder.setPositiveButton("OK", null)
        builder.show()
    }

}

private fun Activity.rawLaunchIntent(intent: Intent?) {
    if (intent != null) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (ignored: AndroidRuntimeException) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.app_name)
            builder.setMessage("Message intent failed!")
            builder.setPositiveButton("OK", null)
            builder.show()
        }

    }
}

fun Activity.rateUs() {
//    TinyDB.getInstance(this).putBoolean("isOutside", true)
    isAppOpenAdShow(false)
    val packageName = applicationContext.packageName
    val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}

fun isLanguageSupportedForMic(translatedTarLangCode: String): Boolean {
    return !(translatedTarLangCode == "be" || translatedTarLangCode == "bs" || translatedTarLangCode == "ceb" ||
            translatedTarLangCode == "co" || translatedTarLangCode == "eo" || translatedTarLangCode == "fy" ||
            translatedTarLangCode == "ht" || translatedTarLangCode == "ha" || translatedTarLangCode == "haw" ||
            translatedTarLangCode == "hmn" || translatedTarLangCode == "ig" || translatedTarLangCode == "ga" ||
            translatedTarLangCode == "kk" || translatedTarLangCode == "ku" || translatedTarLangCode == "ky" ||
            translatedTarLangCode == "la" || translatedTarLangCode == "lb" || translatedTarLangCode == "mg" ||
            translatedTarLangCode == "mt" || translatedTarLangCode == "mi" || translatedTarLangCode == "ps" ||
            translatedTarLangCode == "gd" || translatedTarLangCode == "sn" || translatedTarLangCode == "sd" ||
            translatedTarLangCode == "so" || translatedTarLangCode == "tg" || translatedTarLangCode == "cy" ||
            translatedTarLangCode == "xh" || translatedTarLangCode == "yi" || translatedTarLangCode == "yo" ||
            translatedTarLangCode == "zu"

            )
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Activity.permissionAlreadyGranted(permission: String): Boolean {

    val result = ContextCompat.checkSelfPermission(
        this,
        permission
    )

    return result == PackageManager.PERMISSION_GRANTED

}

fun isSpeakerVisible(translatedTarLangCode: String): Boolean {
    return !(translatedTarLangCode == "sq" || translatedTarLangCode == "am" || translatedTarLangCode == "hy" ||
            translatedTarLangCode == "be" || translatedTarLangCode == "bg" || translatedTarLangCode == "ceb" ||
            translatedTarLangCode == "co" || translatedTarLangCode == "ka" ||
            translatedTarLangCode == "gu" || translatedTarLangCode == "ht" || translatedTarLangCode == "he" ||
            translatedTarLangCode == "hmn" || translatedTarLangCode == "jw" ||
            translatedTarLangCode == "kn" || translatedTarLangCode == "kk" || translatedTarLangCode == "ku" ||
            translatedTarLangCode == "ky" || translatedTarLangCode == "lo" || translatedTarLangCode == "la" ||
            translatedTarLangCode == "lv" || translatedTarLangCode == "lt" || translatedTarLangCode == "mk" ||
            translatedTarLangCode == "ml" || translatedTarLangCode == "mi" || translatedTarLangCode == "mr" ||
            translatedTarLangCode == "mn" || translatedTarLangCode == "my" || translatedTarLangCode == "ny" ||
            translatedTarLangCode == "ps" || translatedTarLangCode == "fa" || translatedTarLangCode == "pa" ||
            translatedTarLangCode == "sd" || translatedTarLangCode == "su" || translatedTarLangCode == "tg" ||
            translatedTarLangCode == "sl" || translatedTarLangCode == "te" ||
            translatedTarLangCode == "xh" || translatedTarLangCode == "yi"
            )
}

fun decodeBitMap(bitmap: Bitmap? = null, resultBitmap: (Bitmap?) -> Unit) {
    var finalBitmap: Bitmap? = null
    CoroutineScope(Dispatchers.IO).launch {
        finalBitmap = convertBitmap(bitmap)
        withContext(Dispatchers.Main) {
            resultBitmap.invoke(finalBitmap)
        }

    }

}

fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}


fun convertBitmap(bitmap: Bitmap?): Bitmap? {
    return try {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        if (byteArrayOutputStream.toByteArray().size / 1024 > 1024) {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        }
        val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(byteArrayInputStream, null, options)
        options.inJustDecodeBounds = false
        val i: Int = options.outWidth
        val i2: Int = options.outHeight
        // int i3 = (i <= i2 || ((float) i) <= 2100.0f) ? (i >= i2 || ((float) i2) <= 2100.0f) ? 1 : ((int) (((float) options.outHeight) / 2100.0f)) + 1 : ((int) (((float) options.outWidth) / 2100.0f)) + 1;
        var i3 =
            if (i <= i2 || i.toFloat() <= 2100.0f) if (i >= i2 || i2.toFloat() <= 2100.0f) 1 else (options.outHeight.toFloat() / 2100.0f).toInt() + 1 else (options.outWidth.toFloat() / 2100.0f).toInt() + 1
        if (i3 <= 0) {
            i3 = 1
        }
        options.inSampleSize = i3
        val byteArrayInputStream2 = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        val decodeStream: Bitmap =
            BitmapFactory.decodeStream(byteArrayInputStream2, null, options)!!
        runCatching {
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
            byteArrayInputStream2.close()
        }.onFailure {
            null
        }
        decoder(decodeStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun decoder(bitmap: Bitmap): Bitmap? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    var i = 90
    while (byteArrayOutputStream.toByteArray().size / 1024 > 1024) {
        byteArrayOutputStream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream)
        i -= 10
    }
    val decodeStream = BitmapFactory.decodeStream(
        ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
        null,
        null
    )
    try {
        byteArrayOutputStream.flush()
        byteArrayOutputStream.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return decodeStream
}

fun getImageBitMap(bitmap: Bitmap?, i: Int): Bitmap? {
    return if (bitmap == null) {
        null
    } else try {
        val matrix = Matrix()
        matrix.postRotate(i.toFloat())
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }
}

fun Activity.loadExitRatingDialog(isExit: Boolean) {
    val exitDialog = getCustomDialog(R.layout.dialog_rate_us, R.style.customDialog)

    exitDialog.apply {
        setCanceledOnTouchOutside(true)

        val pTvLibRateButton = findViewById<AppCompatButton>(R.id.lib_rate_button)
        val ratingBar = findViewById<ScaleRatingBar>(R.id.rating_bar)
        val tvExit = findViewById<TextView>(R.id.tv_exit)
        if (isExit) {
            tvExit.text = "Exit App"
        } else {
            tvExit.text = getString(R.string.cancel)
        }


        pTvLibRateButton.isEnabled = false
        pTvLibRateButton.isClickable = false
        pTvLibRateButton.alpha = 0.5f
        pTvLibRateButton.text = getString(R.string.label_rate_us)

        tvExit.setOnClickListener {
            dismiss()
            if (isExit) {
                finish()
            }

        }

        pTvLibRateButton.setOnClickListener {
            if (isExit) {
                putBoolean("rating_done", true)
            }
            dismiss()
            if (pTvLibRateButton.text.toString()
                    .trim() == getString(R.string.label_rate_us)
            ) rateUs() else sendEmail(
                arrayOf(resources.getString(R.string.email_address)),
                resources.getString(R.string.email_subject),
                ""
            )
        }


        ratingBar.setOnRatingChangeListener { _, rating, _ ->
            when (rating) {
                0F -> {
                    enableRatingButton(
                        pTvLibRateButton,
                        false,
                        getString(R.string.label_rate_us)
                    )

                }

                1F -> {
                    enableRatingButton(
                        pTvLibRateButton,

                        true,
                        getString(R.string.action_feedback)
                    )
                }

                2F -> {
                    enableRatingButton(
                        pTvLibRateButton,

                        true,
                        getString(R.string.action_feedback)
                    )
                }

                3F -> {
                    enableRatingButton(
                        pTvLibRateButton,

                        true,
                        getString(R.string.action_feedback)
                    )
                }

                4F -> {
                    enableRatingButton(
                        pTvLibRateButton,

                        true,
                        getString(R.string.label_rate_us)
                    )

                }

                5F -> {
                    enableRatingButton(
                        pTvLibRateButton,
                        true,
                        getString(R.string.label_rate_us)
                    )
                }

            }
        }
    }
    exitDialog.setOnKeyListener { dialog, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog?.dismiss()
        }
        false
    }

    exitDialog.show()
}

/*
fun Activity.exitDialog() {
    val exitDialog = getCustomDialog(R.layout.dlg_exit_new, R.style.customDialog)
    exitDialog.apply {
        setCanceledOnTouchOutside(true)

        val cancelTV = findViewById<TextView>(R.id.cancelTV)
        val exitTV = findViewById<TextView>(R.id.exitTV)
    }
    exitDialog.setOnKeyListener { dialog, keyCode, _ ->
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialog?.dismiss()
        }
        false
    }

    exitDialog.show()
}
*/

private fun enableRatingButton(
    pTvLibRateButton: Button,
    isEnabled: Boolean,
    text: String,
) {
    if (isEnabled) {
        pTvLibRateButton.alpha = 1f

        pTvLibRateButton.isClickable = true
        pTvLibRateButton.isEnabled = true
    } else {
        pTvLibRateButton.alpha = 0.5f

        pTvLibRateButton.isClickable = false
        pTvLibRateButton.isEnabled = false
    }

    pTvLibRateButton.text = text
}


fun Activity.getCustomDialog(layout: Int, themeId: Int): Dialog {

    val permissionDialog = Dialog(this, themeId)
    permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    permissionDialog.setCancelable(false)
    permissionDialog.setCanceledOnTouchOutside(false)
    permissionDialog.setContentView(layout)
    window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    val window = permissionDialog.window
    if (window != null) {
        //            window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val windowLayoutParams = window.attributes
        windowLayoutParams.dimAmount = 0.7f
    }

    return permissionDialog

}

fun Activity.launchFullScreen(word: String) {
    val fullScreenIntent = Intent(this, FullScreenActivity::class.java)
    fullScreenIntent.putExtra("full_screen_text", word)
    startActivity(fullScreenIntent)
}

fun Context.getFlags(): ArrayList<Int> {
    return arrayListOf(
        R.drawable.afrikaans_flag,
        R.drawable.arabic_flag,
        R.drawable.bangali_flag,
        R.drawable.brazil_flag,
        R.drawable.chinese_flag,
        R.drawable.czech_flag,
        R.drawable.danish_flag,
        R.drawable.dutch_flag,
        R.drawable.us_flag,
        R.drawable.philippines_flag,
        R.drawable.french_flag,
        R.drawable.german_flag,
        R.drawable.indian_flag,
        R.drawable.indonesian_flag,
        R.drawable.italian_flag,
        R.drawable.japnese_flag,
        R.drawable.korean_flag,
        R.drawable.malysia_flag
    )
}

fun Context.languageCode(): List<String> {
    return listOf(
        getString(R.string.african_code),
        getString(R.string.arabic_code),
        getString(R.string.bangali_code),
        getString(R.string.brazil_code),
        getString(R.string.chinese_code),
        getString(R.string.czech_code),
        getString(R.string.danish_code),
        getString(R.string.dutch_code),
        getString(R.string.us_code),
        getString(R.string.flipino_code),
        getString(R.string.french_code),
        getString(R.string.german_code),
        getString(R.string.hindi_code),
        getString(R.string.indonasia_code),
        getString(R.string.italian_code),
        getString(R.string.japnese_code),
        getString(R.string.korean_code),
        getString(R.string.malysia_code)
    )
}

fun Context.languageList(): List<String> {
    return listOf(
        getString(R.string.african_name),
        getString(R.string.arabic_name),
        getString(R.string.bangali_name),
        getString(R.string.brazil_name),
        getString(R.string.chinese_name),
        getString(R.string.czech_name),
        getString(R.string.danish_name),
        getString(R.string.dutch_name),
        getString(R.string.united_states_name),
        getString(R.string.flipino_name),
        getString(R.string.french_name),
        getString(R.string.german_name),
        getString(R.string.hindi_name),
        getString(R.string.indonasia_name),
        getString(R.string.italian_name),
        getString(R.string.japnese_name),
        getString(R.string.korean_name),
        getString(R.string.malysia_name),
    )
}

fun Context.getDefaultLangCode(code: String): String {
    val langCodes = languageCode()
    var langCode = getString(R.string.us_code)
    for (i in langCodes) {
        if (i == code)
            langCode = i
    }
    return langCode
}

fun Context.getSelectedLanguageCode(name: String): String {
    val languageList = languageList()
    var lanCode = getString(R.string.us_code)
    for (i in languageList.indices) {
        if (name == languageList[i]) {
            lanCode = languageCode()[i]
        }
    }
    return lanCode
}

fun Context.getSelectedLanguageName(code: String): String {
    val languageCodeList = languageCode()
    var langName = getString(R.string.united_states_name)
    for (i in languageCodeList.indices) {
        if (code == languageCodeList[i]) {
            langName = languageList()[i]
        }
    }
    return langName
}

fun convertWordText(word: WordResponse): String {
    if (word != null) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("${word[0].word} \n\n")
        for (word in word) {
            if (word.phonetics.isNotEmpty()) {
                stringBuilder.append("Phonetics: ${word.phonetics[0].text} \n\n")
            }
            for (meaning in word.meanings) {
                stringBuilder.append("${meaning.partOfSpeech} \n\n")
                for (definition in meaning.definitions) {
                    stringBuilder.append("• ${definition.definition} \n")
                    if (definition.example != null) {
                        stringBuilder.append("Example: ${definition.example} \n")
                    }
                }
                if (meaning.synonyms.isNotEmpty()) {
                    stringBuilder.append("Synonyms: ${meaning.synonyms.joinToString(", ")} \n")
                }
                if (meaning.antonyms.isNotEmpty()) {
                    stringBuilder.append("Antonyms: ${meaning.antonyms.joinToString(", ")} \n")
                }
                stringBuilder.append("\n")
            }
        }
        return stringBuilder.toString()
    }
    return ""
}

fun Activity.shareWordData(word: WordResponse) {
    val shareBody = convertWordText(word)
    val appLink = getString(R.string.app_name) + "\n" +
            "https://play.google.com/store/apps/details?id=$packageName"
//    val appUri = Uri.parse(appLink)
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    sharingIntent.putExtra(Intent.EXTRA_TEXT, appLink + "\n\n" + shareBody)
    startActivity(
        Intent.createChooser(
            sharingIntent,
            getString(R.string.share_using)
        )
    )
}

fun Activity.copyWordData(word: WordResponse) {
    val copyBody = convertWordText(word)
    if (copyBody.isNotEmpty()) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("translator2", copyBody)
        clipboard.setPrimaryClip(clip)
        showToast(getString(R.string.text_copied_successfully), 0)
    }
}

fun Context.canPostNotification(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

//fun Context.sendTracking(
//    eventName: String,
//    vararg param: Pair<String, String?>
//) {
////    Toast.makeText(this, param[1].second, Toast.LENGTH_SHORT).show()
//    IKTrackingHelper.sendTracking(eventName, *param)
//}

fun getPhraseLanguages(): ArrayList<LanguageModel> {
    return arrayListOf(
        LanguageModel("ar-EG", "Arabic (Egypt)", "", "ar-EG"),
        LanguageModel("ar-SA", "Arabic (Saudi Arabia)", "", "ar-SA"),
        LanguageModel("ar-AE", "Arabic (United Arab Emirates)", "", "ar-AE"),
        LanguageModel("ca", "Catalan", "", "ca"),
        LanguageModel("zh-HK", "Chinese (Hong Kong)", "", "zh-HK"),
        LanguageModel("zh-CN", "Chinese (Simplified)", "", "zh-CN"),
        LanguageModel("zh-TW", "Chinese (Traditional)", "", "zh-TW"),
        LanguageModel("hr", "Croatian", "", "hr"),
        LanguageModel("cs", "Czech", "", "cs"),
        LanguageModel("da", "Danish", "", "da"),
        LanguageModel("nl", "Dutch", "", "nl"),
        LanguageModel("en-US", "English", "", "en-US"),
        LanguageModel("en-AU", "English (Australia)", "", "en-AU"),
        LanguageModel("en-UK", "English (UK)", "", "en-UK"),
        LanguageModel("fi", "Finnish", "", "fi"),
        LanguageModel("fr-CA", "French (Canada)", "", "fr-CA"),
        LanguageModel("fr-FR", "French (France)", "", "fr-FR"),
        LanguageModel("de", "German", "", "de"),
        LanguageModel("el", "Greek", "", "el"),
        LanguageModel("iw", "Hebrew", "", "iw"),
        LanguageModel("hi", "Hindi", "", "hi"),
        LanguageModel("hu", "Hungarian", "", "hu"),
        LanguageModel("id", "Indonesian", "", "id"),
        LanguageModel("it", "Italian", "", "it"),
        LanguageModel("ja", "Japanese", "", "ja"),
        LanguageModel("ko", "Korean", "", "ko"),
        LanguageModel("ms", "Malay", "", "ms"),
        LanguageModel("no", "Norwegian", "", "no"),
        LanguageModel("pl", "Polish", "", "pl"),
        LanguageModel("pt-BR", "Portuguese (Brazil)", "", "pt-BR"),
        LanguageModel("pt-PT", "Portuguese (Portugal)", "", "pt-PT"),
        LanguageModel("ro", "Romanian", "", "ro"),
        LanguageModel("ru", "Russian", "", "ru"),
        LanguageModel("sk", "Slovak", "", "sk"),
        LanguageModel("es-MX", "Spanish (Mexico)", "", "es-MX"),
        LanguageModel("es-ES", "Spanish (Spain)", "", "es-ES"),
        LanguageModel("es-US", "Spanish (US)", "", "es-US"),
        LanguageModel("sw", "Swahili", "", "sw"),
        LanguageModel("sv", "Swedish", "", "sv"),
        LanguageModel("th", "Thai", "", "th"),
        LanguageModel("tr", "Turkish", "", "tr"),
        LanguageModel("uk", "Ukrainian", "", "uk"),
        LanguageModel("vi", "Vietnamese", "", "vi")
    )
}

fun Context.getPhraseLanguagesList(): ArrayList<LanguageModel> {
    val list = getListFromJson(this)
    Log.e("none.", "getPhraseLanguagesList: ${list[0]}")
    val resultList: ArrayList<LanguageModel> = ArrayList()
    for (it in list) {
        resultList.add(
            LanguageModel(
                it.lang_code,
                it.name,
                "",
                it.lang_code
            )
        )
    }
    return resultList
}

fun getListFromJson(context: Context): PhraseLanguages {
    val jsonFileString = getJsonDataFromAsset(context, "phrase_langs.json")
    val gson = Gson()
    val listPersonType = object : TypeToken<PhraseLanguages>() {}.type
    return gson.fromJson(jsonFileString, listPersonType)
}

fun getJsonDataFromAsset(context: Context, fileName: String): String? {
    val jsonString: String
    try {
        jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        return null
    }
    return jsonString
}

fun TabLayout.TabView.setDefaultMaxLines(value: Int) {
    TabLayout.TabView::class.java
        .getDeclaredField("defaultMaxLines")
        .apply { isAccessible = true }.set(this, value)
}

fun String.toLowerCaseExceptFirst(): String {
    if (isNotEmpty()) {
        val firstChar = substring(0, 1)
        val restOfString = substring(1)
        return firstChar + restOfString.lowercase()
    }
    return this
}

@SuppressLint("DiscouragedApi")
fun Context.getResourceIconId(icon: String?): Int {
    return resources.getIdentifier(icon?.lowercase(Locale.getDefault()), "drawable", packageName)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getPhraseMicCode(langCode: String): String? {
    when (langCode) {
        "af" -> return "af-ZA"
        "am" -> return "am-ET"
        "sq" -> return "sq-AL"
        "ar" -> return "ar-SA"
        "ar-EG" -> return "ar-SA"
        "ar-AE" -> return "ar-SA"
        "hy" -> return "hy-AM"
        "az" -> return "az-AZ"
        "eu" -> return "eu-ES"
        "bn" -> return "bn-BD"
        "bg" -> return "bg-BG"
        "ca" -> return "ca-ES"
        "zh" -> return "cmn-Hans-CN"
        "zh-HK" -> return "cmn-Hans-CN"
        "zh-CN" -> return "cmn-Hans-CN"
        "zh-TW" -> return "cmn-Hans-CN"
        "hr" -> return "hr-HR"
        "cs" -> return "cs-CZ"
        "da" -> return "da-DK"
        "nl" -> return "nl-NL"
        "en" -> return "en-US"
        "en-US" -> return "en-US"
        "en-AU" -> return "en-US"
        "en-UK" -> return "en-US"
        "et" -> return "et-EE"
        "fi" -> return "fi-FI"
        "fr" -> return "fr-FR"
        "fr-FR" -> return "fr-FR"
        "fr-CA" -> return "fr-FR"
        "tl" -> return "fil-PH"
        "gl" -> return "gl-ES"
        "ka" -> return "ka-GE"
        "de" -> return "de-DE"
        "el" -> return "el-GR"
        "gu" -> return "gu-IN"
        "he" -> return "he-IL"
        "iw" -> return "he-IL"
        "hi" -> return "hi-IN"
        "hu" -> return "hu-HU"
        "is" -> return "is-IS"
        "id" -> return "id-ID"
        "it" -> return "it-IT"
        "ja" -> return "ja-JP"
        "jw" -> return "jw-ID"
        "kn" -> return "kn-IN"
        "km" -> return "km-KH"
        "ko" -> return "ko-KR"
        "lo" -> return "lo-LA"
        "lv" -> return "lv-LV"
        "lt" -> return "lt-LT"
        "ms" -> return "ms-MY"
        "ml" -> return "ml-IN"
        "mr" -> return "mr-IN"
        "my" -> return "my-MM"
        "ne" -> return "ne-NP"
        "nb" -> return "nb-NO"
        "no" -> return "nb-NO"
        "fa" -> return "fa-IR"
        "pl" -> return "pl-PL"
        "pt" -> return "pt-PT"
        "pt-BR" -> return "pt-PT"
        "pt-PT" -> return "pt-PT"
        "pa" -> return "pa-Guru-IN"
        "ro" -> return "ro-RO"
        "ru" -> return "ru-RU"
        "sr" -> return "sr-RS"
        "sk" -> return "sk-SK"
        "sl" -> return "sl-SI"
        "es" -> return "es-ES"
        "es-ES" -> return "es-ES"
        "es-MX" -> return "es-ES"
        "es-US" -> return "es-ES"
        "su" -> return "su-ID"
        "sw" -> return "sw-TZ"
        "sv" -> return "sv-SE"
        "ta" -> return "ta-IN"
        "te" -> return "te-IN"
        "th" -> return "th-TH"
        "tr" -> return "tr-TR"
        "uk" -> return "uk-UA"
        "ur" -> return "ur-PK"
        "uz" -> return "uz-UZ"
        "vi" -> return "vi-VN"
        "zu" -> return "zu-ZA"
        else -> {
            return null
        }
    }
}