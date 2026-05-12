package com.example.updatedbargainj.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.updatedbargainj.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    onShowOffers: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val product = products.find { it.id == productId }
    var showOfferDialog by remember { mutableStateOf(false) }
    var offerPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (product?.sellerId == currentUser) {
                        TextButton(onClick = onShowOffers) {
                            Text("View Offers")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (product == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = product.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    
                    if (product.buyerId == currentUser) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "PURCHASED",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Seller: ${product.sellerId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (product.status == "sold") {
                        Text(
                            text = "Sold for: ₹${product.soldPrice}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(Original: ₹${product.price})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "₹${product.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Description", style = MaterialTheme.typography.titleMedium)
                    Text(text = product.description, style = MaterialTheme.typography.bodyLarge)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (product.status == "sold") {
                        if (product.buyerId == currentUser) {
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Text("You bought this item")
                            }
                        } else {
                            Button(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false
                            ) {
                                Text("Item Sold")
                            }
                        }
                    } else if (product.sellerId == currentUser) {
                        OutlinedButton(
                            onClick = onShowOffers,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Offers")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.buyNow(product.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buy Now")
                            }
                            OutlinedButton(
                                onClick = { showOfferDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Make Offer")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = { showOfferDialog = false },
            title = { Text("Make an Offer") },
            text = {
                OutlinedTextField(
                    value = offerPrice,
                    onValueChange = { offerPrice = it },
                    label = { Text("Offer Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = offerPrice.toDoubleOrNull()
                        if (price != null) {
                            viewModel.makeOffer(productId, price)
                            showOfferDialog = false
                        }
                    }
                ) {
                    Text("Send Offer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
