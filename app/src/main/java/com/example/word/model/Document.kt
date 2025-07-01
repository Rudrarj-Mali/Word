package com.example.word.model

import android.net.Uri
import kotlinx.serialization.Serializable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.word.R

@Serializable
sealed class DocumentBlockSerializable {
    @Serializable
    data class TextBlock(
        val text: String = "",
        val font: String = "Default"
    ) : DocumentBlockSerializable()
    @Serializable
    data class ImageBlock(
        val uri: String
    ) : DocumentBlockSerializable()
}

sealed class DocumentBlock {
    data class TextBlock(
        var text: String = "",
        var font: String = "Default"
    ) : DocumentBlock()
    data class ImageBlock(
        val uri: Uri
    ) : DocumentBlock()
}

@Serializable
data class DocumentSerializable(
    val id: String = "",
    val title: String = "",
    val blocks: List<DocumentBlockSerializable> = listOf(DocumentBlockSerializable.TextBlock()),
    val lastModified: Long = System.currentTimeMillis()
)

data class Document(
    val id: String = "",
    val title: String = "",
    val blocks: List<DocumentBlock> = listOf(DocumentBlock.TextBlock()),
    val lastModified: Long = System.currentTimeMillis()
)

fun Document.toSerializable(): DocumentSerializable = DocumentSerializable(
    id = id,
    title = title,
    blocks = blocks.map {
        when (it) {
            is DocumentBlock.TextBlock -> DocumentBlockSerializable.TextBlock(it.text, it.font)
            is DocumentBlock.ImageBlock -> DocumentBlockSerializable.ImageBlock(it.uri.toString())
        }
    },
    lastModified = lastModified
)

fun DocumentSerializable.toDocument(): Document = Document(
    id = id,
    title = title,
    blocks = blocks.map {
        when (it) {
            is DocumentBlockSerializable.TextBlock -> DocumentBlock.TextBlock(it.text, it.font)
            is DocumentBlockSerializable.ImageBlock -> DocumentBlock.ImageBlock(Uri.parse(it.uri))
        }
    },
    lastModified = lastModified
)

object Fonts {
    val available = listOf(
        "Default",
        "Roboto",
        "Montserrat",
        "OpenSans",
        "Lato",
        "PlayfairDisplay",
        "Times New Roman",
        "Arial",
        "Courier New",
        "Verdana",
        "Georgia"
    )

    val fontMap: Map<String, FontFamily> = mapOf(
        "Default" to FontFamily.Default,
        "Times New Roman" to FontFamily(Font(R.font.times_new_roman)),
        "Arial" to FontFamily(Font(R.font.arial)),
        "Courier New" to FontFamily(Font(R.font.courier_new)),
        "Verdana" to FontFamily(Font(R.font.verdana)),
        "Georgia" to FontFamily(Font(R.font.georgia))
        // Add more as needed
    )
}