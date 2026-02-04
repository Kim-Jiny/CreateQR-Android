package com.jiny.createqr.domain.model

import androidx.annotation.DrawableRes

data class QRTypeItem(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    val type: CreateType
)
