package com.utfpr.posmoveis.service

import com.utfpr.posmoveis.model.Item
import com.utfpr.posmoveis.model.ItemValue
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ApiService {
    @GET("car")
    suspend fun getItems(): List<Item>

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): ItemValue

    @DELETE("car/{id}")
    suspend fun deleteItem(@Path("id") id: String): ItemValue

    @PATCH("car/{id}")
    suspend fun  updateItem(@Path("id") id: String, @Body item: Item): Item
}
