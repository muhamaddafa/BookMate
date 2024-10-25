package com.sumberrejeki.bookmate.models

import java.util.Date

data class Notes(
    var bookId: String = "",
    var userId: String? = null,
    var text: String? = null,
    var created: Date? = null
)