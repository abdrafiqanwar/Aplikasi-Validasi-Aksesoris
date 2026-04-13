package com.example.validasiaksesoris.data.model.invoice

import com.google.gson.annotations.SerializedName

data class FrameNumber(
    @field:SerializedName("frame_number")
    val frameNumber: String,

    @field:SerializedName("createdAt")
    val createdAt: String,

    var isSelected: Boolean = false
)
