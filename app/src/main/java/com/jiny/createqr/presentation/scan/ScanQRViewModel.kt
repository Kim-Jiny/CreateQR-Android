package com.jiny.createqr.presentation.scan

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiny.createqr.domain.model.CreateType
import com.jiny.createqr.domain.model.QRItem
import com.jiny.createqr.domain.usecase.GenerateQRCodeUseCase
import com.jiny.createqr.domain.usecase.SaveQRItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanQRViewModel @Inject constructor(
    private val saveQRItemUseCase: SaveQRItemUseCase,
    private val generateQRCodeUseCase: GenerateQRCodeUseCase
) : ViewModel() {

    private val _scannedResult = MutableLiveData<String?>()
    val scannedResult: LiveData<String?> = _scannedResult

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    fun onQRScanned(data: String) {
        _scannedResult.value = data
    }

    fun clearResult() {
        _scannedResult.value = null
    }

    fun saveScannedQR(data: String, title: String? = null) {
        viewModelScope.launch {
            val qrType = detectQRType(data)

            val qrItem = QRItem(
                title = title ?: extractTitle(data, qrType),
                qrImagePath = null,
                qrType = qrType,
                qrData = data
            )

            try {
                saveQRItemUseCase(qrItem)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }

    private fun detectQRType(data: String): CreateType {
        return when {
            data.startsWith("BEGIN:VCARD") -> CreateType.CONTACT
            data.startsWith("WIFI:") -> CreateType.WIFI
            data.contains("instagram.com") -> CreateType.INSTAGRAM
            data.contains("youtube.com") || data.contains("youtu.be") -> CreateType.YOUTUBE
            data.contains("tiktok.com") -> CreateType.TIKTOK
            else -> CreateType.URL
        }
    }

    private fun extractTitle(data: String, type: CreateType): String {
        return when (type) {
            CreateType.CONTACT -> {
                val nameMatch = Regex("FN:(.+)").find(data)
                nameMatch?.groupValues?.get(1) ?: data.take(50)
            }
            CreateType.WIFI -> {
                val ssidMatch = Regex("S:([^;]+)").find(data)
                ssidMatch?.groupValues?.get(1) ?: "WiFi"
            }
            CreateType.INSTAGRAM -> {
                extractInstagramUsername(data)?.let { "@$it" } ?: data.take(50)
            }
            CreateType.YOUTUBE -> {
                extractYouTubeChannel(data) ?: data.take(50)
            }
            CreateType.TIKTOK -> {
                extractTikTokUsername(data)?.let { "@$it" } ?: data.take(50)
            }
            else -> data.take(50)
        }
    }

    fun extractInstagramUsername(url: String): String? {
        val regex = Regex("instagram\\.com/([^/?]+)")
        val match = regex.find(url)
        val username = match?.groupValues?.get(1)
        val excluded = listOf("p", "reel", "reels", "stories", "explore", "accounts", "direct")
        return if (username != null && !excluded.contains(username.lowercase())) username else null
    }

    fun extractYouTubeChannel(url: String): String? {
        // @handle format
        val handleRegex = Regex("youtube\\.com/(@[^/?]+)")
        handleRegex.find(url)?.let { return it.groupValues[1] }

        // /channel/xxx format
        val channelRegex = Regex("youtube\\.com/channel/([^/?]+)")
        channelRegex.find(url)?.let { return it.groupValues[1] }

        // /c/xxx format
        val cRegex = Regex("youtube\\.com/c/([^/?]+)")
        cRegex.find(url)?.let { return "@${it.groupValues[1]}" }

        return null
    }

    fun extractTikTokUsername(url: String): String? {
        val regex = Regex("tiktok\\.com/@([^/?]+)")
        return regex.find(url)?.groupValues?.get(1)
    }

    fun isVCard(data: String): Boolean = data.startsWith("BEGIN:VCARD")
    fun isInstagram(data: String): Boolean = data.contains("instagram.com")
    fun isYouTube(data: String): Boolean = data.contains("youtube.com") || data.contains("youtu.be")
    fun isTikTok(data: String): Boolean = data.contains("tiktok.com")
}
