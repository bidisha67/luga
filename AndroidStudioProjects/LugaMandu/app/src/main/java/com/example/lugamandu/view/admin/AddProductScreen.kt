package com.example.lugamandu.view.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lugamandu.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, viewModel: ProductViewModel) {
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    // Form State
    var name by remember { mutableStateOf(selectedProduct?.name ?: "") }
    var price by remember { mutableStateOf(selectedProduct?.price?.toString() ?: "") }
    var desc by remember { mutableStateOf(selectedProduct?.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Luga Mandu Admin", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedProduct != null) {
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Product") },
                                text = { Text("Are you sure you want to delete this product?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.deleteProduct(selectedProduct!!.id) { success ->
                                                if (success) {
                                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                                    navController.popBackStack()
                                                }
                                            }
                                            showDeleteDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Delete", color = Color.White)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Image Picker Card
            Card(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (!selectedProduct?.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = selectedProduct?.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Upload Cloth Photo", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            Text("Product Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (NPR)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    if (name.isBlank() || price.isBlank()) {
                        Toast.makeText(context, "Name and Price are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.uploadAndSave(
                        context,
                        imageUri,
                        name,
                        price.toDoubleOrNull() ?: 0.0,
                        desc
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Upload Failed. Check Internet/Cloudinary.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (selectedProduct == null) "Publish to Shop" else "Update Product", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}