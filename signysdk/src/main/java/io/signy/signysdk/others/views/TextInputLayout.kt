package io.signy.signysdk.others.views

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout


class TextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setupView()
    }

    private fun setupView() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 10, 0, 10)
        layoutParams = params
        val editText = EditText(context)
        val editTextParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
//        editText.setTextColor(ContextCompat.getColor(context, R.color.white))

        addView(editText, editTextParams)
    }
}
