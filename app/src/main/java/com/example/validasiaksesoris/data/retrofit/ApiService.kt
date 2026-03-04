package com.example.validasiaksesoris.data.retrofit

import com.example.validasiaksesoris.data.model.AccessoryResponse
import com.example.validasiaksesoris.data.model.AccessoryRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("exec")
    suspend fun sendData(
        @Body request: AccessoryRequest
    ): AccessoryResponse
}