package com.wrbug.bnalpha

import com.wrbug.base.util.assert
import com.wrbug.base.util.getEnv
import com.wrbug.base.util.gt
import com.wrbug.bnalpha.data.binance.PaymentRequest
import com.wrbug.bnalpha.data.binance.QuoteRequest
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import kotlin.system.exitProcess

val apiService by lazy {
    RetrofitManager.create(BinanceApiService::class.java)
}

fun main() {
    val token = getEnv("CSRF_TOKEN")
    assert("缺少环境变量：TOKEN") { token.isNotEmpty() }
    val cookie = getEnv("COOKIE")
    assert("缺少环境变量：COOKIE") { cookie.isNotEmpty() }
    val videoId = getEnv("F_VIDEO_ID")
    assert("缺少环境变量：F_VIDEO_ID") { videoId.isNotEmpty() }
    val tokenName = getEnv("TOKEN_NAME")
    assert("缺少环境变量：TOKEN_NAME") { tokenName.isNotEmpty() }
    val tokenContractAddress = getEnv("TOKEN_CA")
    assert("缺少环境变量：TOKEN_CA") { tokenContractAddress.isNotEmpty() }
    val chainId = getEnv("CHAIN_ID")
    assert("缺少环境变量：CHAIN_ID") { chainId.isNotEmpty() }
    val deviceInfo = getEnv("DEVICE_INFO")
    assert("缺少环境变量：DEVICE_INFO") { deviceInfo.isNotEmpty() }
    val amount = getEnv("AMOUNT")
    assert("缺少环境变量：AMOUNT") { amount.isNotEmpty() }
    val ua = getEnv("UA")
    assert("缺少环境变量：UA") { ua.isNotEmpty() }
    val uaPlatform = getEnv("UA_PLATFORM")
    assert("缺少环境变量：UA_PLATFORM") { uaPlatform.isNotEmpty() }
    val tradeCount = getEnv("TRADE_COUNT").ifEmpty { "1" }.toInt()
    log("开始校验输入")
    RetrofitManager.init(
        token = token,
        cookie = cookie,
        videoId = videoId,
        deviceInfo = deviceInfo,
        tokenAddress = tokenContractAddress,
        ua = ua,
        uaPlatform = uaPlatform
    )
    runBlocking {
        val result = apiService.getUserProfile()
        val profile = result.data
        if (profile == null) {
            log("token已过期")
            exitProcess(0)
        }
        log("当前账号：" + profile.displayName)
        startTrading(tokenName, tokenContractAddress, chainId, amount, tradeCount)
        log("任务执行完成，30s后查询可用资产")
        sleep(30 * 1000)
        checkHasAvailableBalance(chainId, tokenName, tokenContractAddress)
    }
    exitProcess(0)
}

suspend fun startTrading(
    tokenName: String, tokenContractAddress: String, chainId: String, amount: String, tradeCount: Int
) {
    var i = 1
    var failedCount = 0
    while (i <= tradeCount) {
        if (failedCount >= 10) {
            return
        }
        if (i != 1) {
            log("30s后执行下一次交易...")
            sleep(30 * 1000)
        }
        log("开始第${i}次交易")
        if (!startBuy(tokenName, tokenContractAddress, chainId, amount)) {
            failedCount++
            continue
        }
        val freeAmount = runCatching {
            getAvailableAssets(chainId, tokenContractAddress)
        }.getOrDefault("")
        if (freeAmount.isEmpty()) {
            continue
        }
        i++
        var failCount = 0
        while (true) {
            val success = runCatching {
                startSell(tokenName, tokenContractAddress, chainId, freeAmount)
            }.getOrDefault(false)
            if (success) {
                break
            }
            failCount++
            if (failCount >= 10) {
                log("卖出${tokenName}异常")
                break
            }
            log("10s后重试")
            sleep(10000)
        }
    }
}

suspend fun getAvailableAssets(chainId: String, tokenContractAddress: String): String {
    var count = 0
    while (true) {
        if (count >= 4) {
            return ""
        }
        sleep(10000)
        val assetResult = apiService.getAlphaAssets()
        if (!assetResult.success || assetResult.data == null) {
            log("资产查询失败", assetResult.message)
            count++
            continue
        }
        val asset = assetResult.data.list.find { it.chainId == chainId && it.contractAddress == tokenContractAddress }
        if (asset == null) {
            log("未查到可用资产,等待成交")
            count++
            continue
        }
        log("${asset.name} 可用:${asset.free}")
        return asset.free
    }
}

suspend fun startBuy(
    tokenName: String, tokenContractAddress: String, chainId: String, amount: String
): Boolean {
    log("开始获取{$tokenName}报价")
    val result = apiService.getQuote(
        QuoteRequest(
            fromCoinAmount = amount,
            fromToken = "USDT",
            toBinanceChainId = chainId,
            toContractAddress = tokenContractAddress,
            toToken = tokenName
        )
    )
    val r = result.data
    if (r == null || !result.success) {
        log("获取报价失败:" + result.message)
        sleep(10000)
        return false
    }
    log("汇率：1 USDT = ${r.rate.fromToRatio} $tokenName\n网络费：${r.rate.networkFee}\nminreceive: ${r.minReceivedAmount}")
    log("开始买入 ${tokenName}")
    val r1 = apiService.buy(
        PaymentRequest(
            extra = r.extra,
            fromBinanceChainId = chainId,
            fromCoinAmount = amount,
            fromToken = "USDT",
            payMethod = "FUNDING_AND_SPOT",
            priorityMode = r.priorityMode,
            toBinanceChainId = chainId,
            toCoinAmount = r.toCoinAmount,
            toContractAddress = tokenContractAddress,
            toToken = tokenName
        )
    )
    if (!r1.success || r1.data?.isSuccess != true) {
        log("买入失败", r1.message)
        if (r1.code == "351022") {
            exitProcess(0)
        }
        sleep(10000)
        return false
    }
    log("下单成功,等待成交")
    return true
}

suspend fun startSell(
    tokenName: String, tokenContractAddress: String, chainId: String, freeAmount: String
): Boolean {
    log("开始卖出${tokenName}", "数量：$freeAmount")
    log("开始获取报价")
    val result = apiService.getQuote(
        QuoteRequest(
            fromCoinAmount = freeAmount,
            fromToken = tokenName,
            fromBinanceChainId = chainId,
            fromContractAddress = tokenContractAddress,
            toToken = "USDT"
        )
    )
    val r = result.data
    if (r == null || !result.success) {
        log("获取报价失败:" + result.message)
        return false
    }
    log(
        "汇率：1 ${tokenName} = ${r.rate.fromToRatio} USDT",
        "网络费：${r.rate.networkFee}",
        "minreceive: ${r.minReceivedAmount}"
    )
    log("开始卖出 ${tokenName}")
    val r1 = apiService.sell(
        PaymentRequest(
            extra = r.extra,
            fromBinanceChainId = chainId,
            fromCoinAmount = freeAmount,
            fromToken = tokenName,
            priorityMode = r.priorityMode,
            toBinanceChainId = chainId,
            toCoinAmount = r.toCoinAmount,
            fromContractAddress = tokenContractAddress,
            toToken = "USDT"
        )
    )
    if (!r1.success) {
        log("卖出失败", r1.message)
        return false
    }
    log("下单成功,等待成交")
    return true
}

suspend fun checkHasAvailableBalance(chainId: String, tokenName: String, tokenContractAddress: String) {
    runCatching {
        val assetResult = apiService.getAlphaAssets()
        if (!assetResult.success) {
            PushManager.send(
                "查询${tokenName}资产失败", "请自行前往App查看${tokenName}是否已售出,msg:${assetResult.message}"
            )
            return
        }
        val result =
            assetResult.data?.list?.find { it.chainId == chainId && it.contractAddress == tokenContractAddress }
        if (result?.free.gt(0)) {
            startSell(tokenName, tokenContractAddress, chainId, result?.free.orEmpty())
            PushManager.send("卖出${tokenName}异常", "请自行前往App查看${tokenName}是否已售出")
            return
        }
        log("${tokenName}已全部售出")
    }.getOrElse {
        PushManager.send("查询${tokenName}资产失败", "请自行前往App查看${tokenName}是否已售出")
    }
}