package com.ahmadullahpk.alldocumentreader.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.ahmadullahpk.alldocumentreader.xs.constant.MainConstant
import com.ahmadullahpk.alldocumentreader.xs.system.MainControl
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Office {
    Pdf, Ppt, Word, Text, Excel, Hwp
}

fun returnFileExtension(path: String): String {
    return path.substring(path.lastIndexOf(".") + 1)
}

fun Context.checkFileName(rootFile: File, fileName: String?): String {

    var name = fileName + ".pdf"
    var count = 1

    while (File(rootFile, name).exists()) {

        name = fileName + "(" + count + ").pdf"
        count = count + 1
    }

    return name
}

fun Context.currentPages(control: MainControl?, office: Office): Int {

    if (office == Office.Ppt)
        return control?.currentPptPages!!
    else if (office == Office.Word)
        return control?.currentWordTxtPages!!
    else if (office == Office.Text)
        return control?.currentWordTxtPages!!
    else
        return control?.currentWordTxtPages!!
}

fun Context.getCurrentPageBitmap(control: MainControl?, office: Office, pageNo: Int): Bitmap? {

    if (office == Office.Ppt)
        return control?.getCurrentPptPageBitmap(pageNo)
    else if (office == Office.Word)
        return control?.getCurrentWordTxtPageBitmap(pageNo)
    else if (office == Office.Text)
        return control?.getCurrentWordTxtPageBitmap(pageNo)
    else
        return control?.getCurrentWordTxtPageBitmap(pageNo)
}

fun Context.convertToOffice(applicationType: Int): Office {

    if (applicationType == MainConstant.APPLICATION_TYPE_PPT.toInt())
        return Office.Ppt
    else if (applicationType == MainConstant.APPLICATION_TYPE_WP.toInt())
        return Office.Word
    else if (applicationType == MainConstant.APPLICATION_TYPE_TXT.toInt())
        return Office.Text
    else if (applicationType == MainConstant.APPLICATION_TYPE_SS.toInt())
        return Office.Excel
    else
        return Office.Word
}

suspend fun Context.convertWordToPdf(
    control: MainControl?,
    progressUpdate: (Pair<String, Int>) -> Unit,
    isPrint: Boolean,
    office: Office,
    newFileName: String? = null
): File {

    return coroutineScope {

        var fileName: String? = null
        var dirFile: File? = null

        if (isPrint) {

            fileName = "Pdf " + System.currentTimeMillis() + ".pdf"

            dirFile = File(getFilesDir().absolutePath + "/" + "SharePDFScanner")

            if (!dirFile.exists())
                dirFile.mkdirs()

        } else {

            dirFile = File(getFilesDir().absolutePath + "/" + "PDF Scanner")

            if (!dirFile.exists())
                dirFile.mkdirs()

            fileName = checkFileName(dirFile, newFileName)
        }

        val file = File(dirFile, fileName)

        file.apply {
            if (exists()) {
                delete()
                createNewFile()
            } else {
                createNewFile()
            }
        }

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file.absoluteFile))
        val documentRect: Rectangle = document.pageSize
        document.open()
        var count = 0
        for (pageNo in 1 until currentPages(control, office) + 1) {
            ensureActive()

            getCurrentPageBitmap(control, office, pageNo)?.let { bitmap ->

                ensureActive()

                document.newPage()

                val streamOfBitmap = ByteArrayOutputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
                }

                val image: Image = Image.getInstance(streamOfBitmap.toByteArray())
                val pageWidth = document.pageSize.width
                val pageHeight = document.pageSize.height
                image.scaleToFit(pageWidth, pageHeight)
                image.setAbsolutePosition(
                    (documentRect.width - image.scaledWidth) / 2,
                    (documentRect.height - image.scaledHeight) / 2
                )
                document.add(image)
            }

            count += 1

            val percentage =
                ((count.toFloat() / currentPages(control, office).toFloat()) * 100).toInt()

            val pairObject = Pair("${count}/${currentPages(control, office)}", percentage)

            withContext(Dispatchers.Main) {
                progressUpdate.invoke(pairObject)
            }
        }
        document.close()

        return@coroutineScope file
    }
}