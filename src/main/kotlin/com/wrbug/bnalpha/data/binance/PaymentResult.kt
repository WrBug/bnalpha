package com.wrbug.bnalpha.data.binance

data class PaymentResult(
    val orderHistory: OrderHistory = OrderHistory(), val payStatus: String = ""
) {

    val isSuccess
        get() = payStatus == "SUCCESS"

    data class OrderHistory(
        val chainGasFeeInUsd: Double = 0.0,
        val dbCreateTime: Long = 0,
        val dbUpdateTime: Long = 0,
        val direction: String = "",
        val feeDetail: FeeDetail = FeeDetail(),
        val fromBinanceChainId: String = "",
        val fromBridgeFee: Double = 0.0,
        val fromContractAddress: String = "",
        val fromToken: String = "",
        val fromTokenAmount: String = "",
        val fromTokenId: String = "",
        val nativeTokenPrice: Double = 0.0,
        val orderId: String = "",
        val slippage: Double = 0.0,
        val source: String = "",
        val status: String = "",
        val toBinanceChainId: String = "",
        val toContractAddress: String = "",
        val toToken: String = "",
        val toTokenAmount: String = "",
        val toTokenId: String = "",
        val uniQuoteId: String = "",
        val vendorFromCoinAmount: Double = 0.0
    ) {
        data class FeeDetail(
            val decimals: Any = Any(),
            val defaultRatePercent: Double = 0.0,
            val direction: String = "",
            val id: Int = 0,
            val rateCoinAmount: Int = 0,
            val rateFiatValue: Double = 0.0,
            val ratePercent: Double = 0.0
        )
    }
}