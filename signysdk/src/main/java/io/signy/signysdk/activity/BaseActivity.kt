package io.signy.signysdk.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.signy.signysdk.Constant
import io.signy.signysdk.R
import io.signy.signysdk.others.helpers.dirChecker
import io.signy.signysdk.others.interfaces.PermissionListener
import java.io.File

open class BaseActivity : AppCompatActivity() {
    private var appFolder: File? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    private val PERMISSION_REQUEST_CODE = 1240
    fun getAppFolder(): File {
        if (appFolder == null) {
            appFolder = File(dirChecker(filesDir.path + "/AppData"))
            dirChecker(filesDir.path + "/AppData/" + Constant.CACHE_FOLDER_PREFIX)
        }

        return appFolder!!
    }

    fun setUpToolbar() {

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationIcon(R.drawable.signy_sdk_ic_arrow_back)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    var appPermissionsList: Array<String> = arrayOf();
    var permissionListener: PermissionListener? = null
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
    ): AlertDialog? {

        val builder = AlertDialog.Builder(this)
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