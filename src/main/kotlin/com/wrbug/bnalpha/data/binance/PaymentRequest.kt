package com.wrbug.bnalpha.data.binance

data class PaymentRequest(
    val extra: String? = null,
    val fromBinanceChainId: String? = null,
    val fromCoinAmount: String? = null,
    val fromToken: String? = null,
    val payMethod: String? = null,
    val priorityMode: String? = null,
    val toBinanceChainId: String? = null,
    val toCoinAmount: String? = null,
    val toContractAddress: String? = null,
    val fromContractAddress: String? = null,
    val toToken: String? = null
)