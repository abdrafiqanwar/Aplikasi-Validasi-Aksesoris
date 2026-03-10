package com.example.validasiaksesoris.ui.accessory

import androidx.lifecycle.ViewModel
import com.example.validasiaksesoris.data.model.accessory.AccessoryRequest
import com.example.validasiaksesoris.pref.MainRepository

class AccessoryViewModel(private val repository: MainRepository) : ViewModel() {
    fun sendData(request: AccessoryRequest) = repository.sendData(request)
}