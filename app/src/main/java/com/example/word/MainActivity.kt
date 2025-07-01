@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.word

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.word.ui.theme.WordTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.word.model.Fonts
import com.example.word.viewmodel.DocumentViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Divider
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Slider
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DocumentEditor()
                }
            }
        }
    }
}

@Composable
fun DocumentEditor(viewModel: DocumentViewModel = viewModel()) {
    val context = LocalContext.current
    var showFontDialogForBlock by remember { mutableStateOf<Int?>(null) }
    var selectedImageIdx by remember { mutableStateOf<Int?>(null) }
    var imageRotation by remember { mutableStateOf(0f) }
    var imageScale by remember { mutableStateOf(1f) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Always append image and a new text block at the end
        val insertIdx = viewModel.currentDocument.blocks.size - 1
        if (uri != null) {
            viewModel.insertImageBlock(insertIdx, uri)
            viewModel.insertTextBlock(insertIdx + 1)
        }
    }
    val docxSaveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportDocumentAsDocx(context, uri)
            Toast.makeText(context, "Document saved as DOCX", Toast.LENGTH_SHORT).show()
        }
    }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E5E5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = viewModel.currentDocument.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Document Title") },
                    textStyle = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(12.dp))
                Button(onClick = { viewModel.saveDocument(context) }) {
                    Text("Save")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val title = viewModel.currentDocument.title.ifBlank { "Untitled" }
                    docxSaveLauncher.launch("$title.docx")
                }) {
                    Text("Save as DOCX")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { viewModel.loadDocument(context) }) {
                    Text("Load")
                }
            }
            // The "page"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .verticalScroll(scrollState)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    viewModel.currentDocument.blocks.forEachIndexed { idx, block ->
                        when (block) {
                            is com.example.word.model.DocumentBlock.TextBlock -> {
                                OutlinedTextField(
                                    value = block.text,
                                    onValueChange = { viewModel.updateTextBlock(idx, it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Type here...") },
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = Fonts.fontMap[block.font] ?: FontFamily.Default,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { showFontDialogForBlock = idx }) {
                                            Icon(Icons.Default.FormatSize, contentDescription = "Change Font")
                                        }
                                    }
                                )
                            }
                            is com.example.word.model.DocumentBlock.ImageBlock -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = block.uri,
                                        contentDescription = "Document image",
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF5F5F5))
                                            .clickable {
                                                selectedImageIdx = idx
                                                imageRotation = 0f
                                                imageScale = 1f
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Add Image Button
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .widthIn(max = 300.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = "Add Image")
                Spacer(Modifier.width(8.dp))
                Text("Add Image")
            }
        }
        // Font selection dialog for a block
        if (showFontDialogForBlock != null) {
            AlertDialog(
                onDismissRequest = { showFontDialogForBlock = null },
                title = { Text("Select Font") },
                text = {
                    Column {
                        Fonts.available.forEach { font ->
                            TextButton(
                                onClick = {
                                    viewModel.updateFontBlock(showFontDialogForBlock!!, font)
                                    showFontDialogForBlock = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(font)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showFontDialogForBlock = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        // Image Edit Dialog
        if (selectedImageIdx != null) {
            val block = viewModel.currentDocument.blocks[selectedImageIdx!!]
            if (block is com.example.word.model.DocumentBlock.ImageBlock) {
                AlertDialog(
                    onDismissRequest = { selectedImageIdx = null },
                    title = { Text("Edit Image") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = block.uri,
                                contentDescription = "Edit image",
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(16f / 9f)
                                    .graphicsLayer(
                                        rotationZ = imageRotation,
                                        scaleX = imageScale,
                                        scaleY = imageScale
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5F5F5)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { imageRotation -= 90f }) {
                                    Icon(Icons.Default.RotateLeft, contentDescription = "Rotate Left")
                                }
                                IconButton(onClick = { imageRotation += 90f }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate Right")
                                }
                                IconButton(onClick = { imageScale = (imageScale + 0.1f).coerceAtMost(2f) }) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                                }
                                IconButton(onClick = { imageScale = (imageScale - 0.1f).coerceAtLeast(0.5f) }) {
                                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedImageIdx = null }) {
                            Text("Done")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedImageIdx = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
