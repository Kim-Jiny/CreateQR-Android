package com.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import com.createqr.R
import com.createqr.databinding.ViewTypeSocialBinding

class InstagramTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseQRTypeView(context, attrs, defStyleAttr) {

    private lateinit var inputBinding: ViewTypeSocialBinding

    override fun setupInputView() {
        inputBinding = ViewTypeSocialBinding.inflate(
            LayoutInflater.from(context),
            baseBinding.inputContainer,
            true
        )
        inputBinding.tilUsername.hint = context.getString(R.string.instagram_username)
        inputBinding.tilUsername.placeholderText = "@username"
    }

    override fun getQRData(): String {
        var username = inputBinding.etUsername.text.toString().trim()
        if (username.startsWith("@")) {
            username = username.substring(1)
        }
        return "https://instagram.com/$username"
    }

    override fun validateInput(data: String): Boolean {
        if (inputBinding.etUsername.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_username, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
