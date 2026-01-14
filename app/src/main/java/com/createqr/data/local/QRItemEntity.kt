package com.createqr.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.createqr.domain.model.CreateType
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.model.QRItem

@Entity(tableName = "qr_items")
data class QRItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val qrImagePath: String?,
    val qrType: String,
    val qrData: String,
    val qrColor: String,
    val backgroundColor: String,
    val logoPath: String?,
    val logoStyle: String,
    val createdAt: Long
) {
    fun toDomainModel(): QRItem {
        return QRItem(
            id = id,
            title = title,
            qrImagePath = qrImagePath,
            qrType = CreateType.valueOf(qrType),
            qrData = qrData,
            qrColor = qrColor,
            backgroundColor = backgroundColor,
            logoPath = logoPath,
            logoStyle = LogoStyle.valueOf(logoStyle),
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(item: QRItem): QRItemEntity {
            return QRItemEntity(
                id = item.id,
                title = item.title,
                qrImagePath = item.qrImagePath,
                qrType = item.qrType.name,
                qrData = item.qrData,
                qrColor = item.qrColor,
                backgroundColor = item.backgroundColor,
                logoPath = item.logoPath,
                logoStyle = item.logoStyle.name,
                createdAt = item.createdAt
            )
        }
    }
}
