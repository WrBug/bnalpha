package com.wrbug.bnalpha

import com.wrbug.base.util.createSSLSocketFactory
import com.wrbug.base.util.getEnv
import com.wrbug.base.util.toJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

object PushManager {
    private val client by lazy {
        OkHttpClient.Builder().createSSLSocketFactory().build()
    }

    fun send(title: String, content: String) {
        log(title, content)
        runCatching {
            val sendKey = getEnv("SEND_KEY").takeIf { it.isNotEmpty() } ?: return
            val request = Request.Builder().url(
                "https://api.day.app/$sendKey/${URLEncoder.encode(title)}/${
                    URLEncoder.encode(content)
                }"
            ).build()
            val response = client.newCall(request).execute()
            log(response.body?.string())
        }.onFailure {
            log("PushManager failed", it.message)
        }
    }
}