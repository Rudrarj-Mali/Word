package com.example.word.model



import android.net.Uri

data class Document(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val images: List<Uri> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)