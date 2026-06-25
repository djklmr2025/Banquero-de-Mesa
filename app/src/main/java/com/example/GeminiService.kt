package com.example

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API Request Data Classes (Moshi Compatible) ---

data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiResponseFormatText(
    @Json(name = "mimeType") val mimeType: String
)

data class GeminiResponseFormat(
    @Json(name = "text") val text: GeminiResponseFormatText? = null
)

data class GeminiGenerationConfig(
    @Json(name = "responseFormat") val responseFormat: GeminiResponseFormat? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

// --- Gemini API Response Data Classes (Moshi Compatible) ---

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

// --- Structured AI Transaction Schema ---

data class AiBankTransaction(
    @Json(name = "valid") val valid: Boolean,
    @Json(name = "fromPlayerId") val fromPlayerId: String?, // "banco" or player ID
    @Json(name = "toPlayerId") val toPlayerId: String?,   // "banco" or player ID
    @Json(name = "amount") val amount: Long,
    @Json(name = "message") val message: String,
    @Json(name = "reason") val reason: String
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val jsonParser: Moshi = moshi
}
