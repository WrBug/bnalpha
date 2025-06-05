package com.wrbug.bnalpha

import com.wrbug.bnalpha.data.binance.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BinanceApiService {

    @GET("/bapi/apex/v2/private/apex/user/current/profile/query")
    suspend fun getUserProfile(): BinanceApiResult<UserProfile>


    @POST("/bapi/defi/v1/private/wallet-direct/swap/cex/get-quote")
    suspend fun getQuote(
        @Body quoteRequest: QuoteRequest
    ): BinanceApiResult<QuoteResult>

    @POST("/bapi/defi/v2/private/wallet-direct/swap/cex/buy/pre/payment")
    suspend fun buy(@Body paymentRequest: PaymentRequest): BinanceApiResult<PaymentResult>


    @POST("/bapi/defi/v2/private/wallet-direct/swap/cex/sell/pre/payment")
    suspend fun sell(@Body paymentRequest: PaymentRequest): BinanceApiResult<PaymentResult>

    @GET("/bapi/defi/v1/private/wallet-direct/cloud-wallet/alpha")
    suspend fun getAlphaAssets(): BinanceApiResult<AlphaAssetsResult>
}