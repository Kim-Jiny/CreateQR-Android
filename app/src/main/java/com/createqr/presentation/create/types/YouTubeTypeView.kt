package com.createqr.presentation.create.types

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import com.jiny.createqr.R
import com.jiny.createqr.databinding.ViewTypeSocialBinding

class YouTubeTypeView @JvmOverloads constructor(
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
        inputBinding.tilUsername.hint = context.getString(R.string.youtube_channel)
        inputBinding.tilUsername.placeholderText = "@channel"
    }

    override fun getQRData(): String {
        var channel = inputBinding.etUsername.text.toString().trim()
        if (!channel.startsWith("@")) {
            channel = "@$channel"
        }
        return "https://youtube.com/$channel"
    }

    override fun validateInput(data: String): Boolean {
        if (inputBinding.etUsername.text.toString().trim().isEmpty()) {
            Toast.makeText(context, R.string.enter_channel, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
