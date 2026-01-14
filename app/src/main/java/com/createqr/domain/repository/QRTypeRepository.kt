package com.createqr.domain.repository

import com.createqr.domain.model.QRTypeItem

interface QRTypeRepository {
    fun getQRTypeList(): List<QRTypeItem>
}
