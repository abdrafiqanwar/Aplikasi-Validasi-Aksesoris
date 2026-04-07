package com.example.validasiaksesoris.ui.invoice

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.validasiaksesoris.data.model.invoice.SummaryResponse
import com.example.validasiaksesoris.di.Result
import com.example.validasiaksesoris.pref.MainRepository

class InvoiceViewModel(private val repository: MainRepository) : ViewModel() {
    fun getData() = repository.getData()

    fun getSummary(sheet: String) = repository.getSummary(sheet)

    fun getDetail(frames: String) = repository.getDetail(frames)
}