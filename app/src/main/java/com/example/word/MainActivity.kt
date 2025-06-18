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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.word.ui.theme.WordTheme // ✅ Corrected import
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.word.viewmodel.DocumentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // ✅ Corrected for Material 3
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
    var documentText by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        CenterAlignedTopAppBar(
            title = { Text("Word App") },
            actions = {
                IconButton(onClick = { /* TODO: Add save functionality */ }) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        )

        // Document Title
        OutlinedTextField(
            value = viewModel.currentDocument.title,
            onValueChange = { viewModel.updateTitle(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Document Title") }
        )

        // Document Content
        OutlinedTextField(
            value = viewModel.currentDocument.content,
            onValueChange = { viewModel.updateContent(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Start typing your document...") }
        )

        // Images Preview
        if (viewModel.currentDocument.images.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 8.dp)
            ) {
                items(viewModel.currentDocument.images) { imageUri ->
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Document image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    )
                }
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Add Image")
            }
            Button(onClick = { /* TODO: Add formatting */ }) {
                Text("Format")
            }
        }
    }
}
