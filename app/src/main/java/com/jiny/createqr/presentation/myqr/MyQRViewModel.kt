package com.jiny.createqr.presentation.myqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jiny.createqr.domain.model.QRItem
import com.jiny.createqr.domain.usecase.DeleteQRItemUseCase
import com.jiny.createqr.domain.usecase.GetAllQRItemsUseCase
import com.jiny.createqr.domain.usecase.UpdateQRItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyQRViewModel @Inject constructor(
    getAllQRItemsUseCase: GetAllQRItemsUseCase,
    private val deleteQRItemUseCase: DeleteQRItemUseCase,
    private val updateQRItemUseCase: UpdateQRItemUseCase
) : ViewModel() {

    val qrItems: StateFlow<List<QRItem>> = getAllQRItemsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteItem(item: QRItem) {
        viewModelScope.launch {
            deleteQRItemUseCase(item)
        }
    }

    fun updateTitle(item: QRItem, newTitle: String) {
        viewModelScope.launch {
            updateQRItemUseCase(item.copy(title = newTitle))
        }
    }
}
