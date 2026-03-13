package com.example.validasiaksesoris.pref

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.validasiaksesoris.data.model.ErrorResponse
import com.example.validasiaksesoris.data.model.accessory.AccessoryRequest
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.model.invoice.InvoiceResponse
import com.example.validasiaksesoris.data.retrofit.ApiService
import com.example.validasiaksesoris.di.Result
import com.google.gson.Gson
import retrofit2.HttpException

class MainRepository private constructor(
    private var apiService: ApiService
){
    fun getData(): LiveData<Result<List<FrameNumber>>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.getData()

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    fun getInvoice(frames: String): LiveData<Result<List<InvoiceResponse>>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.getInvoice(frames)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    fun sendData(request: AccessoryRequest): LiveData<Result<ErrorResponse>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.sendData(request)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    companion object {
        @Volatile
        private var instance: MainRepository? = null
        fun getInstance(
            apiService: ApiService,
        ): MainRepository =
            instance ?: synchronized(this) {
                instance ?: MainRepository(apiService)
            }.also { instance = it }
    }
}