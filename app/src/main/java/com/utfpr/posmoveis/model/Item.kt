package com.utfpr.posmoveis.model
data class ItemValue(
    val id: String,
    val value: Item
)

data class Item(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place ?
)

data class Place(
    val lat: Double,
    val long: Double
)

// mapeando o json para a classe
