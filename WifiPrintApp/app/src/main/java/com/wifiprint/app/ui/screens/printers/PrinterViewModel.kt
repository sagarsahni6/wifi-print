package com.wifiprint.app.ui.screens.printers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.PrinterInfo
import com.wifiprint.app.data.repository.PrintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _printers = MutableStateFlow<List<PrinterInfo>>(emptyList())
    val printers: StateFlow<List<PrinterInfo>> = _printers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadPrinters() }

    fun loadPrinters() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getPrinters().fold(
                onSuccess = { _printers.value = it },
                onFailure = { }
            )
            _isLoading.value = false
        }
    }
}
