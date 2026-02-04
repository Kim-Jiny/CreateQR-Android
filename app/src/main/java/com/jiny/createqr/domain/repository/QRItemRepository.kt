package com.jiny.createqr.domain.repository

import com.jiny.createqr.domain.model.QRItem
import kotlinx.coroutines.flow.Flow

interface QRItemRepository {
    fun getAllQRItems(): Flow<List<QRItem>>
    suspend fun getQRItemById(id: Long): QRItem?
    suspend fun insertQRItem(item: QRItem): Long
    suspend fun updateQRItem(item: QRItem)
    suspend fun deleteQRItem(item: QRItem)
    suspend fun deleteQRItemById(id: Long)
}
