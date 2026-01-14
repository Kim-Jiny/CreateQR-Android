package com.createqr.data.repository

import com.createqr.R
import com.createqr.domain.model.CreateType
import com.createqr.domain.model.QRTypeItem
import com.createqr.domain.repository.QRTypeRepository
import javax.inject.Inject

class QRTypeRepositoryImpl @Inject constructor() : QRTypeRepository {

    override fun getQRTypeList(): List<QRTypeItem> {
        return listOf(
            QRTypeItem(
                id = "urlType",
                title = "Text",
                iconRes = R.drawable.ic_text,
                type = CreateType.URL
            ),
            QRTypeItem(
                id = "wifiType",
                title = "WiFi",
                iconRes = R.drawable.ic_wifi,
                type = CreateType.WIFI
            ),
            QRTypeItem(
                id = "contactType",
                title = "Contact",
                iconRes = R.drawable.ic_contact,
                type = CreateType.CONTACT
            ),
            QRTypeItem(
                id = "instagramType",
                title = "Instagram",
                iconRes = R.drawable.ic_instagram,
                type = CreateType.INSTAGRAM
            ),
            QRTypeItem(
                id = "youtubeType",
                title = "YouTube",
                iconRes = R.drawable.ic_youtube,
                type = CreateType.YOUTUBE
            ),
            QRTypeItem(
                id = "tiktokType",
                title = "TikTok",
                iconRes = R.drawable.ic_tiktok,
                type = CreateType.TIKTOK
            )
        )
    }
}
