package com.example.validasiaksesoris.data.retrofit

import com.example.validasiaksesoris.data.model.accessory.AccessoryRequest
import com.example.validasiaksesoris.data.model.ErrorResponse
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("exec")
    suspend fun getData(): List<FrameNumber>

    @POST("exec")
    suspend fun sendData(
        @Body request: AccessoryRequest
    ): ErrorResponse
}