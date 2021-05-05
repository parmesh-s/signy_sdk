package io.signy.signysdk.others.views

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.signy.signysdk.R
import io.signy.signysdk.others.interfaces.DialogListener


class DialogSuccessORFail(
    private val activity: Activity,
    private val dialogListener: DialogListener
) :
    Dialog(activity) {
    private var iv: ImageView? = null
    private var tvTitle: TextView? = null
    private var tvMsg: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.signy_sdk_dialog_success_or_fail)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window!!.attributes = lp
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        findViewById<Button>(R.id.btn_ok).setOnClickListener {
            dismiss()
            this.dialogListener.onOk("done")

        }
        findViewById<Button>(R.id.btn_retry).setOnClickListener {
            dismiss()
            this.dialogListener.onCancel()

        }
        iv = findViewById(R.id.iv)
        tvTitle = findViewById(R.id.tvTitle)
        tvMsg = findViewById(R.id.tvMsg)

    }

    fun setContent(status: Int, msg: String) {
        if (status == 1) {

            iv!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this.activity,
                    R.drawable.signy_sdk_ic_check_circle
                )
            )
            tvTitle!!.text = "Success"
            findViewById<Button>(R.id.btn_retry).visibility = View.GONE
            tvTitle!!.setTextColor(Color.parseColor("#34A853"))
        } else {
            iv!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this.activity,
                    R.drawable.signy_sdk_ic_fail_circle
                )
            )
            tvTitle!!.text = "Fail"
            findViewById<Button>(R.id.btn_retry).visibility = View.VISIBLE
            tvTitle!!.setTextColor(Color.parseColor("#EA4335"))
        }
        tvMsg!!.text = msg
    }
}
