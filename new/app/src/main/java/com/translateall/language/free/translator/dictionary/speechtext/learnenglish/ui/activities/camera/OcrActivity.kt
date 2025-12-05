package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.activities.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImageView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.R
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.LanguageModel
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.database.entities.TranslationHistory
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.databinding.ActivityOcrBinding
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.network.TranslationUtils
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets.CustomCamView
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.*
import com.translateall.language.free.translator.dictionary.speechtext.learnenglish.utils.Constants.*
import kotlinx.coroutines.*
import java.util.*

class OcrActivity : AppCompatActivity(), CropImageView.OnCropImageCompleteListener {

    private lateinit var binding: ActivityOcrBinding
    private var mCamera: android.hardware.Camera? = null
    private var isToggleFlash = false

    private var srcLangCode: String? = null
    private var srcLangName: String? = null
    private var targetLangCode: String? = null
    private var targetLangName: String? = null

    private var srcLangPosition: Int = Constants.DEFAULT_SRC_LANG_POSITION
    private var targetLangPosition: Int = Constants.DEFAULT_TAR_LANG_POSITION

    private var isPhotoCaptured = false
    private var isGallery = false
    private var isResultAvailable = false

    private var translationUtils: TranslationUtils? = null


    private val viewModel: OcrViewModel by lazy {
        ViewModelProviders.of(this).get(OcrViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
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
//      setToolbar()
        setclickListeners()

        getLanguagesData()
    }

    private fun getLanguagesData() {
        srcLangCode = viewModel.getLangData(Constants.SOURCE_LANG_CODE_OCR)
        if (srcLangCode == "") {
            srcLangCode = "en"
            viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode!!)
        }
        srcLangName = viewModel.getLangData(Constants.SOURCE_LANG_NAME_OCR)
        if (srcLangName == "") {
            srcLangName = "English"
            viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName!!)
        }

        srcLangPosition = viewModel.getLangPosition(Constants.SOURCE_LANG_POSITION_OCR)

        if (srcLangPosition == -1) {
            srcLangPosition = Constants.DEFAULT_SRC_LANG_POSITION_OCR
            viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
        }

        targetLangCode = viewModel.getLangData(Constants.TARGET_LANG_CODE)
        if (targetLangCode == "") {
            targetLangCode = "fr"
            viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode!!)
        }

        targetLangName = viewModel.getLangData(Constants.TARGET_LANG_NAME)
        if (targetLangName == "") {
            targetLangName = "French"
            viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName!!)
        }

        targetLangPosition = viewModel.getLangPosition(Constants.TARGET_LANG_POSITION)
        if (targetLangPosition == -1) {
            targetLangPosition = Constants.DEFAULT_TAR_LANG_POSITION
            viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
        }

        binding.tvLangLeft.text = srcLangName
        binding.tvLangRight.text = targetLangName

    }

    private fun setclickListeners() {
        binding.ivBackOcr.setOnClickListener {
            onBackPressed()
        }
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
//            captureImage()
        }

        binding.layoutRetake.setOnClickListener {
            hideResultBox()
            resetCamera()
        }

        binding.tvLangLeft.setOnClickListener {
            openLanguageSheet(LANGUAGE_TYPE_SOURCE)
        }
        binding.tvLangRight.setOnClickListener {
            openLanguageSheet(LANGUAGE_TYPE_TARGET)
        }

        binding.layoutFlashToggle.setOnClickListener {
            onClickIcFlash()
        }
        binding.cameraImport.setOnClickListener {
            if (isDoubleClick()) {
                val i = Intent()
                i.type = "image/*"
                i.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(i, "Select Picture"),
                    REQUEST_CODE_GALLERY)
            }

        }
        binding.layoutRotate.setOnClickListener {
            binding.cropImage.rotateImage(90)
        }
        binding.layoutDone.setOnClickListener {
            if (isDoubleClick())
                getResult()
        }
        binding.ivClearOcr.setOnClickListener {
            hideResultBox()
        }
        binding.ivMoreOcr.setOnClickListener {
            presentData()
        }

        binding.resultLayout.setOnClickListener {
            hideResultBox()
        }
    }

    private fun presentData() {
        val mInputWord = binding.tvOcrInput.text.toString().trim()
        val mTranslatedWord = binding.tvOcrOutput.text.toString().trim()
        val mPrimaryId = mInputWord + srcLangName + targetLangName + mTranslatedWord
        var history = TranslationHistory().apply {
            inputWord = mInputWord
            translatedWord = mTranslatedWord
            srcLang = srcLangName
            targetLang = targetLangName
            srcCode = srcLangCode
            trCode = targetLangCode
            primaryId = mPrimaryId
            isFavorite = false
        }
        hideResultBox()
        val intent = Intent()
        intent.putExtra("history_model", history)
        setResult(Activity.RESULT_OK, intent)


        finish()
    }

    private fun getResult() {
        if (isOnline(this)) {
            binding.progressLayout.visible()
            binding.cropImage.setOnCropImageCompleteListener(this)
            binding.cropImage.getCroppedImageAsync()
        } else {
            showToast(getString(R.string.this_feature_is_not_available_offline), 0)
        }

    }

    private fun openLanguageSheet(type: String) {

        launchLanguageScreen(type)


    }

    private fun resetCamera() {
        isGallery = false
        isPhotoCaptured = false
        binding.cropLayout.gone()
        binding.cameraBack.gone()

        binding.groupActionCapture.visible()
        binding.groupActionCaptured.gone()

        checkVersionBelowM()
        initCamera()
    }

    private fun takePhoto() {
        binding.progressLayout.visibility = View.VISIBLE
        try {
            mCamera?.let { camera ->
                camera.takePicture(null, null,
                    android.hardware.Camera.PictureCallback { bytes: ByteArray, camera: android.hardware.Camera ->
                        Thread {
//                            val imageBitMap= imageProxyToBitmap(bytes)
                            val imageBitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            camera.stopPreview()
                            camera.release()
                            runOnUiThread {
                                isToggleFlash = false
//                                toggleFlash(isToggleFlash)
                                binding.flashToggle.setImageResource(R.drawable.ic_flashlight_off)
                            }
                            convert(imageBitMap)
                        }.start()
                    })
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun convert(bitmap: Bitmap) {
        decodeBitMap(bitmap) { decodedDitmap ->
            decodedDitmap?.let {
                setBitmapRotation(decodedDitmap)
            }

        }
    }

    private fun setBitmapRotation(imageBitMap: Bitmap) {
        if (cameraDisplay != null) {
            val rotation = cameraDisplay!!.rotation
            val rotatedBitmap = if (rotation != 0) {
                when (rotation) {
                    Surface.ROTATION_180 -> getImageBitMap(imageBitMap, 270)
                    Surface.ROTATION_270 -> getImageBitMap(imageBitMap, 180)
                    else -> getImageBitMap(imageBitMap, 360)
                }
            } else {
                getImageBitMap(imageBitMap, 90)
            }
            rotatedBitmap?.let {
                setImageForCropping(rotatedBitmap)
            }


        }

    }

    private fun setImageForCropping(image: Bitmap) {
        binding.cropImage.setImageBitmap(image)
        binding.cropLayout.visible()
        binding.groupActionCapture.gone()
        binding.groupActionCaptured.visible()
        binding.cameraBack.visible()

        if (isToggleFlash) {
            isToggleFlash = false
            toggleFlash(isToggleFlash)
            binding.flashToggle.setImageResource(R.drawable.ic_flashlight_off)
        }
        binding.progressLayout.gone()
        isPhotoCaptured = true
    }




    override fun onDestroy() {
        super.onDestroy()

    }


//    private fun aspectRatio(width: Int, height: Int): Int {
//        val previewRatio = max(width, height).toDouble() / min(width, height)
//        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
//            return AspectRatio.RATIO_4_3
//        }
//        return AspectRatio.RATIO_16_9
//    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feedback_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.feedback -> {
                sendEmail(arrayOf(resources.getString(R.string.email_address)),
                    resources.getString(R.string.email_subject),
                    "")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initCamera() {
        startCamera()
    }

    private fun startCamera() {
        if (isToggleFlash) {
            isToggleFlash = false
            toggleFlash(isToggleFlash)

        }
        kotlin.runCatching {
            mCamera = android.hardware.Camera.open()
            mCamera?.let { camera ->
                val customCamera = CustomCamView(this, camera)
                binding.cameraView.apply {
                    removeAllViews()
                    addView(customCamera)
                }
                var parameters: android.hardware.Camera.Parameters = camera.getParameters()
                parameters.pictureFormat = 256
                cameraDisplay = windowManager.defaultDisplay
                when (cameraDisplay!!.rotation) {
                    Surface.ROTATION_0 -> camera.setDisplayOrientation(90)
                    Surface.ROTATION_90 -> camera.setDisplayOrientation(0)
                    Surface.ROTATION_180 -> camera.setDisplayOrientation(270)
                    Surface.ROTATION_270 -> camera.setDisplayOrientation(180)
                }
                val supportedParameters = CameraConstants.getSupportedParameters(parameters,
                    windowManager.defaultDisplay.width)
                parameters = supportedParameters
                camera.parameters = parameters
                handler.sendEmptyMessageDelayed(0, 500)

            }
        }
    }

    private var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {
                0 -> {
                    setCamParams()
                    return
                }
                1 -> {
                    showToast(getString(R.string.error), 0)
                    return
                }
                else -> {}
            }
        }
    }

    fun setCamParams() {
        try {
            mCamera?.let { camera ->
                val parameters: android.hardware.Camera.Parameters = camera.getParameters()
                parameters.focusMode =
                    android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                camera.parameters = parameters
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFlash(value: Boolean) {
        if (value) {
            flashLightOn()
        } else {
            flashLightOff()
        }

    }

    private fun flashLightOff() {
        binding.flashToggle.setImageResource(R.drawable.ic_flashlight_off)
        binding.tvLabelFlash.text = "Flash Off"
        try {
            mCamera?.let { camera ->
                val parameters = camera.getParameters()
                parameters!!.flashMode = android.hardware.Camera.Parameters.FLASH_MODE_OFF
                camera.setParameters(parameters)

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun flashLightOn() {
        binding.flashToggle.setImageResource(R.drawable.ic_flashlight_on)
        binding.tvLabelFlash.text = "Flash On"
        try {
            mCamera?.let { camera ->
                val parameters = camera.getParameters()
                parameters.flashMode = android.hardware.Camera.Parameters.FLASH_MODE_TORCH
                camera.setParameters(parameters)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        checkVersionBelowM()
        initCamera()
    }

    private fun checkVersionBelowM() {
        if (Build.VERSION.SDK_INT < 23) {
            if (mCamera != null) {
                mCamera!!.release()
                mCamera = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_LANG_SELECTOR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    val langModel = getParcelableExtra("language_model") as LanguageModel?
                    val langType = getStringExtra("language_type")
                    val langPosition = getIntExtra("language_position", 0)
                    setLanguages(langType, langModel, langPosition)
                }


            }
        } else if (requestCode == REQUEST_CODE_GALLERY) {
            if (resultCode == RESULT_OK && null != data) {
                isGallery = true
                binding.progressLayout.visible()
                binding.cameraBack.visible()
                val selectedImageUri = data.data
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = uriToBitmapGlide(selectedImageUri!!)
                    bitmap.let { rotated ->
                        withContext(Dispatchers.Main) {
                            setImageForCropping(rotated)

                        }
                    }
                }
            }
        }
    }

    private fun uriToBitmapGlide(uri: Uri): Bitmap {
        return Glide.with(this@OcrActivity).asBitmap().load(uri).submit().get()
    }

    private fun setLanguages(type: String?, langModel: LanguageModel?, position: Int) {


        if (type == Constants.LANGUAGE_TYPE_SOURCE) {
            srcLangName = langModel?.languageName
            srcLangCode = langModel?.languageCode
            srcLangPosition = position

            viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName!!)
            viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode!!)
            viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
            binding.tvLangLeft.text = srcLangName


        } else if (type == Constants.LANGUAGE_TYPE_TARGET) {
            targetLangName = langModel?.languageName
            targetLangCode = langModel?.languageCode
            targetLangPosition = position

            viewModel.setLangData(Constants.TARGET_LANG_NAME, targetLangName!!)
            viewModel.setLangData(Constants.TARGET_LANG_CODE, targetLangCode!!)
            viewModel.putLangPosition(Constants.TARGET_LANG_POSITION, targetLangPosition)
            binding.tvLangRight.text = targetLangName
        }


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

    companion object {
        @JvmField
        var cameraDisplay: Display? = null

        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
        result?.let {
            viewModel.extractText(it.bitmap,
                srcLangCode!!,
                resultSuccess = { result ->

                    setExtractedText(result)
                },
                languageResult = { langCode, result ->
                    srcLangCode = langCode!!
                    srcLangName = viewModel.getLanguageNameFromList(srcLangCode!!)!!
                    srcLangPosition = viewModel.getLanguagePositionFromList(srcLangCode!!)

                    viewModel.setLangData(Constants.SOURCE_LANG_CODE_OCR, srcLangCode!!)
                    viewModel.setLangData(Constants.SOURCE_LANG_NAME_OCR, srcLangName!!)
                    viewModel.putLangPosition(Constants.SOURCE_LANG_POSITION_OCR, srcLangPosition)
                    binding.tvLangLeft.text = srcLangName
                    setExtractedText(result)
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

    private fun setExtractedText(result: String?) {
        isResultAvailable = true
        binding.progressLayout.gone()
        binding.resultLayout.visible()
        binding.progressOcr.visible()
        binding.tvOcrInput.text = result
        callTranslation(result!!, srcLangCode!!, targetLangCode!!)
    }

    private fun callTranslation(
        inputText: String,
        inputLanguageCode: String,
        translatedLanguageCode: String,
    ) {

        translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
            override fun onFailedResult() {
                runOnUiThread {
                    showToast(resources.getString(R.string.stt_error_network_error), 0)
                }
            }

            override fun onReceiveResult(result: String?) {
                runOnUiThread {
                    result?.let {
                        val outputWord = it
                        handleTranslationResult(inputText, outputWord)
                    }

                }
            }
        }, inputText, inputLanguageCode, translatedLanguageCode)
        translationUtils!!.execute()


    }

    fun handleTranslationResult(inputText: String, result: String) {
        binding.progressOcr.gone()
        binding.tvOcrOutput.text = result
        binding.ivMoreOcr.visible()
        binding.tvOcrOutput.visible()

        val mPrimaryId = inputText + srcLangName + targetLangName + result
        val translationModel = TranslationHistory().apply {
            inputWord = inputText
            translatedWord = result
            srcLang = srcLangName
            targetLang = targetLangName
            srcCode = srcLangCode
            trCode = targetLangCode
            primaryId = mPrimaryId
            isFavorite = false
        }
        insertHistory(translationModel)
    }

    private fun insertHistory(
        translationHistory: TranslationHistory,
    ) {

        runBlocking {
            viewModel.insertHistory(translationHistory)
        }

    }

    private fun hideResultBox() {
        isResultAvailable = false
        binding.resultLayout.gone()
        binding.ivMoreOcr.gone()
        binding.tvOcrOutput.gone()
        binding.tvOcrInput.text = ""
        binding.tvOcrOutput.text = ""
    }

    override fun onBackPressed() {
        if (isResultAvailable) {
            hideResultBox()

        } else if (isPhotoCaptured) {
            resetCamera()

        } else
            super.onBackPressed()
    }
}