package com.example.myapplication.auth

data class NVDevices(
    val name: String,
    val products: List<Product>
)

data class Product(
    val aesKey: String,
    val androidToneStartDelay: Double,
    val bleName: String,
    val bluetoothName: String,
    val category: String,
    val certifiedWithAmazon: Boolean,
    val macAddresses: List<MacAddresse>,
    val modelId: String,
    val name: String,
    val productCapabilities: Int,
    val productId: String,
    val triggerWay: String,
    val vendorId: String
)

data class MacAddresse(
    val leftLimit: String,
    val rightLimit: String
)