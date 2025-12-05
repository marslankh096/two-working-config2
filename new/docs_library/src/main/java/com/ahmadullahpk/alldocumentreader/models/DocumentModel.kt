package com.ahmadullahpk.alldocumentreader.models

import android.net.Uri
import com.ahmadullahpk.alldocumentreader.utils.getFileSize
import com.ahmadullahpk.alldocumentreader.utils.returnFileExtension
import java.text.SimpleDateFormat
import java.util.*

data class DocumentModel(

    var name: String? = null,
    var path: String? = null,
    var date: Long? = null,
    var size: Long? = null,
    var docType :Int ? =null,
    var uri : Uri ? = null
)
{

    fun getDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        return sdf.format(Date(date!!))

//         sdf.format(Date(date?.times(1000L) ?: 0))
    }

    fun getSize(): String {
       return getFileSize(size!!)
    }

    fun getNameWithExtension(): String{
       return name +"."+ returnFileExtension(path!!)
    }
}
