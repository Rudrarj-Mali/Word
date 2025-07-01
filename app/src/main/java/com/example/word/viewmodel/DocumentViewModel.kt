package com.example.word.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.word.model.Document
import com.example.word.model.DocumentBlock
import com.example.word.model.DocumentBlock.TextBlock
import com.example.word.model.DocumentBlock.ImageBlock
import com.example.word.model.DocumentSerializable
import com.example.word.model.DocumentBlockSerializable
import com.example.word.model.toSerializable
import com.example.word.model.toDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import android.content.Context
import java.io.OutputStream
import org.apache.poi.xwpf.usermodel.XWPFDocument
import android.graphics.BitmapFactory
import java.io.InputStream
import org.apache.poi.util.Units

class DocumentViewModel : ViewModel() {
    var currentDocument by mutableStateOf(Document())
        private set

    // Update text in a text block
    fun updateTextBlock(index: Int, newText: String) {
        val blocks = currentDocument.blocks.toMutableList()
        val block = blocks[index]
        if (block is TextBlock) {
            blocks[index] = block.copy(text = newText)
            currentDocument = currentDocument.copy(blocks = blocks, lastModified = System.currentTimeMillis())
        }
    }

    // Update font in a text block
    fun updateFontBlock(index: Int, newFont: String) {
        val blocks = currentDocument.blocks.toMutableList()
        val block = blocks[index]
        if (block is TextBlock) {
            blocks[index] = block.copy(font = newFont)
            currentDocument = currentDocument.copy(blocks = blocks, lastModified = System.currentTimeMillis())
        }
    }

    // Insert an image block after the given index
    fun insertImageBlock(afterIndex: Int, uri: Uri) {
        val blocks = currentDocument.blocks.toMutableList()
        blocks.add(afterIndex + 1, ImageBlock(uri))
        currentDocument = currentDocument.copy(blocks = blocks, lastModified = System.currentTimeMillis())
    }

    // Insert a new text block after the given index
    fun insertTextBlock(afterIndex: Int) {
        val blocks = currentDocument.blocks.toMutableList()
        blocks.add(afterIndex + 1, TextBlock())
        currentDocument = currentDocument.copy(blocks = blocks, lastModified = System.currentTimeMillis())
    }

    // Remove a block
    fun removeBlock(index: Int) {
        val blocks = currentDocument.blocks.toMutableList()
        if (blocks.size > 1) { // Always keep at least one block
            blocks.removeAt(index)
            currentDocument = currentDocument.copy(blocks = blocks, lastModified = System.currentTimeMillis())
        }
    }

    // Update title
    fun updateTitle(newTitle: String) {
        currentDocument = currentDocument.copy(title = newTitle, lastModified = System.currentTimeMillis())
    }

    fun saveDocument(context: Context, filename: String = "document.json") {
        val json = Json.encodeToString(currentDocument.toSerializable())
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun loadDocument(context: Context, filename: String = "document.json") {
        val json = context.openFileInput(filename).bufferedReader().use { it.readText() }
        val loaded = Json.decodeFromString<DocumentSerializable>(json).toDocument()
        currentDocument = loaded
    }

    fun exportDocumentAsDocx(context: Context, uri: Uri) {
        try {
            val doc = XWPFDocument()
            for (block in currentDocument.blocks) {
                if (block is TextBlock) {
                    val para = doc.createParagraph()
                    val run = para.createRun()
                    run.setText(block.text)
                } else if (block is ImageBlock) {
                    try {
                        val imageStream: InputStream? = context.contentResolver.openInputStream(block.uri)
                        if (imageStream != null) {
                            val bytes = imageStream.readBytes()
                            imageStream.close()
                            val pictureType = when (block.uri.toString().lowercase()) {
                                in listOf(".jpg", ".jpeg") -> XWPFDocument.PICTURE_TYPE_JPEG
                                in listOf(".png") -> XWPFDocument.PICTURE_TYPE_PNG
                                in listOf(".gif") -> XWPFDocument.PICTURE_TYPE_GIF
                                else -> XWPFDocument.PICTURE_TYPE_PNG
                            }
                            val para = doc.createParagraph()
                            val run = para.createRun()
                            // Try to get image dimensions
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                            val width = if (options.outWidth > 0) options.outWidth else 400
                            val height = if (options.outHeight > 0) options.outHeight else 300
                            run.addPicture(bytes.inputStream(), pictureType, "image", Units.toEMU(width.toDouble()), Units.toEMU(height.toDouble()))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Optionally, add a placeholder or note in the document
                        val para = doc.createParagraph()
                        val run = para.createRun()
                        run.setText("[Image could not be loaded]")
                    }
                }
            }
            context.contentResolver.openOutputStream(uri)?.use { outStream ->
                doc.write(outStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
