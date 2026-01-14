package com.createqr.domain.model

data class QRItem(
    val id: Long = 0,
    val title: String,
    val qrImagePath: String?,
    val qrType: CreateType,
    val qrData: String,
    val qrColor: String = "FF000000",
    val backgroundColor: String = "FFFFFFFF",
    val logoPath: String? = null,
    val logoStyle: LogoStyle = LogoStyle.SQUARE,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LogoStyle {
    SQUARE,
    CIRCLE
}
