package com.ahmadullahpk.alldocumentreader.utils

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.FileProvider
import androidx.viewbinding.ViewBinding
import com.ahmadullahpk.alldocumentreader.R
import com.ahmadullahpk.alldocumentreader.xs.system.MainControl
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun Context.shareFile(selectList: String, myContext: Context) {

    val share = Intent(Intent.ACTION_SEND_MULTIPLE)
    share.putExtra(Intent.EXTRA_SUBJECT, "")
    //  share.type = "image/*"
    val files: ArrayList<Uri> = ArrayList()
    var uri: Uri? = null
    for (i in 0 until 1) {

        uri = FileProvider.getUriForFile(
            myContext,
            myContext.packageName,
            File(selectList)
        )
        files.add(uri) // uri of my bitmap image1
    }

    share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
    share.type = "*/*"

    myContext.startActivity(Intent.createChooser(share, "Share File"))
}

fun showSoftKeyboard(view: View, context: Context) {
    if (view.requestFocus()) {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

/*fun initCustomDialog(context: Context, layout: Int, isCancelable: Boolean = true): Dialog? {
    val dialog = Dialog(context, R.style.customDialogSize)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(isCancelable)
    dialog.setContentView(layout)
    return dialog
}*/

/*fun Context.initCustomDialog( layout: ViewBinding, isCancelable: Boolean = true): Dialog? {
    val dialog = Dialog(this, com.app.localization.R.style.customDialogSize)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(isCancelable)
    dialog.setContentView(layout.root)
    return dialog
}*/

fun Context.editTextFocusClicked(edt: EditText) {

    edt.isFocusable = true
    edt.isFocusableInTouchMode = true
    edt.isClickable = true
    edt.requestFocus()

    val pos: Int = edt.getText().length

    if (pos > 0) {
        edt.setSelection(pos)
        edt.selectAll()
        edt.setSelectAllOnFocus(true)
    }
}

fun Context.showSoftKeyboard(view: View) {
    if (view.requestFocus()) {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.initBottomSheetDialog(
    viewBinding: ViewBinding,
    isCancelable: Boolean = true
): Dialog? {
    val dialog = BottomSheetDialog(this, R.style.customBottomSheetDialogSize)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(isCancelable)
    dialog.setCanceledOnTouchOutside(isCancelable)
    dialog.setContentView(viewBinding.root)
    dialog.dismissWithAnimation = true
    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
//    dialog.window?.navigationBarColor = Color.parseColor("#f2f2f2")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        val window = dialog.window

        if (window != null) {
            val metrics = DisplayMetrics()
            window.getWindowManager().getDefaultDisplay().getMetrics(metrics)
            val dimDrawable = GradientDrawable()
            // ...customize your dim effect here
            val navigationBarDrawable = GradientDrawable()
            navigationBarDrawable.setShape(GradientDrawable.RECTANGLE)
            navigationBarDrawable.setColor(Color.WHITE)
            val layers: Array<Drawable> = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)
            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(1, metrics.heightPixels - 8)
            window.setBackgroundDrawable(windowBackground)
        }
    }

    return dialog
}

fun getFileSize(size: Long): String {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()

        val otherSymbols = DecimalFormatSymbols(Locale.ENGLISH)
        otherSymbols.setDecimalSeparator('.')

        return DecimalFormat("#,##0.#", otherSymbols).format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        )
            .toString() + " " + units[digitGroups]

    } else {

        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(
            "%.1f",
            (size / Math.pow(1024.0, digitGroups.toDouble()))
        ) + " " + units[digitGroups]
    }
}
fun Context.hideKeyboard(view: EditText) {
    val imm: InputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
}

fun Context.showSnackbar(text: String, viewBinding: View) {

    Snackbar.make(
        viewBinding,
        text,
        Snackbar.LENGTH_SHORT
    )
        .setAction("Close", object : View.OnClickListener {
            override fun onClick(view: View?) {
            }
        })
        .setActionTextColor(resources.getColor(R.color.colorPrimary))
        .show()
}


fun String.isNameValid(): Boolean {
    // invalidResourceCharacters
    arrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|').forEach {
        if (this.contains(it, true)) {
            return false
        }
    }

    //. and .. have special meaning on all platforms
    if (this == "." || this == "..") //$NON-NLS-1$ //$NON-NLS-2$
        return false
    //empty names are not valid
    val length = this.length
    if (length == 0) return false
    val lastChar = this[length - 1]
    // filenames ending in dot are not valid
//        if (lastChar == '.') return false
    // file names ending with whitespace are truncated (bug 118997)
    if (Character.isWhitespace(lastChar)) return false
    val dot = this.indexOf('.')
    //on windows, filename suffixes are not relevant to name validity
    val basename = if (dot == -1) this else this.substring(0, dot)
    val invalidResourceBasenames = arrayOf(
        "aux",
        "com1",
        "com2",
        "com3",
        "com4",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        "com5",
        "com6",
        "com7",
        "com8",
        "com9",
        "con",
        "lpt1",
        "lpt2",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        "lpt3",
        "lpt4",
        "lpt5",
        "lpt6",
        "lpt7",
        "lpt8",
        "lpt9",
        "nul",
        "prn"
    ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
    Arrays.sort(invalidResourceBasenames)

    //CLOCK$ may be used if an extension is provided
    val invalidResourceFullnames: Array<String> = arrayOf("clock$") //$NON-NLS-1$
    return if (Arrays.binarySearch(
            invalidResourceBasenames,
            basename.toLowerCase()
        ) >= 0
    ) false else Arrays.binarySearch(
        invalidResourceFullnames,
        this.toLowerCase()
    ) < 0
    return true

}

