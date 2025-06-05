package com.wrbug.bnalpha

import com.wrbug.base.util.createSSLSocketFactory
import com.wrbug.base.util.fromJson
import com.wrbug.base.util.getEnv
import com.wrbug.base.util.httpProxy
import com.wrbug.bnalpha.data.binance.BinanceApiResult
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {

    private lateinit var retrofit: Retrofit

    fun init(
        token: String,
        cookie: String,
        videoId: String,
        deviceInfo: String,
        tokenAddress: String,
        ua: String,
        uaPlatform: String
    ) {
        retrofit =
            Retrofit.Builder().baseUrl("https://www.binance.com/").addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder().createSSLSocketFactory().addInterceptor(ErrorCodeInterceptor())
                        .addInterceptor(
                            AuthInterceptor(
                                token, deviceInfo, tokenAddress, cookie, videoId, ua, uaPlatform
                            )
                        ).apply {
                            if (getEnv("ENABLE_PROXY") == "1") {
                                val host = getEnv("PROXY_HOST").ifEmpty { "127.0.0.1" }
                                val port = getEnv("PROXY_PORT").ifEmpty { "8888" }.toInt()
                                val username = getEnv("PROXY_USERNAME")
                                val password = getEnv("PROXY_PASSWORD")
                                httpProxy(host, port, username, password)
                                log("使用代理：$host:$port:$username:$password")
                            }
                        }.build()
                ).build()
    }

    fun <T> create(clazz: Class<T>): T {
        return retrofit.create<T>(clazz)
    }


    private class AuthInterceptor(
        private val token: String,
        private val deviceInfo: String,
        private val tokenAddress: String,
        private val cookie: String,
        private val videoId: String,
        private val ua: String,
        private val uaPlatform: String
    ) : Interceptor {
        private val cookieList = cookie.split(";")
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder().addHeader("sec-ch-ua-platform", "\"$uaPlatform\"")
                .addHeader("csrftoken", token).addHeader("lang", "zh-CN").addHeader(
                    "sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\""
                ).addHeader("sec-ch-ua-mobile", "?0").addHeader("fvideo-id", videoId).addHeader(
                    "user-agent", ua
                ).addHeader("clienttype", "web").addHeader(
                    "device-info", deviceInfo
                ).addHeader("sec-fetch-site", "same-origin").addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty").apply {
                    cookieList.forEach {
                        addHeader("Cookie", it)
                    }
                }.addHeader(
                    "referer", "https://www.binance.com/zh-CN/alpha/bsc/$tokenAddress"
                )
            return chain.proceed(request.build())
        }
    }

    private class ErrorCodeInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            val content = response.body?.string()
            val result = content.fromJson<BinanceApiResult<Any>>()
            if (response.code == 401 || result?.code in arrayOf("100001005", "351022")) {
                PushManager.send("状态异常", result?.message.orEmpty())
            }
            return response.newBuilder().body(ResponseBody.create(response.body?.contentType(), content.orEmpty()))
                .build()
        }
    }
}