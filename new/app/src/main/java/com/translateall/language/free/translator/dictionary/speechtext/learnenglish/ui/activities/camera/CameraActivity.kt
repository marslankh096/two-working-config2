package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.camera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import com.bumptech.glide.Glide
import com.hm.admanagerx.AdConfigManager
import com.hm.admanagerx.AdsManagerX
import com.hm.admanagerx.isAppOpenAdShow
import com.theartofdev.edmodo.cropper.CropImageView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityCameraBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.BaseActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.cameraTranslate.CameraTranslateActivity
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.AppUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.isOnline
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.gone
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isDoubleClick
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.isPremium
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.launchLanguageScreen
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.hide
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.pdf.util.show
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.showToast
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraActivity : BaseActivity(), CropImageView.OnCropImageCompleteListener {
    companion object {
        private const val TAG = "Camera Translator"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"


        fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }

    val EXTENSION_WHITELIST = arrayOf("JPG")
    private lateinit var binding: ActivityCameraBinding

    // camera helpers
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private var isToggleFlash = false

    // languages
    private var srcLangCode: String = ""
    private var srcLangName: String = ""
    private var targetLangCode: String = ""
    private var targetLangName: String = ""
    private var srcLangPosition: Int = Constants.DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = Constants.DEFAULT_TAR_LANG_POSITION

    private var isPhotoCaptured = false
    private var isGallery = false
    private var isResultAvailable = false
    private var translationUtils: TranslationUtils? = null

    //var bannerAd: AdView? = null

    private val viewModel: OcrViewModel by lazy {
        ViewModelProviders.of(this).get(OcrViewModel::class.java)
    }
    private lateinit var outputDirectory: File
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
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

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        getLanguagesData()
        setUpCamera()
        setClickListeners()
        loadShowBannerAd()
        AdsManagerX.loadInterAd(this, AdConfigManager.INTER_AD_CAMERA_TRANSLATION)
    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(Constants.SOURCE_LANG_CODE_OCR)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
        }
        srcLangName = viewModel.getLangData(Constants.SOURCE_LANG_NAME_OCR)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
        }

        srcLangPosition = viewModel.getLangPosition(Constants.SOURCE_LANG_POSITION_OCR)

        if (srcLangPosition == -1) {
            srcLangPosition = Constants.DEFAULT_SRC_LANG_POSITION_OCR
            viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
        }

        targetLangCode = viewModel.getLangData(Constants.TARGET_LANG_CODE)
        if (targetLangCode == "") {
            targetLangCode = "fr"
            viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
        }

        targetLangName = viewModel.getLangData(Constants.TARGET_LANG_NAME)
        if (targetLangName == "") {
            targetLangName = "French"
            viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
        }

        targetLangPosition = viewModel.getLangPosition(Constants.TARGET_LANG_POSITION)
        if (targetLangPosition == -1) {
            targetLangPosition = Constants.DEFAULT_TAR_LANG_POSITION
            viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
        }

        setLanguagesName()
    }

    private fun setLanguagesName() {
        binding.tvLangLeft.setText(srcLangName)
        binding.tvLangRight.setText(targetLangName)
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        try {
            val metrics =
                WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this).bounds
            val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())

            if (binding.cameraView.display != null) {
                val rotation = binding.cameraView.display.rotation
                val cameraProvider = cameraProvider
                    ?: throw IllegalStateException("Camera initialization failed.")
                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                preview = Preview.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build()
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                preview?.setSurfaceProvider(binding.cameraView.surfaceProvider)

            } else {
                lifecycleScope.launch {
                    delay(50)
                    bindCameraUseCases()
                }
            }

        } catch (e: java.lang.Exception) {
            Log.e(TAG, "bindCameraUseCases: ${e.localizedMessage}")
        }

    }

    private fun setClickListeners() {
        binding.ivBackOcr.setOnClickListener {
            backPressed()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        binding.layoutRetake.setOnClickListener {
            hideResultBox()
            resetCamera()
        }

        binding.tvLangLeft.setOnClickListener {
            openLanguageSheet(Constants.LANGUAGE_TYPE_SOURCE)
        }

        binding.tvLangRight.setOnClickListener {
            openLanguageSheet(Constants.LANGUAGE_TYPE_TARGET)
        }

        binding.layoutFlashToggle.setOnClickListener {
            onClickIcFlash()
        }

        binding.cameraImport.setOnClickListener {
            if (isDoubleClick()) {
                isAppOpenAdShow(false)
                val i = Intent()
                i.type = "image/*"
                i.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(i, "Select Picture"),
                    Constants.REQUEST_CODE_GALLERY
                )
            }

        }

        binding.layoutRotate.setOnClickListener {
            binding.cropImage.rotateImage(90)
        }

        binding.layoutOk.setOnClickListener {
            if (isDoubleClick())
                getResult()
        }

        binding.ivClearOcr.setOnClickListener {
            hideResultBox()
        }

        binding.resultLayout.setOnClickListener {
            hideResultBox()
        }
    }

    private fun backPressed() {
        if (isResultAvailable) {
            hideResultBox()

        } else if (isPhotoCaptured) {
            resetCamera()

        } else
            finish()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        binding.progressLayout.visible()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    binding.progressLayout.gone()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    binding.progressLayout.gone()
                    processPhoto(savedUri)
                    // showCaptureInterAd(savedUri)
                }
            })

    }

    private fun processPhoto(savedUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val mUri = savedUri
            withContext(Dispatchers.Main) {

                isToggleFlash = false
                toggleFlash(isToggleFlash)
                binding.cropLayout.visible()
                binding.imageBlack.visible()
                binding.cropImage.setImageUriAsync(mUri)
                binding.progressLayout.gone()
                binding.groupActionCapture.gone()
                binding.groupActionCaptured.visible()
                binding.cameraView.gone()
                //showCropBanner()
                isPhotoCaptured = true
                sendBroadCast(mUri)

            }
        }
    }

    fun sendBroadCast(savedUri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sendBroadcast(
                Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
            )
        }
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(savedUri.toFile().extension)
        MediaScannerConnection.scanFile(
            this@CameraActivity,
            arrayOf(savedUri.toFile().absolutePath),
            arrayOf(mimeType)
        ) { _, uri -> }
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun onClickIcFlash() {
        if (isToggleFlash) {
            isToggleFlash = false
            toggleFlash(isToggleFlash)
        } else {
            isToggleFlash = true
            toggleFlash(isToggleFlash)

        }
    }

    private fun toggleFlash(value: Boolean) {
        if (value) {
            flashLightOn()
        } else {
            flashLightOff()
        }
    }

    private fun flashLightOn() {
        binding.flashToggle.setImageResource(R.drawable.ic_flashlight_on)
        binding.tvLabelFlash.text = "Flash On"
        try {
            val cameraControl = camera?.cameraControl
            cameraControl?.enableTorch(true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun flashLightOff() {
        binding.flashToggle.setImageResource(R.drawable.ic_flashlight_off)
        binding.tvLabelFlash.text = "Flash Off"
        try {
            val cameraControl = camera?.cameraControl
            cameraControl?.enableTorch(false)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun openLanguageSheet(type: String) {
        launchLanguageScreen(type)
    }

    private fun hideResultBox() {
        isResultAvailable = false
        binding.resultLayout.gone()
        binding.ivMoreOcr.gone()
        binding.tvOcrOutput.gone()
        binding.tvOcrInput.text = ""
        binding.tvOcrOutput.text = ""
    }

    private fun resetCamera() {

        isGallery = false
        isPhotoCaptured = false
        //showCameraBanner()
        binding.cameraView.visible()
        binding.cropLayout.gone()
        binding.imageBlack.gone()
        deleteAllCaptured()
        binding.groupActionCapture.visible()
        binding.groupActionCaptured.gone()
    }

    private fun deleteAllCaptured() {

        CoroutineScope(Dispatchers.IO).launch {
            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
                val directoryPath = outputDirectory.absolutePath
                val rootDirectory = File(directoryPath)
                val mediaList = rootDirectory.listFiles { file ->
                    EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
                }?.sortedDescending()?.toMutableList() ?: mutableListOf()

                for (image in mediaList) {
                    image.delete()
                    MediaScannerConnection.scanFile(
                        this@CameraActivity, arrayOf(image.absolutePath), null, null
                    )

                }
                withContext(Dispatchers.Main) {}

            }
        }
    }

    private fun uriToBitmapGlide(uri: Uri): Bitmap {
        return Glide.with(this@CameraActivity).asBitmap().load(uri).submit().get()
    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {
        langModel?.let { model ->
            if (type == Constants.LANGUAGE_TYPE_SOURCE) {
                srcLangName = model.languageName
                srcLangCode = model.languageCode
                srcLangPosition = position

                viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
                viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
                viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
                binding.tvLangLeft.text = srcLangName


            } else if (type == Constants.LANGUAGE_TYPE_TARGET) {
                AppUtils.onLanguageChange?.invoke(
                    type,
                    model,
                    position
                )
                targetLangName = model.languageName
                targetLangCode = model.languageCode
                targetLangPosition = position

                viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName)
                viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode)
                viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
                binding.tvLangRight.text = targetLangName
            }

        }

    }

    private fun getResult() {
        if (Constants.isOnline(this)) {
            isAppOpenAdShow(false)
            binding.progressLayout.visible()
            binding.cropImage.setOnCropImageCompleteListener(this)
            binding.cropImage.getCroppedImageAsync()
        } else {
            showToast(getString(R.string.this_feature_is_not_available_offline), 0)
        }

    }

    private fun setExtractedText(result: String?) {
        isResultAvailable = true
        result?.let {
            launchCameraTranslation(it)
        }
    }

    private fun launchCameraTranslation(input: String) {
        val intent = Intent(this, CameraTranslateActivity::class.java)
        intent.putExtra("has_data", "yes")
        intent.putExtra(Constants.INPUT_TYPE_KEY, input)
        startActivityForResult(intent, Constants.REQUEST_CODE_INPUT)
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
        result?.let {
            viewModel.extractText(it.bitmap,
                srcLangCode,
                resultSuccess = { result ->
                    result?.let { text ->
                        showResultInterAd(text)
                    }
                },
                languageResult = { langCode, result ->
                    srcLangCode = langCode!!
                    srcLangName = viewModel.getLanguageNameFromList(srcLangCode)!!
                    srcLangPosition = viewModel.getLanguagePositionFromList(srcLangCode)

                    viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
                    viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
                    viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
                    binding.tvLangLeft.text = srcLangName
                    result?.let { text ->
                        showResultInterAd(text)
                    }
                },
                onErrorDetected = { error ->
                    val msg = if (error == "default_lang") {
                        getString(R.string.no_text_detected)
                    } else {
                        error
                    }
                    showToast(msg, 0)
                    binding.progressLayout.gone()
                })
        } ?: run {
            binding.progressLayout.gone()
            showToast(getString(R.string.cropping_failed), 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_LANG_SELECTOR) {
            if (binding.layoutRetake.isVisible) {

            } else {

            }
            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val langModel = getParcelableExtra("language_model") as LanguageModel?
                    val langType = getStringExtra("language_type")
                    val langPosition = getIntExtra("language_position", 0)
                    setLanguages(langType, langModel, langPosition)
                }

            }
        } else if (requestCode == Constants.REQUEST_CODE_GALLERY) {
            if (resultCode == RESULT_OK && null != data) {
                isGallery = true
                binding.progressLayout.visible()
                val selectedImageUri = data.data
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = uriToBitmapGlide(selectedImageUri!!)
                    bitmap.let { rotated ->
                        withContext(Dispatchers.Main) {

                            isToggleFlash = false
                            toggleFlash(isToggleFlash)
                            binding.cropLayout.visible()
                            binding.imageBlack.visible()
                            binding.cropImage.setImageBitmap(rotated)
                            binding.progressLayout.gone()
                            binding.groupActionCapture.gone()
                            binding.groupActionCaptured.visible()
                            binding.cameraView.gone()
                            //showCropBanner()
                            isPhotoCaptured = true

                        }
                    }
                }
            }
        } else if (requestCode == Constants.REQUEST_CODE_INPUT) {
            binding.progressLayout.gone()
            resetCamera()
            getLanguagesData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        runOnUiThread {
            if (isToggleFlash) {
                isToggleFlash = false
                toggleFlash(isToggleFlash)
            }
        }

    }

    private fun loadShowBannerAd() {
        if (isPremium().not() && isOnline(this@CameraActivity)) {
            binding.bannerContainerParent.show()
            AdsManagerX.loadBannerAd(this,
                AdConfigManager.BANNER_AD_CAMERA,
                binding.bannerContainer,
                onAdLoaded = {}, onAdFailed = {
                    binding.bannerContainerParent.hide()
                })
        } else {
            binding.bannerContainerParent.hide()
        }
    }

    private fun showResultInterAd(text: String) {
        binding.progressLayout.gone()
        if (!AdsManagerX.isAppOpenAdShowing(AdConfigManager.APP_OPEN)) {
            AdsManagerX.showInterAd(
                this@CameraActivity,
                AdConfigManager.INTER_AD_CAMERA_TRANSLATION.apply {
                    adConfig.fullScreenAdLoadingLayout = R.layout.loading_layout
                },
                onAdClose = {
                    setExtractedText(text)
                },
                funBlock = {
                    setExtractedText(text)
                })
        }
    }
}