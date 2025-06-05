package com.wrbug.bnalpha.data.binance

data class QuoteRequest(
    val fromCoinAmount: String? = null,
    val fromToken: String? = null,
    val toBinanceChainId: String? = null,
    val fromBinanceChainId: String? = null,
    val toContractAddress: String? = null,
    val toToken: String? = null,
    val fromContractAddress: String? = null
)