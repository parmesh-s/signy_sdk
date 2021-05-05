package io.signy.signysdk.activity.addDocument

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.SparseArray
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Engine
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import io.signy.signysdk.Constant.CACHE_FOLDER_PREFIX
import io.signy.signysdk.R
import io.signy.signysdk.activity.BaseActivity

import io.signy.signysdk.others.helpers.getBitmapRotatedByDegree
import io.signy.signysdk.others.helpers.saveBitmapToFile
import kotlinx.android.synthetic.main.signy_sdk_activity_capture_document.*
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class CaptureDocument : BaseActivity() {

    companion object {
        var DOCUMENT_KEY = "documentKey"
        var PAGE_TYPE = "pageType"

        val RESULT_IMG_PATH = "imagePath"
        val RESULT_PAGE_TYPE = "pageType"

        val RESULT_CODE = 9
    }

    var documentKey: String? = "documentKey"
    var pageType: String? = "page"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signy_sdk_activity_capture_document)

        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.signy_sdk_black2))
        documentKey = intent.getStringExtra(DOCUMENT_KEY)
        pageType = intent.getStringExtra(PAGE_TYPE)


        setUpToolbar()
        mPreview.audio = Audio.OFF;
        mPreview.engine = Engine.CAMERA2
        mPreview.facing = Facing.BACK
        mPreview.mode = Mode.PICTURE
mPreview.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
        btnCapture.setOnClickListener {
            mPreview.takePicture()
            //            takePhoto()
        }
        ivFlipCamera.setOnClickListener {
            if (mPreview.facing == Facing.FRONT) {
                mPreview.facing = Facing.BACK
                return@setOnClickListener
            }
            mPreview.facing = Facing.FRONT
        }
        createCameraSource()

    }

    private fun createCameraSource() {
        val textRecognizer = TextRecognizer.Builder(applicationContext)

            .build()


        mPreview.setLifecycleOwner(this)
//        mPreview.addFrameProcessor {
//
//            val bmp = getBitmap(it)
//            val frame = Frame.Builder().setBitmap(bmp).build()
//            receiveDetections(textRecognizer.detect(frame))
//        }
        mPreview.useDeviceOrientation = false
        mPreview.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                // Picture was taken!
                // If planning to show a Bitmap, we will take care of
                // EXIF rotation and background threading for you...


                result.toBitmap(-1, -1) { srcBmp ->
                    val dstBmp: Bitmap?
                    if (srcBmp!!.width >= srcBmp.height) {

                        dstBmp = Bitmap.createBitmap(
                            srcBmp,
                            srcBmp.width / 2 - srcBmp.height / 2 + (srcBmp.width * .1).roundToInt(),
                            0,
                            (srcBmp.height * .75).roundToInt(),
                            srcBmp.height
                        )

                    } else {

                        dstBmp = Bitmap.createBitmap(
                            srcBmp,
                            0,
                            srcBmp.height / 2 - srcBmp.width / 2 + (srcBmp.height * .1).roundToInt(),
                            srcBmp.width,
                            (srcBmp.width * .75).roundToInt()
                        )
                    }


                    val imageFile = saveBitmapToFile(
                        getAppFolder().path + "/" + CACHE_FOLDER_PREFIX,
                        documentKey + "_" + pageType,
                        dstBmp!!
                    )
                    val intent = Intent()
                    if (imageFile != null) {
                        intent.putExtra(RESULT_IMG_PATH, imageFile.path)
                    }
                    intent.putExtra(RESULT_PAGE_TYPE, pageType)
                    setResult(RESULT_CODE, intent)
                    finish()//finishing activity
                }

            }
        })

    }


    private fun getBitmap(frame: com.otaliastudios.cameraview.frame.Frame): Bitmap? {
        val yuvImage = YuvImage(
            frame.getData(),
            ImageFormat.NV21,
            frame.size.width,
            frame.size.height,
            null
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, frame.size.width, frame.size.height),
            100,
            byteArrayOutputStream
        )
        val jpegArray = byteArrayOutputStream.toByteArray()
        val tempBitmap =
            BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)


        return getBitmapRotatedByDegree(tempBitmap, 90)
    }


}
