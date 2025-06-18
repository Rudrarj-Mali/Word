package com.example.word.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.word.model.Document

class DocumentViewModel : ViewModel() {
    var currentDocument by mutableStateOf(Document())
        private set

    fun updateContent(newContent: String) {
        currentDocument = currentDocument.copy(
            content = newContent,
            lastModified = System.currentTimeMillis()
        )
    }

    fun addImage(imageUri: Uri) {
        currentDocument = currentDocument.copy(
            images = currentDocument.images + imageUri,
            lastModified = System.currentTimeMillis()
        )
    }

    fun updateTitle(newTitle: String) {
        currentDocument = currentDocument.copy(
            title = newTitle,
            lastModified = System.currentTimeMillis()
        )
    }
}
