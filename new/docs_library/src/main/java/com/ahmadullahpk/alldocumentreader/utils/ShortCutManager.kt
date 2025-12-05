package com.ahmadullahpk.alldocumentreader.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.pm.ShortcutManagerCompat.FLAG_MATCH_PINNED
import androidx.core.graphics.drawable.IconCompat
import com.ahmadullahpk.alldocumentreader.R
import com.ahmadullahpk.alldocumentreader.gone
import com.ahmadullahpk.alldocumentreader.xs.constant.MainConstant
import com.bumptech.glide.Glide
import java.util.*

fun Context.createDocumentIcon(docType: Int, name: String, uri: Uri, path : String, isOfficeDoc: Boolean = true) {

    if(checkIfShortCutExist(path)){
        Toast.makeText(this,getString(R.string.toast_shortcut_exist), Toast.LENGTH_SHORT).show()
    }
    else{
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {

//            var launchIntent = Intent(this, SplashActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                data = uri
//                action = Intent.ACTION_VIEW
//                putExtra("isIconDocument",true)
//                putExtra("isOfficeDoc",isOfficeDoc)
//                putExtra("fileName",name)
//            }

            val shortcutInfo: ShortcutInfoCompat = ShortcutInfoCompat.Builder(this, path)

                .setIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com")
                    )
                )

                .setShortLabel(name)
                .setLongLabel(name)
                .setIcon(IconCompat.createWithResource(this, getDocumentIcon(docType)))
                .build()
            ShortcutManagerCompat.requestPinShortcut(this, shortcutInfo, null)

        } else {
            Toast.makeText(this,getString(R.string.toast_shortcut_launcher_not_support), Toast.LENGTH_SHORT).show()
        }
    }
}

@SuppressLint("WrongConstant")
fun Context.checkIfShortCutExist(shortCutId : String) : Boolean{

    var list = ShortcutManagerCompat.getShortcuts(this, ShortcutManagerCompat.FLAG_MATCH_PINNED)

    var status = false

    for (shortCut in list){
        if(shortCut.id == shortCutId){
            status = true
            break
        }
    }
    return status
}

fun getDocumentIcon(docType: Int): Int {

    var icon = 0

    when (docType) {
        MainConstant.APPLICATION_TYPE_WP.toInt() -> {
            icon = R.drawable.ic_doc_icon_launcher
        }
        MainConstant.APPLICATION_TYPE_TXT.toInt() -> {
            icon = R.drawable.ic_txt_icon_launcher
        }
        MainConstant.APPLICATION_TYPE_PPT.toInt() -> {
            icon = R.drawable.ic_ppt_icon_launcher
        }
        MainConstant.APPLICATION_TYPE_SS.toInt() -> {
            icon = R.drawable.ic_xls_icon_launcher
        }
    }

   return  icon
}
