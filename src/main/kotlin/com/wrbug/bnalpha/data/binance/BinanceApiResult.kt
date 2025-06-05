package com.wrbug.bnalpha.data.binance

data class BinanceApiResult<T>(
    val code: String = "",
    val message: String? = null,
    val messageDetail: Any? = null,
    val data: T? = null,
    val success: Boolean = false
)