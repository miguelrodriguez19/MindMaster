package com.utad.proyecto_integrador.api

import android.view.translation.TranslationResponse
import retrofit2.Call
import retrofit2.http.*

interface DeeplApi {

    @FormUrlEncoded
    @POST("v2/translate")
    fun translate(
        @Header("Authorization") authKey: String = ApiRest.deeplKey,
        @Field("text") text: String,
        @Field("target_lang") targetLang: String
    ): Call<TranslationResponse>
}
