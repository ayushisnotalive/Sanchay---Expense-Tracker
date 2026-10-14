package com.camuel.sanchay

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Response model
data class ExchangeRateResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

// API Interface
interface CurrencyApiService {
    @GET("v4/latest/{baseCurrency}")
    suspend fun getLatestRates(@Path("baseCurrency") baseCurrency: String): ExchangeRateResponse
}

// Retrofit singleton with offline resilience
object CurrencyApi {
    private const val BASE_URL = "https://api.exchangerate-api.com/"

    // In-memory cache for fast lookups
    private val rateCache = mutableMapOf<String, Double>()

    // Standard fallback exchange rates for offline travel mode
    private val fallbackRates = mapOf(
        "USD_EUR" to 0.92, "USD_GBP" to 0.79, "USD_INR" to 83.5, "USD_JPY" to 155.0,
        "EUR_USD" to 1.08, "EUR_GBP" to 0.86, "EUR_INR" to 90.5, "EUR_JPY" to 168.0,
        "GBP_USD" to 1.26, "GBP_EUR" to 1.16, "GBP_INR" to 105.0, "GBP_JPY" to 195.0,
        "INR_USD" to 0.012, "INR_EUR" to 0.011, "INR_GBP" to 0.0095, "INR_JPY" to 1.85,
        "JPY_USD" to 0.0064, "JPY_EUR" to 0.0059, "JPY_GBP" to 0.0051, "JPY_INR" to 0.54
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService: CurrencyApiService by lazy {
        retrofit.create(CurrencyApiService::class.java)
    }

    suspend fun getRate(fromCode: String, toCode: String): Double {
        if (fromCode == toCode) return 1.0
        val key = "${fromCode}_$toCode"
        rateCache[key]?.let { return it }

        return try {
            val response = retrofitService.getLatestRates(fromCode)
            val rate = response.rates[toCode] ?: fallbackRates[key] ?: 1.0
            rateCache[key] = rate
            rate
        } catch (e: Exception) {
            fallbackRates[key] ?: 1.0
        }
    }
}
