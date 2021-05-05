package io.signy.signysdk.activity.addDocument

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.error.VolleyError
import com.android.volley.request.SimpleMultiPartRequest
import com.android.volley.toolbox.Volley
import io.signy.signysdk.Constant
import io.signy.signysdk.Constant.API_KEY
import io.signy.signysdk.Constant.OCR_URL
import io.signy.signysdk.R
import io.signy.signysdk.activity.BaseActivity
import io.signy.signysdk.activity.addDocument.SelectDocumentActivity.Companion.RESULT_DOC_DATA
import io.signy.signysdk.activity.addDocument.SelectDocumentActivity.Companion.RESULT_DOC_NAME
import io.signy.signysdk.activity.addDocument.fieldExtractor.Fields

import io.signy.signysdk.others.helpers.*
import io.signy.signysdk.others.views.DialogImagePicker
import io.signy.signysdk.others.views.ImagePickerDialogListener
import kotlinx.android.synthetic.main.signy_sdk_activity_select_document_image.*
import org.json.JSONObject
import java.io.File

class SelectDocumentImageActivity : BaseActivity() {

    companion object {
        var INTENT_KEY_DOC_NAME = "documentName"
        var INTENT_KEY_DOC_KEY = "documentKey"
        var REQUEST_CODE = 202
        fun startActivityForResult(a: Activity, docName: String, docKey: String) {
            a.startActivityForResult(
                Intent(a, SelectDocumentImageActivity::class.java).putExtra(
                    INTENT_KEY_DOC_NAME,
                    docName
                ).putExtra(
                    INTENT_KEY_DOC_KEY,
                    docKey
                ), REQUEST_CODE
            )
        }
    }

    private lateinit var selectedTextView: TextView
    private var cameraImageUri: Uri? = null
    private var dialogImagePicker: DialogImagePicker? = null


    private var documentName: String? = null
    private var documentKey: String? = null

    private val documentImagePaths = HashMap<String, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signy_sdk_activity_select_document_image)
        documentName = intent.getStringExtra(INTENT_KEY_DOC_NAME)
        documentKey = intent.getStringExtra(INTENT_KEY_DOC_KEY)
        setUpToolbar()
        toolbar.title = documentName

        init()

    }


    private fun init() {
        tvDocument1.setOnClickListener {
            pickDocumentImage(it, "front")
        }
        tvDocument2.setOnClickListener {

            pickDocumentImage(it, "back")
        }

        tvDocument1.setOnLongClickListener {
            removeImage(it)
        }
        tvDocument2.setOnLongClickListener {
            removeImage(it)
        }

        btnNext.setOnClickListener { scanImages() }

        tvStart2.text = " "
        if (documentName!!.contains("Emirates")) {
            tvStart2.text = "*"
        }
    }


    private fun pickDocumentImage(v: View, type: String) {
        selectedTextView = v as TextView
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
                if (selectedTextView.contentDescription != getString(R.string.signy_sdk_upload_selfie)) {
                    val intent =
                        Intent(this@SelectDocumentImageActivity, CaptureDocument::class.java)
                    intent.putExtra(
                        CaptureDocument.DOCUMENT_KEY,
                        documentName!!.toLowerCase().replace("\\s".toRegex(), "_")
                    )
                    intent.putExtra(
                        CaptureDocument.PAGE_TYPE,
                        selectedTextView.contentDescription
                    )
                    startActivityForResult(
                        intent,
                        CaptureDocument.RESULT_CODE
                    )// Activity is started with requestCode 2
                } else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val values = ContentValues(1)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                    cameraImageUri = contentResolver?.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    startActivityForResult(
                        intent,
                        DialogImagePicker.PICK_IMAGE_FROM_CAMERA_REQUEST
                    )
                }


            }
        }, type)
//        }
        dialogImagePicker?.show()
    }

    private fun removeImage(v: View): Boolean {
        val key = v.contentDescription.toString()

        if (documentImagePaths[key] != null) {
            documentImagePaths.remove(key)

        }
        selectedTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DialogImagePicker.PICK_IMAGE_FROM_DEVICE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imgUri = data!!.data
            updateImage(imgUri, "device")
        } else if (requestCode == DialogImagePicker.PICK_IMAGE_FROM_CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            updateImage(cameraImageUri, "camera")
        } else if (requestCode == CaptureDocument.RESULT_CODE) {
            try {
                val imgUri = data!!.getStringExtra(CaptureDocument.RESULT_IMG_PATH)
                updateImage(Uri.parse(imgUri), "app")
            } catch (e: Exception) {
            }

        } else if (requestCode === DocFields.REQUEST_CODE) {


            if (data != null) {
                val intent = Intent()
                intent.putExtra(RESULT_DOC_DATA, data.getStringExtra(RESULT_DOC_DATA))
                intent.putExtra(RESULT_DOC_NAME, data.getStringExtra(RESULT_DOC_NAME))
                setResult(REQUEST_CODE, intent)
                finish()//finishing activity
            }
        }
    }


    private fun updateImage(imgUri: Uri?, pickFrom: String) {
        val imageFile = dialogImagePicker?.getOriginalFilePathFromUri(imgUri!!, pickFrom)
        selectedTextView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            R.drawable.signy_sdk_tick_green,
            0
        )
        documentImagePaths[selectedTextView.contentDescription.toString()] = imageFile!!

    }

    private fun showFrontPageMsg(msg: String) {
        showToast(msg)
    }

    private fun scanImages() {
        if (!documentImagePaths.containsKey(getString(R.string.signy_sdk_upload_front_image))) {
            showFrontPageMsg("Please add Front page image of document.")

            return
        }
        if (documentName.equals("Emirates Id")) {
            if (!documentImagePaths.containsKey(getString(R.string.signy_sdk_upload_front_image))) {
                showFrontPageMsg("Please add Front page image of document.")
                return
            }
            if (!documentImagePaths.containsKey(getString(R.string.signy_sdk_upload_back_image))) {
                showFrontPageMsg("Please add back image of Document.")
                return
            }
        }
        var newDocName = documentName;
        if (newDocName!!.contains("Passport")) {
            newDocName = "Passport"
        }

        extractDataFromImage(
            newDocName,
            documentImagePaths[getString(R.string.signy_sdk_upload_front_image)]!!
        )
    }

    private fun extractDataFromImage(document: String, imageFilePath: String) {
        val url = when (document) {
            "Passport" -> "readPassport"
            "Pancard" -> "readPan"
            "Aadhaar Card" -> "readAadhaar"
            else -> ""
        }


        val rotatedFile = getRotatedFilePath(getAppFolder().path, File(imageFilePath))
        val reducedImagePath =
            saveByteToFile(getAppFolder(), reduceImageSize(rotatedFile), url)
        pb.visibility = View.VISIBLE
        val smr = object: SimpleMultiPartRequest(
            Request.Method.POST, OCR_URL + url,
            Response.Listener { response: String? ->

                try {
                    val jObj = JSONObject(response).getJSONObject("response").getJSONObject("data")
                    when (document) {
                        "Passport" -> {
                            val fields = HashMap<String, String>()
//                            var mrz = jObj.getString("mrz").toString()
//                            mrz = mrz.substring(0, 44) + "\n" + mrz.substring(44, mrz.length);
//                            val record = MrzParser.parse(mrz)


                            fields[Fields.passportFields[0]] = jObj.getString("name")

                            fields[Fields.passportFields[1]] = jObj.getString("passport")
                            fields[Fields.passportFields[2]] = jObj.getString("dob")
                            fields[Fields.passportFields[3]] = jObj.getString("nationality")
                            fields[Fields.passportFields[5]] =
                                if (jObj.has("gender")) jObj.getString("gender") else ""

                            fields[Fields.passportFields[4]] = jObj.getString("doe")

                            onExtractionComplete(fields)
                        }
                        "Pancard" -> {
                            val fields = HashMap<String, String>()

                            fields.put(Fields.panCardFields[0], jObj.getString("name"))
                            fields.put(Fields.panCardFields[1], jObj.getString("pan"))
                            fields.put(Fields.panCardFields[2], jObj.getString("dob"))
                            onExtractionComplete(fields)
                        }
                        "Aadhaar Card" -> {
                            val fields = HashMap<String, String>()

                            fields.put(Fields.aadhaarCardFields[0], jObj.getString("Name"))
                            fields.put(Fields.aadhaarCardFields[1], jObj.getString("DoB"))
                            fields.put(Fields.aadhaarCardFields[2], jObj.getString("Gender"))
                            fields.put(Fields.aadhaarCardFields[3], jObj.getString("Aadhaar_no"))
                            onExtractionComplete(fields)
                        }
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        pb.visibility = View.GONE
                        showToast(e.message.toString())
                        onExtractionComplete(HashMap())
                    }
                }

            }, Response.ErrorListener { error: VolleyError ->
                runOnUiThread {
                    pb.visibility = View.GONE
                    try {

                        showToast(
                            JSONObject(String(error.networkResponse.data)).getJSONObject("response")
                                .getString("message")
                        )
                    } catch (e: java.lang.Exception) {
                        showToast("Error while extracting data");
                    }
                }
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $API_KEY"
                return headers
            }
        }

        //The file param name is "profile-pic". and Content-type is form-data
        smr.addFile("file", reducedImagePath.path)
        smr.retryPolicy = DefaultRetryPolicy(
            60 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        val mRequestQueue =
            Volley.newRequestQueue(applicationContext)
        mRequestQueue.add(smr);
    }


    private fun onExtractionComplete(extractedFields: HashMap<String, String>) {

        runOnUiThread {
            pb.visibility = View.GONE
            if (!Constant.SHOW_CONFIRM_DOCUMENT_FIELD_SCREEN) {
                val filePath = createTxtFolderOfDocument(
                    getAppFolder(),
                    documentName!!,
                    extractedFields,
                    documentImagePaths
                )


                val intent = Intent()
                intent.putExtra(SelectDocumentActivity.RESULT_DOC_DATA, filePath)
                intent.putExtra(SelectDocumentActivity.RESULT_DOC_NAME, documentName)
                setResult(REQUEST_CODE, intent)
                finish()//finishing activity
            } else {
                DocFields.startActivity(
                    this@SelectDocumentImageActivity,
                    documentName!!,
                    extractedFields,
                    documentImagePaths
                )
            }
        }
    }

}
