package com.createqr.domain.usecase

import android.graphics.Bitmap
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.repository.QRGeneratorRepository
import javax.inject.Inject

class GenerateQRCodeUseCase @Inject constructor(
    private val repository: QRGeneratorRepository
) {
    operator fun invoke(
        data: String,
        size: Int = 512,
        qrColor: Int = 0xFF000000.toInt(),
        backgroundColor: Int = 0xFFFFFFFF.toInt(),
        logo: Bitmap? = null,
        logoStyle: LogoStyle = LogoStyle.SQUARE
    ): Bitmap? {
        return repository.generateQRCode(
            data = data,
            size = size,
            qrColor = qrColor,
            backgroundColor = backgroundColor,
            logo = logo,
            logoStyle = logoStyle
        )
    }
}
