package com.jojo.terasrumah.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.jojo.terasrumah.data.FoodCategory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:3000/" // MongoDB API server
    // Use "http://localhost:3000/" for physical device on same network
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(FoodCategory::class.java, FoodCategoryDeserializer())
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val foodApiService: FoodApiService = retrofit.create(FoodApiService::class.java)
    val reviewApiService: ReviewApiService = retrofit.create(ReviewApiService::class.java)
    val userApiService: UserApiService = retrofit.create(UserApiService::class.java)
}

class FoodCategoryDeserializer : JsonDeserializer<FoodCategory> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FoodCategory {
        val categoryString = json?.asString ?: "ALL"
        return try {
            FoodCategory.valueOf(categoryString)
        } catch (e: IllegalArgumentException) {
            FoodCategory.ALL
        }
    }
}
