package com.wrbug.bnalpha.data.binance

data class AlphaAsset(
    val amount: String = "",
    val cexAsset: Boolean = false,
    val chainId: String = "",
    val contractAddress: String = "",
    val free: String = "",
    val freeze: String = "",
    val locked: String = "",
    val name: String = "",
    val symbol: String = "",
    val tokenId: String = "",
    val valuation: String = "",
    val withdrawing: String = ""
)