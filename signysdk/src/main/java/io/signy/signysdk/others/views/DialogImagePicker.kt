package io.signy.signysdk.others.views

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import io.signy.signysdk.R
import io.signy.signysdk.others.helpers.getRealPathFromURICamera
import io.signy.signysdk.others.helpers.getRealPathFromURIDevice
import io.signy.signysdk.others.helpers.reduceImageSize
import kotlinx.android.synthetic.main.signy_sdk_camera_gallery_dialog.*
import java.io.File


interface ImagePickerDialogListener {
    fun choseFromGallery()
    fun takePhoto()

}


class DialogImagePicker(
    private val activity: Activity,
    private val imagePickerDialogListener: ImagePickerDialogListener,
    private val type: String
) :
    Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.signy_sdk_camera_gallery_dialog)
        if (type == "selfie") {
            tvChooseFromGallrey.visibility = View.GONE
            cameraGalleryView.visibility = View.GONE
        } else {
            tvChooseFromGallrey.visibility = View.VISIBLE
            cameraGalleryView.visibility = View.VISIBLE
        }
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tvChooseFromGallrey.setOnClickListener { imagePickerDialogListener.choseFromGallery() }
        tvTakePhoto.setOnClickListener { imagePickerDialogListener.takePhoto() }
        tvCancel.setOnClickListener {
            try {
                this.dismiss()
            } catch (e: Exception) {
            }
        }
    }

    fun getOriginalFilePathFromUri(uri: Uri, pckFrom: String): String {
        return when (pckFrom) {
            "camera" -> getRealPathFromURICamera(activity, uri)!!
            "app" -> uri.toString()
            else -> getRealPathFromURIDevice(activity, uri)
        }
    }



    companion object {
        var PICK_IMAGE_FROM_DEVICE_REQUEST = 151
        var PICK_IMAGE_FROM_CAMERA_REQUEST = 152
    }
}
