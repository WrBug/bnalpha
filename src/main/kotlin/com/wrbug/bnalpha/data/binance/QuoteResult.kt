package com.wrbug.bnalpha.data.binance

data class QuoteResult(
    val extra: String,
    val isMinReceiveTooLow: Boolean,
    val maxInputAmount: String,
    val mevDisableWarning: Boolean,
    val mevProtection: Boolean,
    val minInputAmount: String,
    val minReceivedAmount: String,
    val mode: Mode,
    val priorityMode: String,
    val rate: Rate,
    val toCoinAmount: String
) {
    data class Mode(
        val priorityOnPrice: PriorityOnPrice,
        val priorityOnSuccess: PriorityOnSuccess
    ) {
        data class PriorityOnPrice(
            val networkFee: String,
            val slippage: String
        )

        data class PriorityOnSuccess(
            val networkFee: String,
            val slippage: String
        )
    }

    data class Rate(
        val basicFromToRatio: String,
        val fromToRatio: String,
        val networkFee: String,
        val tradingFee: String
    )
}