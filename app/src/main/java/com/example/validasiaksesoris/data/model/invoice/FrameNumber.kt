package com.example.validasiaksesoris.data.model.invoice

import com.google.gson.annotations.SerializedName

data class FrameNumber(
    @field:SerializedName("frame_number")
    val frameNumber: String,

    @field:SerializedName("vehicle_model")
    val vehicleModel: String,

    var isSelected: Boolean = false
)
