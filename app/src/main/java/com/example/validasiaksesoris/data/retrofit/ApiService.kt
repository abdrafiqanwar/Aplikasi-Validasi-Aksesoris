package com.example.validasiaksesoris.data.retrofit

import com.example.validasiaksesoris.data.model.accessory.AccessoryResponse
import com.example.validasiaksesoris.data.model.ErrorResponse
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.model.invoice.DetailResponse
import com.example.validasiaksesoris.data.model.invoice.SummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("exec")
    suspend fun getAccessories(
        @Query("sheet") sheet: String
    ): List<AccessoryResponse>

    @GET("exec")
    suspend fun getData(): List<FrameNumber>

    @GET("exec")
    suspend fun getSummary(
        @Query("sheet") sheet: String
    ): List<SummaryResponse>

    @GET("exec")
    suspend fun getDetail(
        @Query("frames") frames: String
    ): List<DetailResponse>

    @POST("exec")
    suspend fun sendData(
        @Body request: AccessoryResponse
    ): ErrorResponse
}