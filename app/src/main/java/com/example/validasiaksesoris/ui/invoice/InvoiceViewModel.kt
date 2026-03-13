package com.example.validasiaksesoris.ui.invoice

import androidx.lifecycle.ViewModel
import com.example.validasiaksesoris.pref.MainRepository

class InvoiceViewModel(private val repository: MainRepository) : ViewModel() {
    fun getData() = repository.getData()

    fun getInvoice(frames: String) = repository.getInvoice(frames)
}