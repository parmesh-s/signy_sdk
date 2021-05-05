package io.signy.signy_sdk_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.signy.signysdk.SignySDK
import io.signy.signysdk.others.helpers.showToast
import io.signy.signysdk.others.interfaces.OnFaceMatchComplete
import io.signy.signysdk.others.interfaces.PermissionListener
import io.signy.signysdk.others.views.DialogImagePicker
import io.signy.signysdk.others.views.ImagePickerDialogListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var cameraImageUri: Uri? = null
    private var dialogImagePicker: DialogImagePicker? = null
    private val apiKey =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZWQ5MGQ1YTY1ZTM1YjFkOGI5NTcxMTUiLCJlbWFpbCI6ImhyQHNpZ255LmlvIiwicm9sZSI6IkNMSUVOVCIsImV4cCI6MTU5NzU3Mjk3OSwiaWF0IjoxNTkyMzg4OTc5fQ.h3Q3CZNtoKL_0cB9FWtnZrvgbyS_gz_RxVn8nEHhSLg"

    private val appPermissions =
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if (checkAndRequestPermission(appPermissions, object : PermissionListener {
//                override fun OnPermissionApprove() {
//                    init()
//                }
//            })) {
//            init()
//        }
        SignySDK.getDocument(this, apiKey)
//        SignySDK.startLiveliness(this)
    }


    private fun init() {
        btnImages1.setOnClickListener {
            pickDocumentImage(it, "front")
        }
        btnImages2.setOnClickListener {
            pickDocumentImage(it, "back")
        }

        btnCompare.setOnClickListener {
            if (file1 != null && file2 != null) {

                try {

                    SignySDK.compareFaces(
                        this,
                        apiKey,
                        file1!!,
                        file2!!,
                        object : OnFaceMatchComplete {
                            override fun onComplete(b: Boolean) {
                                runOnUiThread {
                                    tvResult.text =
                                        if (b) "Result : Face Match" else "Result : Face does not match"
                                }
                            }

                            override fun onFail() {
                                runOnUiThread {
                                    showToast("Something wrong...")
                                }
                            }

                        })

                } catch (e: Exception) {
                    runOnUiThread {
                        showToast(e.message.toString())
                    }
                }
            } else
                showToast("Please select all image first")
        }
    }

    var selectedButton: Button? = null
    private fun pickDocumentImage(v: View, type: String) {
        selectedButton = v as Button
        selectedButton?.contentDescription = type
        /*if (dialogImagePicker == null) {*/
        dialogImagePicker = DialogImagePicker(this, object : ImagePickerDialogListener {
            override fun choseFromGallery() {
                dialogImagePicker?.dismiss()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, DialogImagePicker.PICK_IMAGE_FROM_DEVICE_REQUEST)
            }

            override fun takePhoto() {
                dialogImagePicker?.dismiss()

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val values = ContentValues(1)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                cameraImageUri = contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivityForResult(
                    intent,
                    DialogImagePicker.PICK_IMAGE_FROM_CAMERA_REQUEST
                )


            }
        }, type)
//        }
        dialogImagePicker?.show()
    }

    var appPermissionsList: Array<String> = arrayOf()
    var permissionListener: PermissionListener? = null
    private val PERMISSION_REQUEST_CODE = 1240

    fun checkAndRequestPermission(
        appPermissions: Array<String>,
        listener: PermissionListener? = permissionListener
    ): Boolean {
        appPermissionsList = appPermissions
        this.permissionListener = listener
        val listPermissionNeeded = ArrayList<String>()
        for (perm: String in appPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(perm)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toArray(arrayOfNulls<String>(listPermissionNeeded.size)),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DialogImagePicker.PICK_IMAGE_FROM_DEVICE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imgUri = data!!.data
            updateImage(imgUri, "device")
        } else if (requestCode == DialogImagePicker.PICK_IMAGE_FROM_CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            updateImage(cameraImageUri, "camera")
        }
    }


    var file1: File? = null
    var file2: File? = null

    private fun updateImage(imgUri: Uri?, pickFrom: String) {
        val imageFile = dialogImagePicker?.getOriginalFilePathFromUri(imgUri!!, pickFrom)

        selectedButton?.setBackgroundColor(Color.parseColor("#6200EE"))
        if (selectedButton?.contentDescription.toString() == "front") {
            file1 = File(imageFile)
        } else if (selectedButton?.contentDescription.toString() == "back") {
            file2 = File(imageFile)
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val permissionResult = HashMap<String, Int>()
            var deniedCount = 0
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResult.put(permissions[i], grantResults[i])
                    deniedCount++
                }
            }

            if (deniedCount == 0) {
                if (this.permissionListener != null)
                    this.permissionListener!!.OnPermissionApprove()
            } else {
                for (entry: Map.Entry<String, Int> in permissionResult.entries) {
                    val permName = entry.key
                    val permResult = entry.value

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                        showDialog(
                            "",
                            "This app needs Storage and Camera permissions to work without and problems.",
                            "Yes, Grant permissions",
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                checkAndRequestPermission(appPermissionsList)
                            },
                            "No",
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            },
                            false
                        )
                    } else {
                        showDialog(
                            "",
                            "You have denied some permissions. Allow all permissions at [Setting] > [Permissions]",
                            "Go to Settings",
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                val i = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            },
                            "No",
                            DialogInterface.OnClickListener { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            },
                            false
                        )
                        break
                    }
                }
            }
        }
    }

    fun showDialog(
        title: String,
        msg: String,
        positiveLabel: String,
        positiveOnClick: DialogInterface.OnClickListener,
        negativeLabel: String,
        negativeOnClick: DialogInterface.OnClickListener,
        isCancelAbel: Boolean = true
    ): android.app.AlertDialog? {

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setCancelable(isCancelAbel)
        builder.setMessage(msg)
        builder.setPositiveButton(positiveLabel, positiveOnClick)
        builder.setPositiveButton(negativeLabel, negativeOnClick)

        val alert = builder.create()
        alert.show()
        return alert
    }
}
