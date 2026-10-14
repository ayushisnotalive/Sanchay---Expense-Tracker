package com.camuel.sanchay.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class NetworkApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class NetworkExpenseDto(
    val id: Long? = null,
    val clientUuid: String,
    val amount: String,
    val category: String,
    val date: String,
    val paymentMethod: String?,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val walletId: Long?,
    val tags: List<String>?,
    val deleted: Boolean
)

data class NetworkPushRequest(
    val expenses: List<NetworkExpenseDto>
)

data class NetworkPullResponse(
    val serverTimestamp: String?,
    val expenses: List<NetworkExpenseDto>
)

interface SyncApiService {

    @POST("api/v1/sync/push")
    suspend fun pushDelta(
        @Header("Authorization") token: String,
        @Body request: NetworkPushRequest
    ): NetworkApiResponse<String>

    @GET("api/v1/sync/pull")
    suspend fun pullDelta(
        @Header("Authorization") token: String,
        @Query("since") since: String?
    ): NetworkApiResponse<NetworkPullResponse>

    @POST("api/v1/ai/advice")
    suspend fun getAiAdvice(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): NetworkApiResponse<String>
}

object SanchayApiClient {

    // Configurable backend URL. Defaults to 10.0.2.2:8080 (Android Emulator -> Host machine)
    private var baseUrl: String = "http://10.0.2.2:8080/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var syncApiService: SyncApiService = retrofit.create(SyncApiService::class.java)
        private set

    fun updateBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = formatted
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        syncApiService = retrofit.create(SyncApiService::class.java)
    }
}
