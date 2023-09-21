package com.example.weather.data

import com.example.weather.data.forecastModels.Forecast
import com.example.weather.data.models.currentWeather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface apiInterface {
    @GET("weather?")
    suspend fun getCurrentWeather(
        @Query("q") city : String,
        @Query("units") units : String,
        @Query("appid") apiKey : String,

    ): Response<currentWeather>

    @GET("forecast?")
    suspend fun getForecast(
        @Query("q") city: String,
        @Query("units") units : String,
        @Query("appid") apiKey : String,
        ) : Response<Forecast>
}