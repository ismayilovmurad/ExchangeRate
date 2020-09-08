package com.martiandeveloper.exchangerate.service

import com.martiandeveloper.exchangerate.model.ExchangeRate
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeApi {

    @GET("rates.php")
    suspend fun getExchanges(@Query("base") base: String): Response<List<ExchangeRate>>
}
