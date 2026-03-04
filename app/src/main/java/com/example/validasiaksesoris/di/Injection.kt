package com.example.validasiaksesoris.di

import com.example.validasiaksesoris.data.retrofit.ApiConfig
import com.example.validasiaksesoris.pref.AccessoryRepository

object Injection {
    fun provideRepository() : AccessoryRepository {
        val apiService = ApiConfig.getApiService()
        return AccessoryRepository.getInstance(apiService)
    }
}