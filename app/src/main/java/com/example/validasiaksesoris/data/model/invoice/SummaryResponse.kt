package com.example.validasiaksesoris.data.model.invoice

import com.google.gson.annotations.SerializedName

data class SummaryResponse(
    @field:SerializedName("item")
    val item: String,

    @field:SerializedName("qty")
    val qty: Int,

    @field:SerializedName("price")
    val price: Int,

    @field:SerializedName("total")
    val total: Int
)
