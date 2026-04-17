package com.example.validasiaksesoris.data.model.accessory

import com.google.gson.annotations.SerializedName

data class AccessoryResponse(
    @field:SerializedName("frame_number")
    val frameNumber: String,

    @field:SerializedName("vehicle_model")
    val vehicleModel: String,

    @field:SerializedName("accessories")
    val accessories: List<AccessoryItem>
)