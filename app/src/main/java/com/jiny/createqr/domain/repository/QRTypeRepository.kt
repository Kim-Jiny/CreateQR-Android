package com.jiny.createqr.domain.repository

import com.jiny.createqr.domain.model.QRTypeItem

interface QRTypeRepository {
    fun getQRTypeList(): List<QRTypeItem>
}
