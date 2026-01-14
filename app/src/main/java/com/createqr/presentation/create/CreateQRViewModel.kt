package com.createqr.presentation.create

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.createqr.domain.model.CreateType
import com.createqr.domain.model.LogoStyle
import com.createqr.domain.model.QRItem
import com.createqr.domain.model.QRTypeItem
import com.createqr.domain.usecase.GenerateQRCodeUseCase
import com.createqr.domain.usecase.GetQRTypeListUseCase
import com.createqr.domain.usecase.SaveQRItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateQRViewModel @Inject constructor(
    private val getQRTypeListUseCase: GetQRTypeListUseCase,
    private val generateQRCodeUseCase: GenerateQRCodeUseCase,
    private val saveQRItemUseCase: SaveQRItemUseCase
) : ViewModel() {

    private val _qrTypes = MutableLiveData<List<QRTypeItem>>()
    val qrTypes: LiveData<List<QRTypeItem>> = _qrTypes

    private val _selectedType = MutableLiveData<QRTypeItem?>()
    val selectedType: LiveData<QRTypeItem?> = _selectedType

    private val _generatedQRBitmap = MutableLiveData<Bitmap?>()
    val generatedQRBitmap: LiveData<Bitmap?> = _generatedQRBitmap

    private val _currentQRData = MutableLiveData<String>()
    val currentQRData: LiveData<String> = _currentQRData

    private val _qrColor = MutableLiveData(0xFF000000.toInt())
    val qrColor: LiveData<Int> = _qrColor

    private val _backgroundColor = MutableLiveData(0xFFFFFFFF.toInt())
    val backgroundColor: LiveData<Int> = _backgroundColor

    private val _logo = MutableLiveData<Bitmap?>()
    val logo: LiveData<Bitmap?> = _logo

    private val _logoStyle = MutableLiveData(LogoStyle.SQUARE)
    val logoStyle: LiveData<LogoStyle> = _logoStyle

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    init {
        loadQRTypes()
    }

    private fun loadQRTypes() {
        val types = getQRTypeListUseCase()
        _qrTypes.value = types
        if (types.isNotEmpty()) {
            _selectedType.value = types.first()
        }
    }

    fun selectType(type: QRTypeItem) {
        _selectedType.value = type
        // Reset QR when type changes
        _generatedQRBitmap.value = null
    }

    fun generateQR(data: String) {
        _currentQRData.value = data
        val bitmap = generateQRCodeUseCase(
            data = data,
            qrColor = _qrColor.value ?: 0xFF000000.toInt(),
            backgroundColor = _backgroundColor.value ?: 0xFFFFFFFF.toInt(),
            logo = _logo.value,
            logoStyle = _logoStyle.value ?: LogoStyle.SQUARE
        )
        _generatedQRBitmap.value = bitmap
    }

    fun setQRColor(color: Int) {
        _qrColor.value = color
        regenerateQR()
    }

    fun setBackgroundColor(color: Int) {
        _backgroundColor.value = color
        regenerateQR()
    }

    fun setLogo(bitmap: Bitmap?, style: LogoStyle) {
        _logo.value = bitmap
        _logoStyle.value = style
        regenerateQR()
    }

    private fun regenerateQR() {
        val data = _currentQRData.value
        if (!data.isNullOrEmpty()) {
            generateQR(data)
        }
    }

    fun saveQRToApp(title: String? = null) {
        viewModelScope.launch {
            val data = _currentQRData.value ?: return@launch
            val type = _selectedType.value?.type ?: CreateType.URL

            val qrItem = QRItem(
                title = title ?: data.take(50),
                qrImagePath = null, // Will be saved separately
                qrType = type,
                qrData = data,
                qrColor = String.format("%08X", _qrColor.value),
                backgroundColor = String.format("%08X", _backgroundColor.value),
                logoPath = null,
                logoStyle = _logoStyle.value ?: LogoStyle.SQUARE
            )

            try {
                saveQRItemUseCase(qrItem)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }
}
