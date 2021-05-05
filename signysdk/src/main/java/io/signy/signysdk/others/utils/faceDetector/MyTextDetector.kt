package io.signy.signysdk.others.utils.faceDetector

import android.graphics.*
import android.util.Log
import android.util.SparseArray
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt


internal class MyTextDetector(
    private val mDelegate: Detector<TextBlock>,
    private val OnBitmapReceived: OnBitmapReceived
) : Detector<TextBlock>() {

    override fun detect(frame: Frame): SparseArray<TextBlock>? {
        val yuvImage = YuvImage(
            frame.grayscaleImageData.array(),
            ImageFormat.NV21,
            frame.metadata.width,
            frame.metadata.height,
            null
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, frame.metadata.width, frame.metadata.height),
            100,
            byteArrayOutputStream
        )
        val jpegArray = byteArrayOutputStream.toByteArray()
        var tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)
        val matrix = Matrix()
        matrix.postScale(0.5f, 0.5f)
//        tempBitmap = Bitmap.createBitmap(tempBitmap, 100, 100, 100, 100, matrix, true)
        //TempBitmap is a Bitmap version of a frame which is currently captured by your CameraSource in real-time
        //So you can process this TempBitmap in your own purposes adding extra code here

        Log.v("SIZES", tempBitmap.width.toString() + "_" + tempBitmap.height.toString())

        if (tempBitmap.width >= tempBitmap.height) {

            val height = (tempBitmap.height * .8).roundToInt()
            tempBitmap = Bitmap.createBitmap(
                tempBitmap,
                tempBitmap.width / 2 - height / 2,
                0,
                (height * 0.7).roundToInt(),
                height

            )

        } else {
            val width = (tempBitmap.width * .8).roundToInt()
            tempBitmap = Bitmap.createBitmap(
                tempBitmap,
                0,
                tempBitmap.height / 2 - width/ 2,

                width,
                (width * 0.7).roundToInt()
            )
        }
        OnBitmapReceived.bitmapReceived(tempBitmap)


        return mDelegate.detect(frame)
    }

    override fun isOperational(): Boolean {
        return mDelegate.isOperational
    }

    override fun setFocus(id: Int): Boolean {
        return mDelegate.setFocus(id)
    }
}