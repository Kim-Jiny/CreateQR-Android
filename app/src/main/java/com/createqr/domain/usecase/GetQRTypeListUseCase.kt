package com.createqr.domain.usecase

import com.createqr.domain.model.QRTypeItem
import com.createqr.domain.repository.QRTypeRepository
import javax.inject.Inject

class GetQRTypeListUseCase @Inject constructor(
    private val repository: QRTypeRepository
) {
    operator fun invoke(): List<QRTypeItem> {
        return repository.getQRTypeList()
    }
}
