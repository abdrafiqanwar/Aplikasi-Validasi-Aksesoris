package com.example.validasiaksesoris.di

import com.example.validasiaksesoris.data.retrofit.ApiConfig
import com.example.validasiaksesoris.pref.MainRepository

object Injection {
    fun provideRepository() : MainRepository {
        val apiService = ApiConfig.getApiService()
        return MainRepository.getInstance(apiService)
    }
}