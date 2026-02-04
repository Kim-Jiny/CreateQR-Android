package com.jiny.createqr.domain.usecase

import com.jiny.createqr.domain.model.QRItem
import com.jiny.createqr.domain.repository.QRItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllQRItemsUseCase @Inject constructor(
    private val repository: QRItemRepository
) {
    operator fun invoke(): Flow<List<QRItem>> {
        return repository.getAllQRItems()
    }
}

class SaveQRItemUseCase @Inject constructor(
    private val repository: QRItemRepository
) {
    suspend operator fun invoke(item: QRItem): Long {
        return repository.insertQRItem(item)
    }
}

class DeleteQRItemUseCase @Inject constructor(
    private val repository: QRItemRepository
) {
    suspend operator fun invoke(item: QRItem) {
        repository.deleteQRItem(item)
    }
}

class UpdateQRItemUseCase @Inject constructor(
    private val repository: QRItemRepository
) {
    suspend operator fun invoke(item: QRItem) {
        repository.updateQRItem(item)
    }
}
