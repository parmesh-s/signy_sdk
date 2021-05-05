package io.signy.signysdk.others.utils.faceDetector

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.SparseArray
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import java.io.ByteArrayOutputStream


internal class MyFaceDetector(
    private val mDelegate: Detector<Face>,
    private val OnBitmapReceived: OnBitmapReceived
) : Detector<Face>() {

    override fun detect(frame: Frame): SparseArray<Face> {
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
        val tempBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.size)

        //TempBitmap is a Bitmap version of a frame which is currently captured by your CameraSource in real-time
        //So you can process this TempBitmap in your own purposes adding extra code here
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