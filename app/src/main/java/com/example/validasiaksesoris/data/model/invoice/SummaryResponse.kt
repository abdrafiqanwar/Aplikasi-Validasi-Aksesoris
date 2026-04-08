package com.example.validasiaksesoris.data.model.invoice

import com.google.gson.annotations.SerializedName

data class SummaryResponse(
    @field:SerializedName("item")
    val item: String,

    @field:SerializedName("qty")
    val qty: Int,

    @field:SerializedName("harga_satuan")
    val harga_satuan: Int,

    @field:SerializedName("jumlah")
    val jumlah: Int
)
