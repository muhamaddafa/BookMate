package com.sumberrejeki.bookmate.models

data class Shelf (
    val id: String? = null,
    val imageUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val userId: String? = null,
    val books: List<String>? = listOf()
)