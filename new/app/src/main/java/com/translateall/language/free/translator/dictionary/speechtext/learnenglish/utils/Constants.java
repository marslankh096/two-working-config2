package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R;

import java.util.ArrayList;
import java.util.List;


public class Constants {

    public static Long mLastClickTime = 0L;
    public static final String DOCS_FILE_TYPE = "0";
    public static final String PPT_FILE_TYPE = "2";
    public static final String SPLASH_FIRST = "is_first_splash";
    public static final String SAVE_DIR_NAME = "background";
    public static final String IS_FILES_DOWNLOADED = "is_file_downloaded";
    public static final String DIALOG_SESSION = "dialog_session";
    public static final String SHOW_LIKE_DIALOG = "show_rate_us";
    public static final String STICKY_SERVICE = "sticky_service";
    public static final String CHECK_INTER_AD_SHOW = "check_interstitial_ad";
    public static final String TABLE_NAME_TRANSLATION = "translation";
    public static final String TABLE_NAME_FAVORITE = "favorites";
    public static final String TABLE_NAME_LANGUAGE = "languages";
    public static final String TABLE_NAME_WORDS = "words";
    public static final String DB_NAME = "translation.db";
    public static final String DB_NAME_CONVERSATION = "conversation.db";
    public static final String INPUT_WORD = "inputword";

    public static final String WORD_OF_THE_DAY = "word_of_the_day";
    public static final String CLIP_BOARD_SERVICE = "enable_copy";
    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAurv41sEaji5UeJPWhNehC4Nb5P5zVAyYDk/jiO2/J3gEpm93XEMCdfJfzVVc0DJVCs6pSQ6If0t4PxTrq/oTtIPUcUeIzMoZBxZt2SvEK2AnZbs2ruD2dIey0qdV/zCKoSw/90Xa1H6ATmZ5D39DOcuxRMueDrab2mK5hAOOQ9W394kRT8alUolMtXeGFnTl71wHmAuuy5TX863uQySfEY++bE529bXQtu4GoYKK+zQtSxw03QY0LN87WvPy6+tVEWyy6pfTE2dVNyWF1rIyWq+QtJQuQcoJ+yKNIq/kAkYJsiwaE+ErxwDonBxfUSPXPzT7liZ/ElOyTILktbyytwIDAQAB";
    public static final String MERCHANT_ID = "17797136114525812522";
    public static final String SUBSCRIPTION_ID_MONTH = "month_subscription";
    public static final String SUBSCRIPTION_ID_YEAR = "year_subscription_1";
    public static final String SUBSCRIPTION_ID_FREE_TRIAL = "free_trial_subscription";
    public static final String IN_APP_NAME_REMOVE_ADS = "remove_ads";
    public final static String BACKGROUND_URL = "http://www.clicksolapps.com/dictionary/background/";
    public final static String BUNDLE_TARGET = "tr_lang";
    public final static String BUNDLE_TARGET_CODE = "tr_lang_code";
    public final static String BUNDLE_SOURCE = "src_lang";
    public final static String BUNDLE_SOURCE_CODE = "src_lang_code";
    public final static String TRANSLATION_API_KEY = "translation_api_key";
    public final static String APP_VERSION = "app_version";
    public final static String BACK_PRESS_VALUE = "back_press_value";
    public final static String TRANSLATION_FROM = "is_from_api";

    public final static String FIRE_IS_SMALL_RECT_BANNER = "is_small_rect_banner";
    public final static String FIRE_IS_RECT_BANNER_SHOW = "is_rect_banner_show";
    public final static String SEARCH_COUNT = "search_count";
    public static final String GOOGLE_API_KEY = "g_translation_api_key";

    public static final String SUBSCRIPTION_KEY = "subscription_key";
    public static final String SUBSCRIPTION_REGION = "subscription_region";
    public static final String LOCK_SCREEN_AD = "is_lock_screen_ad";
    public static final String FACEBOOK_PRIORITY = "is_fb_ad_priority";
    public static final String LOCK_SCREEN_SERVICE = "is_lock_screen";
    public static final String ON_REUME_AD = "on_resume_ad_time";

    public static final String SRC_LANG_NAME = "convo_src_lang_name";
    public static final String SRC_LANG_CODE = "convo_src_lang_code";
    public static final String SRC_COUNTRY_CODE = "convo_src_country_code";
    public static final String TAR_LANG_NAME = "convo_tar_lang_name";
    public static final String TAR_LANG_CODE = "convo_tar_lang_code";
    public static final String TAR_COUNTRY_CODE = "convo_tar_country_code";

    public static final String INTENT_KEY_NAME_INPUT = "key_name_input";
    public static final String INTENT_KEY_CODE_INPUT = "key_code_input";
    public static final String INTENT_KEY_POSITION_INPUT = "key_position_input";
    public static final String INTENT_KEY_WORD_INPUT = "key_word_input";
    public static final String INTENT_KEY_NAME_OUTPUT = "key_name_output";
    public static final String INTENT_KEY_CODE_OUTPUT = "key_code_output";
    public static final String INTENT_KEY_POSITION_OUTPUT = "key_position_output";
    public static final String INTENT_KEY_WORD_OUTPUT = "key_word_output";


    public static final String IS_PREMIUM = "is_premium";
    public static final String PRIVACY_URL = "https://apps-wing.blogspot.com/2021/07/privacy-policy.html";
    public static final String TERMS_CONDITION = "https://apps-wing.blogspot.com/2021/07/terms-conditions.html";

    public static final String yearlyId ="yearly_13_99";
    public static final String monthlyId = "monthly_6_99";
    public static final String weeklyId = "weekly_2_99";

    public static final String INPUT_TYPE_KEY_TEXT = "text";
    public static final String INPUT_TYPE_KEY_MIC = "mic";
    public static final String INPUT_TYPE_KEY = "input_type";

    public static final String SOURCE_LANG_CODE = "source_language_code_online";
    public static final String SOURCE_LANG_NAME = "source_language_name_online";
    public static final String SOURCE_LANG_POSITION = "source_language_position";

    public static final String SOURCE_LANG_CODE_OCR = "source_language_code_ocr";
    public static final String SOURCE_LANG_NAME_OCR = "source_language_name_ocr";
    public static final String SOURCE_LANG_POSITION_OCR = "source_language_pos_ocr";

    public static final String TARGET_LANG_CODE = "target_language_code_online";
    public static final String TARGET_LANG_NAME = "target_language_name_online";
    public static final String TARGET_LANG_POSITION = "target_language_position";

    public static final String TARGET_LANG_CODE_OCR = "target_language_code_ocr";
    public static final String TARGET_LANG_NAME_OCR = "target_language_name_ocr";
    public static final String TARGET_LANG_POSITION_OCR = "target_language_ocr";


    public static final String KEY_PHRASE_INPUT_LANG_NAME = "input_phrase_lang_name";
    public static final String KEY_PHRASE_INPUT_LANG_CODE = "input_phrase_lang_code";
    public static final String KEY_PHRASE_INPUT_LANG_POSITION = "input_phrase_lang_position";
//    public static final String KEY_PHRASE_INPUT_LANG_MEANING = "input_phrase_lang_meaning";

    public static final String KEY_PHRASE_TRANSLATED_LANG_NAME = "translated_phrase_lang_name";
    public static final String KEY_PHRASE_TRANSLATED_LANG_CODE = "translated_phrase_lang_code";
    public static final String KEY_PHRASE_TRANSLATED_LANG_POSITION = "translated_phrase_lang_position";
//    public static final String KEY_PHRASE_TRANSLATED_LANG_MEANING = "translated_phrase_lang_meaning";


    public static final int DEFAULT_SRC_LANG_POSITION = 19;
    public static final int DEFAULT_SRC_LANG_POSITION_OCR = 12;
    public static final int DEFAULT_SRC_LANG_POSITION_PHRASE = 11;
    public static final int DEFAULT_TAR_LANG_POSITION = 24;
    public static final int DEFAULT_TAR_LANG_POSITION_PHRASE = 16;


    public static final String LANGUAGE_TYPE_SOURCE = "source";
    public static final String LANGUAGE_TYPE_TARGET = "target";
    public static final String LANGUAGE_LIST_TYPE_ALL = "list_all";
    public static final String LANGUAGE_LIST_TYPE_OCR = "list_ocr";
    public static final String LANGUAGE_LIST_TYPE_PHRASE = "list_phrase";
    public static final String LANGUAGE_SOURCE_PHRASE = "language_source_phrase";

    public static final String VOICE_MALE = "male";
    public static final String VOICE_FEMALE = "female";


    public static final int NOTIFICATION_ID_FOREGROUND_SERVICE = 8466503;
    public static final int REQ_CODE_SPEECH_INPUT = 100;
    public static final int REQUEST_CODE_LANG_SELECTOR = 10;
    public static final int REQUEST_CODE_HISTORY_FAVORITE = 11;
    public static final int REQUEST_CODE_CONVERSATION = 12;
    public static final int REQUEST_CODE_OCR = 13;
    public static final int REQUEST_CODE_GALLERY = 14;
    public static final int REQUEST_CODE_INPUT = 15;
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 110;

    public static final String INTENT_HISTORY = "history";
    public static final String INTENT_FAVORITE = "favorite";

    public static final String LANGUAGE_PREF = "translatorAppLanguage";

    public static class STATE_SERVICE {
        public static final int CONNECTED = 10;
        public static final int NOT_CONNECTED = 0;
    }

    public static class ACTION {
        public static final String MAIN_ACTION = "com.translateall.language.free.translator.dictionary.speechtext.learnenglish.action.main";
        public static final String START_ACTION = "com.translateall.language.free.translator.dictionary.speechtext.learnenglish.action.start";
        public static final String STOP_ACTION = "com.translateall.language.free.translator.dictionary.speechtext.learnenglish.action.stop";
    }


    public static void darkenStatusBar(Activity activity, int color) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            activity.getWindow().setStatusBarColor(
                    darkenColor(
                            ContextCompat.getColor(activity, color)));
        }

    }


    // Code to darken the color supplied (mostly color of toolbar)
    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }

    public static boolean isOnline(Activity activity) {
        final NetworkInfo networkInfo = ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String removeSpans(String word) {
        String newWord = word;
        newWord = newWord.replace("<span class=\"searchmatch\">", "");
        newWord = newWord.replace("</span>", "");
        return newWord;
    }

    public static String removeBraces(String word) {
        String newWord = word;
        newWord = newWord.replace("[", "");
        newWord = newWord.replace("]", "");
        return newWord;

    }

    public static String removeSynoBrace(String word) {
        String newWord = word;
        newWord = newWord.replace("(", "");
        newWord = newWord.replace("similar term", "");
        newWord = newWord.replace(")", "");
        newWord = newWord.replace("|", " , ");

        return newWord;
    }

    public static boolean containsWhiteSpace(final String testCode) {
        if (testCode != null) {
            for (int i = 0; i < testCode.length(); i++) {
                if (Character.isWhitespace(testCode.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }


    public static List<Integer> getOffensiveWordsList() {
        List<Integer> offensiveWordsList = new ArrayList<>();
        offensiveWordsList.add(5517);
        offensiveWordsList.add(326);// no word
        offensiveWordsList.add(327); // no word
        offensiveWordsList.add(349);
        offensiveWordsList.add(8113);
        offensiveWordsList.add(1341);
        offensiveWordsList.add(1234);
        offensiveWordsList.add(1900);
        offensiveWordsList.add(1796);
        offensiveWordsList.add(2638);
        offensiveWordsList.add(7261);
        offensiveWordsList.add(7303);
        offensiveWordsList.add(8253);
        offensiveWordsList.add(8815);
        offensiveWordsList.add(9024);
        offensiveWordsList.add(9881);
        offensiveWordsList.add(9882);
        offensiveWordsList.add(9883);
        offensiveWordsList.add(9884);
        offensiveWordsList.add(9885);
        offensiveWordsList.add(9886);
        offensiveWordsList.add(9899);
        offensiveWordsList.add(9900);
        offensiveWordsList.add(9931);
        offensiveWordsList.add(18748);
        offensiveWordsList.add(18749);
        offensiveWordsList.add(18750);
        offensiveWordsList.add(18888);
        offensiveWordsList.add(19856);
        offensiveWordsList.add(21109);
        offensiveWordsList.add(21781);
        offensiveWordsList.add(23730);
        offensiveWordsList.add(23731);
        offensiveWordsList.add(23732);
        offensiveWordsList.add(25438);
        offensiveWordsList.add(25439);
        offensiveWordsList.add(25440);
        offensiveWordsList.add(25441);
        offensiveWordsList.add(25442);
        offensiveWordsList.add(15027);
        offensiveWordsList.add(27421);
        offensiveWordsList.add(27422);
        offensiveWordsList.add(27423);
        offensiveWordsList.add(27424);
        offensiveWordsList.add(27425);
        offensiveWordsList.add(27426);
        offensiveWordsList.add(27427);
        offensiveWordsList.add(27428);
        offensiveWordsList.add(27429);
        offensiveWordsList.add(27430);
        offensiveWordsList.add(27431);
        offensiveWordsList.add(27432);
        offensiveWordsList.add(27433);
        offensiveWordsList.add(27434);
        offensiveWordsList.add(27435);
        offensiveWordsList.add(27436);
        offensiveWordsList.add(27437);
        offensiveWordsList.add(27438);
        offensiveWordsList.add(27439);
        offensiveWordsList.add(27440);
        offensiveWordsList.add(27441);
        offensiveWordsList.add(34136);
        offensiveWordsList.add(27690);
        offensiveWordsList.add(28361);
        offensiveWordsList.add(28362);
        offensiveWordsList.add(28363);
        offensiveWordsList.add(28364);
        offensiveWordsList.add(30431);
        offensiveWordsList.add(30432);
        offensiveWordsList.add(30433);
        offensiveWordsList.add(30433);
        offensiveWordsList.add(30434);
        offensiveWordsList.add(30435);
        offensiveWordsList.add(30436);
        offensiveWordsList.add(30437);
        offensiveWordsList.add(30438);
        offensiveWordsList.add(30439);
        offensiveWordsList.add(30440);
        offensiveWordsList.add(30441);
        offensiveWordsList.add(36130);
        offensiveWordsList.add(36131);
        offensiveWordsList.add(36132);
        offensiveWordsList.add(36133);
        offensiveWordsList.add(36134);

        return offensiveWordsList;
    }


    public static void makeToast(Context activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    public static boolean getMicVisibility(String srcCode) {
        return !srcCode.equals("sq") && !srcCode.equals("be") && !srcCode.equals("bs") && !srcCode.equals("ceb") && !srcCode.equals("co") && !srcCode.equals("eo") &&
                !srcCode.equals("fy") && !srcCode.equals("ht") && !srcCode.equals("ha") && !srcCode.equals("haw") && !srcCode.equals("hmn") && !srcCode.equals("ig") &&
                !srcCode.equals("ga") && !srcCode.equals("kk") && !srcCode.equals("ku") && !srcCode.equals("ky") && !srcCode.equals("la") &&
                !srcCode.equals("lb") && !srcCode.equals("mx") && !srcCode.equals("mg") && !srcCode.equals("mt") && !srcCode.equals("mi") &&
                !srcCode.equals("mn") && !srcCode.equals("nf") && !srcCode.equals("ps") && !srcCode.equals("pa") && !srcCode.equals("gd") && !srcCode.equals("sd") &&
                !srcCode.equals("sn") && !srcCode.equals("so") && !srcCode.equals("tg") && !srcCode.equals("cy") && !srcCode.equals("xh") && !srcCode.equals("yi") &&
                !srcCode.equals("yu");
    }

//    public static boolean isSpeakerVisible(String trLangCodeOnline) {
//        return !trLangCodeOnline.equals("am") && !trLangCodeOnline.equals("hy") && !trLangCodeOnline.equals("az")&& !trLangCodeOnline.equals("eu") && !trLangCodeOnline.equals("be") && !trLangCodeOnline.equals("bg") && !trLangCodeOnline.equals("ceb") &&
//                !trLangCodeOnline.equals("co") && !trLangCodeOnline.equals("ka") && !trLangCodeOnline.equals("gu") && !trLangCodeOnline.equals("ht") && !trLangCodeOnline.equals("he") && !trLangCodeOnline.equals("hmn") && !trLangCodeOnline.equals("jw") &&
//                !trLangCodeOnline.equals("kn") && !trLangCodeOnline.equals("kk") && !trLangCodeOnline.equals("ku") && !trLangCodeOnline.equals("ky") && !trLangCodeOnline.equals("lo") && !trLangCodeOnline.equals("la") && !trLangCodeOnline.equals("lv") &&
//                !trLangCodeOnline.equals("lt") && !trLangCodeOnline.equals("mk") && !trLangCodeOnline.equals("ml") && !trLangCodeOnline.equals("mi") && !trLangCodeOnline.equals("mr") && !trLangCodeOnline.equals("mn") && !trLangCodeOnline.equals("my") &&
//                !trLangCodeOnline.equals("ny") && !trLangCodeOnline.equals("ps") && !trLangCodeOnline.equals("fa") && !trLangCodeOnline.equals("pa") && !trLangCodeOnline.equals("sd") && !trLangCodeOnline.equals("su") && !trLangCodeOnline.equals("tg") &&
//                !trLangCodeOnline.equals("sl") && !trLangCodeOnline.equals("te") && !trLangCodeOnline.equals("xh") && !trLangCodeOnline.equals("yi");
//    }

    public static boolean permissionAlreadyGranted(Activity activity) {

        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    public static List<String> getMicroSoftSupportedLanguages() {

        List<String> langList = new ArrayList<String>();
        langList.add("ar-EG"); // arabic
        langList.add("ca-ES"); // catalan
        langList.add("da-DK"); // danish
        langList.add("de-DE"); // german
        langList.add("en-US"); // en US
        langList.add("es-ES");// spanish Spain
        langList.add("fi-FI");// finish
        langList.add("fr-FR"); // french
        langList.add("it-IT");// italian
        langList.add("ja-JP"); // japanese
        langList.add("ko-KR"); // koriean
        langList.add("nb-NO"); // Norwegian
        langList.add("nl-NL"); // dutch
        langList.add("pl-PL"); // polish
        langList.add("pt-PT"); //Portuguese
        langList.add("ru-RU");// Russian
        langList.add("sv-SE"); // Swedish
        langList.add("zh-CN"); // Chinese
        langList.add("th-TH");// Thai

        return langList;

    }

    public static String getCountryCode(String code) {

        switch (code) {
            case "af":
                return "af-ZA";
            case "am":
                return "am-ET";

            case "ar":
                return "ar-SA";

            case "hy":
                return "hy-AM";

            case "az":
                return "az-AZ";

            case "eu":
                return "eu-ES";

            case "bn":
                return "bn-BD";

            case "bg":
                return "bg-BG";

            case "ca":
                return "ca-ES";

            case "zh":
                return "cmn-Hans-CN";

            case "hr":
                return "hr-HR";

            case "cs":
                return "cs-CZ";

            case "da":
                return "da-DK";

            case "nl":
                return "nl-NL";

            case "en":
                return "en-US";

            case "et":
                return "et-EE";

            case "fi":
                return "fi-FI";

            case "fr":
                return "fr-FR";

            case "tl":
                return "fil-PH";

            case "gl":
                return "gl-ES";

            case "ka":
                return "ka-GE";

            case "de":
                return "de-DE";

            case "el":
                return "el-GR";

            case "gu":
                return "gu-IN";

            case "he":
                return "he-IL";

            case "hi":
                return "hi-IN";

            case "hu":
                return "hu-HU";

            case "is":
                return "is-IS";

            case "id":
                return "id-ID";

            case "it":
                return "it-IT";

            case "ja":
                return "ja-JP";

            case "jw":
                return "jw-ID";

            case "kn":
                return "kn-IN";

            case "km":
                return "km-KH";

            case "ko":
                return "ko-KR";

            case "lo":
                return "lo-LA";

            case "lv":
                return "lv-LV";

            case "lt":
                return "lt-LT";

            case "ms":
                return "ms-MY";

            case "ml":
                return "ml-IN";

            case "mr":
                return "mr-IN";

            case "my":
                return "my-MM";

            case "ne":
                return "ne-NP";

            case "nb":
                return "nb-NO";

            case "fa":
                return "fa-IR";

            case "pl":
                return "pl-PL";

            case "pt":
                return "pt-PT";

            case "ro":
                return "ro-RO";

            case "ru":
                return "ru-RU";

            case "sr":
                return "sr-RS";

            case "sk":
                return "sk-SK";

            case "sl":
                return "sl-SI";

            case "es":
                return "es-ES";

            case "su":
                return "su-ID";

            case "sw":
                return "sw-TZ";

            case "sv":
                return "sv-SE";

            case "ta":
                return "ta-IN";

            case "te":
                return "te-IN";

            case "th":
                return "th-TH";

            case "tr":
                return "tr-TR";

            case "uk":
                return "uk-UA";

            case "ur":
                return "ur-PK";

            case "uz":
                return "uz-UZ";

            case "vi":
                return "vi-VN";

            case "zu":
                return "zu-ZA";

            default:
                return "error";
        }
    }

    public static boolean appInstalledOrNot(Activity activity, String uri) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    public static String getErrorCode(Activity activity, int i) {
        int i2;
        switch (i) {
            case 1:
                i2 = R.string.stt_error_network_timeout;
                break;
            case 2:
                i2 = R.string.stt_error_network_error;
                break;
            case 3:
                i2 = R.string.stt_error_audio;
                break;
            case 4:
                i2 = R.string.stt_error_server;
                break;
            case 5:
                i2 = R.string.stt_error_client;
                break;
            case 6:
                i2 = R.string.stt_error_speech_timeout;
                break;
            case 7:
                i2 = R.string.stt_error_no_match;
                break;
            case 8:
                i2 = R.string.stt_error_recognizer_busy;
                break;
            case 9:
                i2 = R.string.stt_error_insufficient_permissions;
                break;
            default:
                i2 = R.string.stt_error;
                break;
        }
        return activity.getString(i2);
    }


    public static void callGooglePermission(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", "com.google.android.googlequicksearchbox", null);
        intent.setData(uri);
        activity.startActivity(intent);
    }


    public static void callGoogleIntent(Activity activity) {
        String appPackageName = "com.google.android.googlequicksearchbox";
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException exception) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id" + appPackageName)));
        }

    }


}
