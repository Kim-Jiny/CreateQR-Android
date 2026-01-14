package com.createqr.data.repository

import com.createqr.data.local.QRItemDao
import com.createqr.data.local.QRItemEntity
import com.createqr.domain.model.QRItem
import com.createqr.domain.repository.QRItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QRItemRepositoryImpl @Inject constructor(
    private val qrItemDao: QRItemDao
) : QRItemRepository {

    override fun getAllQRItems(): Flow<List<QRItem>> {
        return qrItemDao.getAllQRItems().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getQRItemById(id: Long): QRItem? {
        return qrItemDao.getQRItemById(id)?.toDomainModel()
    }

    override suspend fun insertQRItem(item: QRItem): Long {
        return qrItemDao.insertQRItem(QRItemEntity.fromDomainModel(item))
    }

    override suspend fun updateQRItem(item: QRItem) {
        qrItemDao.updateQRItem(QRItemEntity.fromDomainModel(item))
    }

    override suspend fun deleteQRItem(item: QRItem) {
        qrItemDao.deleteQRItem(QRItemEntity.fromDomainModel(item))
    }

    override suspend fun deleteQRItemById(id: Long) {
        qrItemDao.deleteQRItemById(id)
    }
}
