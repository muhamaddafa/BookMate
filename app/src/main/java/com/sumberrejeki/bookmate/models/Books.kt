package com.sumberrejeki.bookmate.models

import java.io.Serializable

data class Books(
    var id: String? = null,
    var title: String? = null,
    var author: String? = null,
    var publisher: String? = null,
    var pages: Int? = 0,
    var description: String? = null,
    var userId: String? = null,
    var imageUrl: String? = null
) : Serializable
