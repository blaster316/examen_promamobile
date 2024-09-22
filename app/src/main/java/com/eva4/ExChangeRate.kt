package com.eva4

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class RateResponse (
    val version: String,
    val autor: String,
    val fecha: String,
    val dolar_intercambio: Bitcoin,
)

data class Bitcoin (
    val valor: Double
)


interface ExchangeRateApi {
    @GET("api")
    suspend fun getExchangeRate(): RateResponse
}

object RetrofitInstance {
    val api: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://mindicador.cl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}