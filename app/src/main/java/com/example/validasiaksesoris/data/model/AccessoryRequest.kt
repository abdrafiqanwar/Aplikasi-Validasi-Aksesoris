package com.example.validasiaksesoris.data.model

import com.google.gson.annotations.SerializedName

data class AccessoryRequest(
    @field:SerializedName("frame_number")
    val frameNumber: String,

    @field:SerializedName("vehicle_model")
    val vehicleModel: String,

    @field:SerializedName("accessories")
    val accessories: Map<String, Boolean>
)