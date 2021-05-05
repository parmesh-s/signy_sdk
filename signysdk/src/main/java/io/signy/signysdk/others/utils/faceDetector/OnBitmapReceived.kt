package io.signy.signysdk.others.utils.faceDetector

import android.graphics.Bitmap

interface OnBitmapReceived{
    fun bitmapReceived(bitmap: Bitmap)
}