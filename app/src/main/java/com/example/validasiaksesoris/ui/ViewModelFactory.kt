package com.example.validasiaksesoris.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.validasiaksesoris.di.Injection
import com.example.validasiaksesoris.pref.MainRepository
import com.example.validasiaksesoris.ui.accessory.AccessoryViewModel
import com.example.validasiaksesoris.ui.invoice.InvoiceViewModel

class ViewModelFactory(
    private val repository: MainRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AccessoryViewModel::class.java) -> {
                AccessoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> {
                InvoiceViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Uknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        @JvmStatic
        fun getInstance(): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(Injection.provideRepository())
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}