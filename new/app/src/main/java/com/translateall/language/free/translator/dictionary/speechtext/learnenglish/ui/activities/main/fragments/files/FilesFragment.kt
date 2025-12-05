package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.main.fragments.files

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hm.admanagerx.isAppOpenAdShow
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.FragmentFilesBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.docs.DocsActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.pdf.PdfActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.pdf.PdfActivity.Companion.FILE_URL
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.DOCS_FILE_TYPE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.PPT_FILE_TYPE
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick

class FilesFragment : Fragment() {
    private var _binding: FragmentFilesBinding? = null
    private val binding: FragmentFilesBinding get() = _binding!!
    private var mContext: Context? = null

    private val launcher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            mContext?.let { ctx ->
                uri?.let {
                    Log.e("none", "file uri: $it")
                    val mimeType = ctx.contentResolver.getType(it)
                    val fileName = getFileNameFromUri(it)
                    if (mimeType != null) {
                        when (mimeType) {
                            "application/pdf" -> {
//                                IkmSdkController.setEnableShowResumeAds(true)
                                launchPdfFromUri(it.toString())
                            }

                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
//                                IkmSdkController.setEnableShowResumeAds(true)
                                val mIntent = Intent(ctx, DocsActivity::class.java)
                                mIntent.putExtra("name", fileName)
                                mIntent.putExtra("fileType", DOCS_FILE_TYPE)
                                mIntent.putExtra("path", it.toString())
                                startActivity(mIntent)
                            }

                            "application/vnd.ms-powerpoint",
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> {
//                                IkmSdkController.setEnableShowResumeAds(true)
                                val mIntent = Intent(ctx, DocsActivity::class.java)
                                mIntent.putExtra("name", fileName)
                                mIntent.putExtra("fileType", PPT_FILE_TYPE)
                                mIntent.putExtra("path", it.toString())
                                startActivity(mIntent)
                            }

                            "text/plain" -> {
//                                IkmSdkController.setEnableShowResumeAds(true)
                                val mIntent = Intent(ctx, DocsActivity::class.java)
                                mIntent.putExtra("name", fileName)
                                mIntent.putExtra("fileType", DOCS_FILE_TYPE)
                                mIntent.putExtra("path", it.toString())
                                startActivity(mIntent)
                            }

                            else -> {

                                // Handle other file types or unsupported type
                            }
                        }
                    } else {
                        // Handle case where MIME type couldn't be determined
                    }
                }
            }
        }

    @SuppressLint("Range", "Recycle")
    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        mContext?.let {
            if (uri.scheme == "content") {
                val cursor = it.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } catch (e: Exception) {
                    return "Doc_name"
                } finally {
                    cursor?.close()
                }
            }
            if (name == null) {
                name = uri.path ?: "Doc_name"
                val cut = name?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    name = name?.substring(cut + 1)
                }
            }
        }
        return name
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null)
            _binding = FragmentFilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
    }

    private fun init() {}

    private fun setListeners() {
        binding.browseTV.setOnClickListener {
            if (isDoubleClick()) {
                launchFilePicker()
            }
        }
    }

    private fun launchFilePicker() {
        mContext?.let { ctx->
            ctx.isAppOpenAdShow(false)
        }
        val mimeTypes = arrayOf(
            "application/pdf",
            "application/msword",  // DOC
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", //DOCX
            "text/plain",
            "application/vnd.ms-powerpoint", // PPT
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" //PPTX
        )

        launcher.launch(mimeTypes)
    }

    private fun launchPdfFromUri(uri: String) {
        mContext?.let {
            startActivity(Intent(it, PdfActivity::class.java).putExtra(FILE_URL, uri))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        mContext = null
    }

}