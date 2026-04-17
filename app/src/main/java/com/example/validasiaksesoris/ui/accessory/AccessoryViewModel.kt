package com.example.validasiaksesoris.ui.accessory

import androidx.lifecycle.ViewModel
import com.example.validasiaksesoris.data.model.accessory.AccessoryResponse
import com.example.validasiaksesoris.pref.MainRepository

class AccessoryViewModel(private val repository: MainRepository) : ViewModel() {
    fun getAccessories(sheet: String) = repository.getAccessories(sheet)

    fun sendData(request: AccessoryResponse) = repository.sendData(request)
}