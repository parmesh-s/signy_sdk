package io.signy.signysdk.activity.addDocument

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.signy.signysdk.Constant
import io.signy.signysdk.R
import io.signy.signysdk.activity.BaseActivity
import io.signy.signysdk.activity.addDocument.adapters.DocumentAdapter
import io.signy.signysdk.others.interfaces.ListListener
import io.signy.signysdk.others.interfaces.PermissionListener
import io.signy.signysdk.room.getDocumentList
import kotlinx.android.synthetic.main.signy_sdk_activity_select_document.*


class SelectDocumentActivity : BaseActivity() {
    companion object {


        private const val PERMISSION_REQUEST_CODE = 1240
        private const val DOC_TYPE = "doc_type"


        var REQUEST_CODE = 201
        var RESULT_DOC_DATA = "DocData"
        var RESULT_DOC_NAME = "DocName"


        fun startActivityForResult(a: Activity,docType: String) {
            a.startActivityForResult(
                Intent(a, SelectDocumentActivity::class.java).putExtra(
                    DOC_TYPE,
                    docType
                ), REQUEST_CODE
            )
        }
    }

    private val appPermissions =
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signy_sdk_activity_select_document)
        setUpToolbar()
        if (checkAndRequestPermission(appPermissions, object : PermissionListener {
                override fun OnPermissionApprove() {
                    initList()
                }
            })) {
            initList()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initList() {
        val documents = getDocumentList()
        val adapter = DocumentAdapter(documents, object : ListListener {
            override fun onClickOnItem(position: Int) {

                val bDocPath =
                    getAppFolder().path + "/" + documents[position].name + "/" + Constant.DOC_FILE_NAME


                io.signy.signysdk.others.helpers.deleteFile(bDocPath)
                SelectDocumentImageActivity.startActivityForResult(
                    this@SelectDocumentActivity,
                    documents[position].name,
                    documents[position].key
                )
            }
        })
        rv.adapter = adapter

        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === SelectDocumentImageActivity.REQUEST_CODE && data != null) {

            val intent = Intent()
            intent.putExtra(RESULT_DOC_DATA, data.getStringExtra(RESULT_DOC_DATA))
            intent.putExtra(RESULT_DOC_NAME, data.getStringExtra(RESULT_DOC_NAME))
            setResult(REQUEST_CODE, intent)
            finish()//finishing activity
        }
    }
}
