package com.sumberrejeki.bookmate.models

data class Books(
    var title: String? = null,
    var author: String? = null,
    var publisher: String? = null,
    var pages: Int? = 0,
    var description: String? = null,
    var userId: String? = null,
    var imageUrl: String? = null
)
