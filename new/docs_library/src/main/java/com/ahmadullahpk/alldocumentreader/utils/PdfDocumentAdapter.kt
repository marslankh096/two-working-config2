package com.ahmadullahpk.alldocumentreader.utils

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PdfDocumentAdapter(private val pathName: String) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        bundle: Bundle
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        } else {

            if (pathName.isNullOrEmpty() && pathName.isNullOrBlank()) {
                callback.onLayoutCancelled()
                return

            } else {

                try {
                    val builder = PrintDocumentInfo.Builder(File(pathName).nameWithoutExtension)
                    builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()

                    callback.onLayoutFinished(builder.build(), newAttributes == oldAttributes)

                } catch (e :Exception) {

                    callback.onLayoutCancelled()
                    return
                }
            }
        }
    }

    override fun onWrite(
        pageRanges: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        try {
            // copy file from the input stream to the output stream
            FileInputStream(File(pathName)).use { inStream ->
                FileOutputStream(destination.fileDescriptor).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }

            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
            } else {
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }

        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}

fun Context.printPDF(path: String) {

    val printManager: PrintManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
    try {
        val printAdapter = PdfDocumentAdapter(path)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    } catch (e: Exception) {
    }
}