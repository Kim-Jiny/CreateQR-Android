package com.jiny.createqr.presentation.create.types

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.isVisible
import com.jiny.createqr.databinding.ViewQrTypeBaseBinding

abstract class BaseQRTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected val baseBinding: ViewQrTypeBaseBinding

    private var onGenerateListener: ((String) -> Unit)? = null
    private var onSaveListener: (() -> Unit)? = null
    private var onShareListener: (() -> Unit)? = null
    private var onColorListener: (() -> Unit)? = null
    private var onLogoListener: (() -> Unit)? = null

    init {
        baseBinding = ViewQrTypeBaseBinding.inflate(LayoutInflater.from(context), this, true)
        setupBaseButtons()
        setupInputView()
    }

    private fun setupBaseButtons() {
        baseBinding.apply {
            btnGenerate.setOnClickListener {
                val data = getQRData()
                if (validateInput(data)) {
                    onGenerateListener?.invoke(data)
                }
            }

            btnSave.setOnClickListener {
                onSaveListener?.invoke()
            }

            btnShare.setOnClickListener {
                onShareListener?.invoke()
            }

            btnColor.setOnClickListener {
                onColorListener?.invoke()
            }

            btnLogo.setOnClickListener {
                onLogoListener?.invoke()
            }
        }
    }

    abstract fun setupInputView()
    abstract fun getQRData(): String
    abstract fun validateInput(data: String): Boolean

    fun setQRImage(bitmap: Bitmap?) {
        baseBinding.ivQrCode.setImageBitmap(bitmap)
        baseBinding.qrActionsLayout.isVisible = bitmap != null
    }

    fun setOnGenerateListener(listener: (String) -> Unit) {
        onGenerateListener = listener
    }

    fun setOnSaveListener(listener: () -> Unit) {
        onSaveListener = listener
    }

    fun setOnShareListener(listener: () -> Unit) {
        onShareListener = listener
    }

    fun setOnColorListener(listener: () -> Unit) {
        onColorListener = listener
    }

    fun setOnLogoListener(listener: () -> Unit) {
        onLogoListener = listener
    }
}
