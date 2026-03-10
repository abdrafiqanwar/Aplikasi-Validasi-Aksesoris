package com.example.validasiaksesoris.pref

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.validasiaksesoris.data.model.accessory.AccessoryResponse
import com.example.validasiaksesoris.data.model.accessory.AccessoryRequest
import com.example.validasiaksesoris.data.retrofit.ApiService
import com.example.validasiaksesoris.di.Result
import com.google.gson.Gson
import retrofit2.HttpException

class AccessoryRepository private constructor(
    private var apiService: ApiService
){
    fun sendData(request: AccessoryRequest): LiveData<Result<AccessoryResponse>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.sendData(request)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, AccessoryResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    companion object {
        @Volatile
        private var instance: AccessoryRepository? = null
        fun getInstance(
            apiService: ApiService,
        ): AccessoryRepository =
            instance ?: synchronized(this) {
                instance ?: AccessoryRepository(apiService)
            }.also { instance = it }
    }
}