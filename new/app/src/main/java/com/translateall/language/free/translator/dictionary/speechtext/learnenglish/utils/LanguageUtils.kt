package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils

import android.content.Context
import android.provider.Settings
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import org.intellij.lang.annotations.Language
import java.security.NoSuchAlgorithmException


var mOcrLangList: java.util.ArrayList<LanguageModel>? = null
fun langsDataOCr(): ArrayList<LanguageModel> {
    if (mOcrLangList != null && mOcrLangList!!.size > 0) {
        return mOcrLangList!!
    } else {
        val langList = ArrayList<LanguageModel>()

        langList.add(getLanguageObject("af", "Afrikaans", "Afrikaans", "ZA"))
        langList.add(getLanguageObject("sq", "Albanian", "shqiptar", "AL"))
        langList.add(getLanguageObject("az", "Azerbaijani", "Azərbaycan", "AZ"))
        langList.add(getLanguageObject("eu", "Basque", "Euskal", "ES"))
        langList.add(getLanguageObject("bs", "Bosnian", "bosanski", "BA"))
        langList.add(getLanguageObject("ca", "Catalan", "Català", "ES"))
        langList.add(getLanguageObject("ceb", "Cebuano", "Cebuano", "PH"))
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
//        langList.add(getLanguageObject("tl", "Filipino", "Pilipino", "PH"))

        langList.add(getLanguageObject("gl", "Galician", "Galego", "ES"))
        langList.add(getLanguageObject("de", "German", "Deutsch", "DE"))

        langList.add(getLanguageObject("ht", "Haitian Creole", "Haitian Creole Version", "HT"))
        langList.add(getLanguageObject("ha", "Hausa", "Hausa", "NG"))
        langList.add(getLanguageObject("haw", "Hawaiian", "ʻ .lelo Hawaiʻi", "AAH")) // hawai flag
        langList.add(getLanguageObject("he", "Hebrew", "עברית", "IL"))
        langList.add(getLanguageObject("hmn", "Hmong", "Hmong", "CN"))
        langList.add(getLanguageObject("hu", "Hungarian", "Magyar", "HU"))

        langList.add(getLanguageObject("is", "Icelandic", "Íslensku", "IS"))
        langList.add(getLanguageObject("ig", "Igbo", "Ndi Igbo", "NG"))
        langList.add(getLanguageObject("id", "Indonesian", "Indonesia", "ID"))
        langList.add(getLanguageObject("ga", "Irish", "Gaeilge", "IE"))
        langList.add(getLanguageObject("it", "Italian", "Italiano", "IT"))

        langList.add(getLanguageObject("jw", "Javanese", "Basa jawa", "ID"))

        langList.add(getLanguageObject("ku", "Kurdish", "Kurdî", "IQ"))


        langList.add(getLanguageObject("la", "Latin", "Latine", "IT"))
        langList.add(getLanguageObject("lv", "Latvian", "Latviešu valoda", "LV"))
        langList.add(getLanguageObject("lt", "Lithuanian", "Lietuvių", "LT"))
        langList.add(getLanguageObject("lb", "Luxembourgish", "Lëtzebuergesch", "LU"))

        langList.add(getLanguageObject("mg", "Malagasy", "Malagasy", "MG"))
        langList.add(getLanguageObject("ms", "Malay", "Bahasa Melayu", "MY"))
        langList.add(getLanguageObject("mt", "Maltese", "Il-Malti", "MT"))
        langList.add(getLanguageObject("mi", "Maori", "Maori", "NZ"))

//        langList.add(getLanguageObject("nb", "Norwegian", "Norsk", "NO"))
        langList.add(getLanguageObject("pl", "Polish", "Polski", "PL"))
        langList.add(getLanguageObject("pt", "Portuguese", "Português", "PT"))

        langList.add(getLanguageObject("ro", "Romanian", "Română", "RO"))
        langList.add(getLanguageObject("gd", "Scots Gaelic", "Gàidhlig na h-Alba", "GB"))
        langList.add(getLanguageObject("sn", "Shona", "Shona", "ZW"))
        langList.add(getLanguageObject("sk", "Slovak", "Slovenský", "SK"))
        langList.add(getLanguageObject("sl", "Slovenian", "Slovenščina", "SL"))
        langList.add(getLanguageObject("so", "Somali", "Soomaali", "SO"))
        langList.add(getLanguageObject("es", "Spanish", "Español", "ES"))
        langList.add(getLanguageObject("su", "Sundanese", "Urang Sunda", "ID"))
        langList.add(getLanguageObject("sw", "Swahili", "Kiswahili", "CD"))

        langList.add(getLanguageObject("tr", "Turkish", "Türk", "TR"))

        langList.add(getLanguageObject("uz", "Uzbek", "O'zbek", "UZ"))

        langList.add(getLanguageObject("vi", "Vietnamese", "Tiếng Việt", "VN"))

        langList.add(getLanguageObject("cy", "Welsh", "Cymraeg", "GB"))

        langList.add(getLanguageObject("yo", "Yoruba", "Yoruba", "NG"))

        langList.add(getLanguageObject("zu", "Zulu", "IsiZulu", "ZA"))

        mOcrLangList = langList
        return mOcrLangList!!
    }



}