package com.jiny.createqr.domain.repository

import android.graphics.Bitmap
import com.jiny.createqr.domain.model.LogoStyle

interface QRGeneratorRepository {
    fun generateQRCode(
        data: String,
        size: Int = 512,
        qrColor: Int = 0xFF000000.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt(),
        logo: Bitmap? = null,
        logoStyle: LogoStyle = LogoStyle.SQUARE
    ): Bitmap?
}
