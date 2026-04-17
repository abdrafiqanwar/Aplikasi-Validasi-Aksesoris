package com.example.validasiaksesoris.pref

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.validasiaksesoris.data.model.ErrorResponse
import com.example.validasiaksesoris.data.model.accessory.AccessoryResponse
import com.example.validasiaksesoris.data.model.invoice.FrameNumber
import com.example.validasiaksesoris.data.model.invoice.DetailResponse
import com.example.validasiaksesoris.data.model.invoice.SummaryResponse
import com.example.validasiaksesoris.data.retrofit.ApiService
import com.example.validasiaksesoris.di.Result
import com.google.gson.Gson
import retrofit2.HttpException

class MainRepository private constructor(
    private var apiService: ApiService
){
    fun getAccessories(sheet: String): LiveData<Result<List<AccessoryResponse>>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.getAccessories(sheet)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }
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

    fun getSummary(sheet: String): LiveData<Result<List<SummaryResponse>>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.getSummary(sheet)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    fun getDetail(frames: String): LiveData<Result<List<DetailResponse>>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.getDetail(frames)

            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            emit(Result.Error(errorMessage))
        }
    }

    fun sendData(request: AccessoryResponse): LiveData<Result<ErrorResponse>> = liveData {
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