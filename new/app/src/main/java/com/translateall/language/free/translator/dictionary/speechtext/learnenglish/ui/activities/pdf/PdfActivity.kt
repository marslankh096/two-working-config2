package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.pdf

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityPdfBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.dialogs.GoToDialog
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchInputScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.PdfRendererView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.FileUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PdfActivity : BaseActivity() {
    private lateinit var binding: ActivityPdfBinding
    private lateinit var jumpToDialog: GoToDialog

    // Use lifecycle-aware coroutine scope
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val FILE_URL = "pdf_file_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Android 15+ edge-to-edge setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                view.updatePadding(
                    left = systemBarInsets.left,
                    top = systemBarInsets.top,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )

                WindowInsetsCompat.CONSUMED
            }
        }

        setListeners()
        init()
    }

    private fun init() {
        // Show progress immediately on UI thread
        binding.progressLayout.visible()

        // Move heavy work to background thread
        activityScope.launch {
            try {
                // Setup dialog on main thread
                withContext(Dispatchers.Main) {
                    setJumpDialog()
                }

                // Small delay to let UI render
                delay(300)

                if (intent.extras?.containsKey(FILE_URL) == true) {
                    val fileUrl = intent.extras?.getString(FILE_URL)
                    // Process file on IO thread
                    initPdfViewerWithPath(fileUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.something_went_wrong), 0)
                    backPressed()
                }
            }
        }
    }

    private fun setListeners() {
        binding.progressLayout.setOnClickListener {}

        binding.pdfView.statusListener = object : PdfRendererView.StatusCallBack {
            override fun onPdfLoadStart() {
                // Progress already shown
            }

            override fun onPdfLoadProgress(
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long?
            ) {
                // Download progress
            }

            override fun onPdfLoadSuccess(absolutePath: String) {
                runOnUiThread {
                    binding.progressLayout.gone()
                }
            }

            override fun onError(error: Throwable) {
                runOnUiThread {
                    showToast(error.toString(), 0)
                    backPressed()
                }
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                // Page changed
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })

        binding.backIV.setOnClickListener {
            if (isDoubleClick())
                backPressed()
        }

        binding.jumpIV.setOnClickListener {
            if (isDoubleClick()) {
                jumpToDialog.let {
                    if (!it.isVisible && !it.isAdded)
                        it.show(supportFragmentManager, "jump_page_dialog")
                }
            }
        }

        binding.getTextBtn.setOnClickListener {
            if (isDoubleClick()) {
                extractTextFromCurrentPage()
            }
        }
    }

    private fun setJumpDialog() {
        jumpToDialog = GoToDialog.newInstance()
        jumpToDialog.initListener(object : GoToDialog.GoToPageListener {
            override fun pageNumber(num: Int) {
                binding.pdfView.jumpToPage(num - 1)
            }
        })
    }

    private suspend fun initPdfViewerWithPath(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            withContext(Dispatchers.Main) {
                binding.progressLayout.gone()
            }
            return
        }

        try {
            // Process file on IO thread
            val file = withContext(Dispatchers.IO) {
                if (filePath!!.startsWith("content://")) {
                    FileUtils.uriToFile(applicationContext, Uri.parse(filePath))
                } else {
                    File(filePath)
                }
            }

            // Initialize PDF on main thread (if required by library)
            withContext(Dispatchers.Main) {
                binding.pdfView.initWithFile(file)
                jumpToDialog.setPagesLimit(binding.pdfView.totalPageCount)
                binding.progressLayout.gone()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                showToast(getString(R.string.something_went_wrong), 0)
                backPressed()
            }
        }
    }

    private fun extractTextFromCurrentPage() {
        activityScope.launch {
            try {
                // Show progress on main thread
                withContext(Dispatchers.Main) {
                    binding.progressLayout.visible()
                }

                // Get bitmap on main thread (if required)
                val bitmap = withContext(Dispatchers.Main) {
                    binding.pdfView.getBitmapCurrentPage()
                }

                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        showToast("Unable to get page bitmap", 0)
                        binding.progressLayout.gone()
                    }
                    return@launch
                }

                // Process text extraction on IO thread
                withContext(Dispatchers.IO) {
                    extractText(
                        bitmap = bitmap,
                        resultSuccess = { result ->
                            // Handle success on main thread
                            activityScope.launch(Dispatchers.Main) {
                                result?.let { resultText ->
                                    launchInputScreen(resultText, "")
                                    backPressed()
                                }
                            }
                        },
                        onErrorDetected = { error ->
                            // Handle error on main thread
                            activityScope.launch(Dispatchers.Main) {
                                showToast(error, 0)
                                binding.progressLayout.gone()
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error extracting text: ${e.localizedMessage}", 0)
                    binding.progressLayout.gone()
                }
            }
        }
    }

    private suspend fun extractText(
        bitmap: Bitmap,
        resultSuccess: (String?) -> Unit,
        onErrorDetected: (String) -> Unit,
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        kotlin.runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    var originalStr = result.text
                    originalStr = originalStr.replace("\n", " ")
                    resultSuccess.invoke(originalStr)
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.localizedMessage?.let { onErrorDetected.invoke(it) }
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }
        }.onFailure { exception ->
            exception.localizedMessage?.let { onErrorDetected.invoke(it) }
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines
        activityScope.cancel()
    }

    private fun backPressed() {
        // Clean up resources
        binding.pdfView.closePdfRender()
        finish()
    }
}
/*
class PdfActivity : BaseActivity() {
    private lateinit var binding: ActivityPdfBinding
    private lateinit var jumpToDialog: GoToDialog

    companion object {
        const val FILE_URL = "pdf_file_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Android 15+ edge-to-edge setup (only once, here)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ViewCompat.setOnApplyWindowInsetsListener(requireNotNull(binding).root) { view, windowInsets ->
                val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                // Apply padding to your view using KTX extension
                view.updatePadding(
                    left = systemBarInsets.left,
                    top = systemBarInsets.top,
                    right = systemBarInsets.right,
                    bottom = imeInsets.bottom.coerceAtLeast(systemBarInsets.bottom)
                )

                // Consume the insets if you've handled them
                WindowInsetsCompat.CONSUMED
            }
        }

        init()
        setListeners()
    }

    private fun init() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.progressLayout.visible()
            setJumpDialog()
            delay(500)
            if (intent.extras!!.containsKey(FILE_URL)) {
                val fileUrl = intent.extras!!.getString(FILE_URL)
                initPdfViewerWithPath(fileUrl)
            }
        }

    }

    private fun setListeners() {
        binding.progressLayout.setOnClickListener {}

        binding.pdfView.statusListener = object : PdfRendererView.StatusCallBack {
            override fun onPdfLoadStart() {
//                runOnUiThread {
//                    true.showProgressBar()
//                }
            }

            override fun onPdfLoadProgress(
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long?
            ) {
                //Download is in progress
            }

            override fun onPdfLoadSuccess(absolutePath: String) {
                runOnUiThread {
                    binding.progressLayout.gone()
//                    false.showProgressBar()
                }
            }

            override fun onError(error: Throwable) {
                runOnUiThread {
//                    false.showProgressBar()
                    showToast(error.toString(), 0)
                    backPressed()
                }
            }

            override fun onPageChanged(currentPage: Int, totalPage: Int) {
                //Page change. Not require
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })

        binding.backIV.setOnClickListener {
            if (isDoubleClick())
                backPressed()
        }

        binding.jumpIV.setOnClickListener {
            if (isDoubleClick()) {
                jumpToDialog?.let {
                    if (!it.isVisible && !it.isAdded)
                        it.show(supportFragmentManager, "jump_page_dialog")
                }
            }
        }

        binding.getTextBtn.setOnClickListener {
            if (isDoubleClick()) {
                binding.progressLayout.visible()
                binding.pdfView.getBitmapCurrentPage()?.let {
                    extractText(it, resultSuccess = { result ->
                        result?.let { resultText ->
                            launchInputScreen(resultText, "")
                            backPressed()
                        }
                    }, onErrorDetected = { error ->
                        showToast(error, 0)
                        binding.progressLayout.gone()
                    })
                }
            }

        }

    }

    private fun setJumpDialog() {
        jumpToDialog = GoToDialog.newInstance()
        jumpToDialog.initListener(object : GoToDialog.GoToPageListener {

            override fun pageNumber(num: Int) {
//                showToast(num.toString(), 0)
                binding.pdfView.jumpToPage(num - 1)
            }

        })
    }

    private fun initPdfViewerWithPath(filePath: String?) {
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        try {
            val file = if (filePath!!.startsWith("content://")) {
                FileUtils.uriToFile(applicationContext, Uri.parse(filePath))
            } else {
                File(filePath)
            }
            binding.pdfView.initWithFile(file)
            jumpToDialog.setPagesLimit(binding.pdfView.totalPageCount)

            binding.progressLayout.gone()
        } catch (e: Exception) {
            showToast(getString(R.string.something_went_wrong), 0)
            backPressed()
        }
    }

    private fun extractText(
        bitmap: Bitmap,
        resultSuccess: (String?) -> Unit,
        onErrorDetected: (String) -> Unit,
    ) {
        kotlin.runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { mSuccess ->
                    mSuccess.let { result ->
                        var originalStr = result.text
                        originalStr = originalStr.replace("\n", " ")
                        resultSuccess.invoke(originalStr)
                    }
                }
                .addOnFailureListener {
                    it.localizedMessage?.let { e1 -> onErrorDetected.invoke(e1) }
                }
        }.onFailure {
            it.localizedMessage?.let { e2 -> onErrorDetected.invoke(e2) }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun backPressed() {
//        binding.pdfView.statusListener = null
        binding.pdfView.closePdfRender()
        finish()
    }

}*/
