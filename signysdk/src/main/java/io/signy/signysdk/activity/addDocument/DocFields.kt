package io.signy.signysdk.activity.addDocument

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import io.signy.signysdk.R
import io.signy.signysdk.activity.BaseActivity
import io.signy.signysdk.activity.addDocument.fieldExtractor.Fields
import io.signy.signysdk.model.DocumentField
import io.signy.signysdk.others.helpers.createTxtFolderOfDocument
import io.signy.signysdk.others.views.TextInputLayout
import kotlinx.android.synthetic.main.signy_sdk_activity_doc_fields.*
import org.json.JSONException

class DocFields : BaseActivity() {


    companion object {
        private var INTENT_KEY_DOC_NAME = "documentName"
        private var INTENT_KEY_EXTRACTED_FIELDS = "extractedDocFields"
        private var INTENT_KEY_DOC_IMAGES = "docImages"
        var REQUEST_CODE = 203
        fun startActivity(
            a: Activity,
            docName: String,
            extractedDocFields: HashMap<String, String>,
            docImagePaths: HashMap<String, String>
        ) {
            a.startActivityForResult(
                Intent(a, DocFields::class.java)
                    .putExtra(
                        INTENT_KEY_DOC_NAME,
                        docName
                    )
                    .putExtra(INTENT_KEY_EXTRACTED_FIELDS, extractedDocFields)
                    .putExtra(
                        INTENT_KEY_DOC_IMAGES, docImagePaths
                    )
                , REQUEST_CODE
            )
        }
    }

    private lateinit var extractedFields: HashMap<String, String>
    private lateinit var docImages: HashMap<String, String>
    private lateinit var docName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signy_sdk_activity_doc_fields)
        ivBack.setOnClickListener {
            finish()
        }
        btnRetry.setOnClickListener {
            finish()
        }
        extractedFields =
            intent.getSerializableExtra(INTENT_KEY_EXTRACTED_FIELDS) as HashMap<String, String>
        docImages = intent.getSerializableExtra(INTENT_KEY_DOC_IMAGES) as HashMap<String, String>
        docName = intent.getStringExtra(INTENT_KEY_DOC_NAME)


        initFields()
        btnNext.isActivated = true
        btnNext.setOnClickListener {
            if (btnNext.isActivated) {
                btnNext.isActivated = false
                saveDocument()
            }
        }
    }


    private fun validateField(): Boolean {
        var isValid = true
        for (i in 0 until llFields.childCount) {
            try {
                val til = llFields.getChildAt(i) as TextInputLayout

                if (til.editText!!.text.toString().trim().isEmpty()) {
                    isValid = false
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        btnRetry.visibility = View.VISIBLE
        if (!isValid) {
            btnNext.visibility = View.GONE
            showText.text = "Please use better quality image for information extraction."
        } else {
            btnNext.visibility = View.VISIBLE
            showText.text = getString(R.string.signy_sdk_detail_retry_option)
        }

        return isValid
    }

    @SuppressLint("ResourceAsColor")
    private fun initFields() {
        var newDocName = docName;
        if (newDocName!!.contains("Passport")) {
            newDocName = "Passport"
        }
        val fields = when (newDocName) {
            "Passport" -> Fields.passportFields
            "Pancard" -> Fields.panCardFields
            "Aadhaar Card" -> Fields.aadhaarCardFields
            "Emirates Id" -> Fields.emiratesIDFIelds
            "Driving Licence" -> Fields.licenceFields
            else -> listOf()
        }
        llFields.removeAllViews()
        for (label in fields) {
            var til = TextInputLayout(this)
//            til.isEnabled = false
//            til.editText!!.setTextColor(R.color.base_color)
//            til.editText!!.setHintTextColor(R.color.black)
            til.editText!!
                .setText(if (extractedFields.containsKey(label)) extractedFields[label] else "")
            til.hint = label
            llFields.addView(til)
        }

    }

    private fun saveDocument() {


        val docFields = mutableListOf<DocumentField>()
        var error = false
        for (i in 0 until llFields.childCount) {
            val til = llFields.getChildAt(i) as TextInputLayout
            if (til.editText!!.text.toString().trim().isBlank()) {
                til.error = "Please enter " + til.hint.toString()
                error = true
            }
        }
        if (error) {

            btnNext.isActivated = true

            return
        }

        for (i in 0 until llFields.childCount) {
            try {
                val til = llFields.getChildAt(i) as TextInputLayout

//                var cameFrom = "Manual"
//
//                var verificationStatus = getString(R.string.verified_by_self_msg)
////                Check is field value is match with ocr
//                if (extractedFields[til.hint.toString()].equals(
//                        til.editText!!.text.toString().trim(),
//                        true
//                    )
//                ) {
//                    cameFrom = "AI"
//                    verificationStatus = getString(R.string.verified_by_ai_msg) + " " + docName
//                }
                extractedFields.put(til.hint.toString(), til.editText!!.text.toString())
//                docFields.add(
//                    DocumentField(
//                        label = til.hint.toString(),
//                        value = til.editText!!.text.toString(),
////                        cameFrom = cameFrom,
//                        verificationStatus = ArrayList(listOf(verificationStatus))
//                    )
//                )


            } catch (e: JSONException) {
                e.printStackTrace()
                btnNext.isActivated = true
            }
        }


        val filePath = createTxtFolderOfDocument(
            getAppFolder(),
            docName,
            extractedFields,
            docImages
        )


        val intent = Intent()
        intent.putExtra(SelectDocumentActivity.RESULT_DOC_DATA, filePath)
        intent.putExtra(SelectDocumentActivity.RESULT_DOC_NAME, docName)
        setResult(REQUEST_CODE, intent)
        finish()


    }

}
