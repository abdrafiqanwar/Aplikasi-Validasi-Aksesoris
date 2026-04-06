package com.example.validasiaksesoris.data.model.invoice

import com.google.gson.annotations.SerializedName

data class InvoiceResponse(
    @field:SerializedName("frame_number")
    val frameNumber: String,

    @field:SerializedName("vehicle_model")
    val vehicleModel: String,

    @field:SerializedName("accessories")
    val accessories: List<InvoiceItem>,

    @field:SerializedName("total")
    val total: String,

    @field:SerializedName("createdAt")
    val createdAt: String,
)
