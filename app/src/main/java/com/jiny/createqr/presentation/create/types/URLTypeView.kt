package com.jiny.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import com.jiny.createqr.R
import com.jiny.createqr.databinding.ViewTypeUrlBinding

class URLTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseQRTypeView(context, attrs, defStyleAttr) {

    private lateinit var inputBinding: ViewTypeUrlBinding

    override fun setupInputView() {
        inputBinding = ViewTypeUrlBinding.inflate(
            LayoutInflater.from(context),
            baseBinding.inputContainer,
            true
        )
    }

    override fun getQRData(): String {
        return inputBinding.etUrl.text.toString().trim()
    }

    override fun validateInput(data: String): Boolean {
        if (data.isEmpty()) {
            Toast.makeText(context, R.string.enter_text_or_url, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
