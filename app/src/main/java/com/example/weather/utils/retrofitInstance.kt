package com.example.weather.utils

import com.example.weather.data.apiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object retrofitInstance {
    val api: apiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(utils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(apiInterface::class.java)
    }
}