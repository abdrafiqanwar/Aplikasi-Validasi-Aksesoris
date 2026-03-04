package com.example.validasiaksesoris.ui.accessory

import androidx.lifecycle.ViewModel
import com.example.validasiaksesoris.data.model.AccessoryRequest
import com.example.validasiaksesoris.pref.AccessoryRepository

class AccessoryViewModel(private val repository: AccessoryRepository) : ViewModel() {
    fun sendData(request: AccessoryRequest) = repository.sendData(request)
}