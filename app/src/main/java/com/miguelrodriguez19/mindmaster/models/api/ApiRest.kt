package com.utad.proyecto_integrador.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiRest {

    lateinit var service: DeeplApi
    const val deeplKey = "55ead75a8637cc700aaf3c232f2a2775"
    const val deeplUrl = "https://api-free.deepl.com/"

    fun initDeepl() {
        val retrofit =
            Retrofit.Builder().baseUrl(deeplUrl).addConverterFactory(GsonConverterFactory.create())
                .build()
        service = retrofit.create(DeeplApi::class.java)
    }
}