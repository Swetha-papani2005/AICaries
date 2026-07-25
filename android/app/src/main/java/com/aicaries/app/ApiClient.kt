package com.aicaries.app

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {

    // !! CHANGE THIS to your computer's IP address !!
    // Example: "http://192.168.1.5/aicaries/api/"
    // To find IP: open Command Prompt → type ipconfig → look for IPv4 Address
    const val BASE_URL = "https://app-17b106e1-724c-43db-bcc5-196c5d9e4f65.cleverapps.io/api/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun post(endpoint: String, params: JSONObject, callback: (JSONObject?) -> Unit) {
        val body = params.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(BASE_URL + endpoint)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        callback(JSONObject(responseBody))
                    } else {
                        callback(null)
                    }
                } catch (e: Exception) {
                    callback(null)
                }
            }
        })
    }
}