package com.createqr.data.repository

import android.graphics.*
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.repository.QRGeneratorRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import javax.inject.Inject

class QRGeneratorRepositoryImpl @Inject constructor() : QRGeneratorRepository {

    override fun generateQRCode(
        data: String,
        size: Int,
        qrColor: Int,
        backgroundColor: Int,
        logo: Bitmap?,
        logoStyle: LogoStyle
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) qrColor else backgroundColor
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            // Add logo if provided
            if (logo != null) {
                addLogoToQRCode(bitmap, logo, logoStyle)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addLogoToQRCode(qrBitmap: Bitmap, logo: Bitmap, logoStyle: LogoStyle): Bitmap {
        val combined = Bitmap.createBitmap(qrBitmap.width, qrBitmap.height, qrBitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combined)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        val logoSize = qrBitmap.width / 4
        val logoX = (qrBitmap.width - logoSize) / 2f
        val logoY = (qrBitmap.height - logoSize) / 2f

        val scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)

        when (logoStyle) {
            LogoStyle.CIRCLE -> {
                val circularLogo = getCircularBitmap(scaledLogo)
                canvas.drawBitmap(circularLogo, logoX, logoY, null)
            }
            LogoStyle.SQUARE -> {
                val roundedLogo = getRoundedCornerBitmap(scaledLogo, 16f)
                canvas.drawBitmap(roundedLogo, logoX, logoY, null)
            }
        }

        return combined
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        // Draw white background circle
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
        }

        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        // Draw white background with rounded corners
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }
}
