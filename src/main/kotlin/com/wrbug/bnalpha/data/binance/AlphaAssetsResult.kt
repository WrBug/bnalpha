package com.wrbug.bnalpha.data.binance

data class AlphaAssetsResult(
    val totalValuation: String = "", val list: List<AlphaAsset> = emptyList()
)
