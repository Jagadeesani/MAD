package com.example.updatedbargainj.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.updatedbargainj.model.Offer
import com.example.updatedbargainj.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(
    productId: String,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit
) {
    val offers by viewModel.getOffers(productId).collectAsState(initial = emptyList())
    val products by viewModel.products.collectAsState()
    val product = products.find { it.id == productId }
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No offers yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(offers) { offer ->
                    OfferItem(
                        offer = offer,
                        isSeller = product?.sellerId == currentUser,
                        onAccept = { viewModel.updateOfferStatus(offer.id, productId, "accepted") },
                        onReject = { viewModel.updateOfferStatus(offer.id, productId, "rejected") }
                    )
                }
            }
        }
    }
}

@Composable
fun OfferItem(
    offer: Offer,
    isSeller: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${offer.offerPrice}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From: ${offer.buyerId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                StatusBadge(status = offer.status)
            }
            
            // Only show Accept/Reject buttons if current user is the seller and status is pending
            if (isSeller && offer.status == "pending") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onReject, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "accepted" -> Color.Green
        "rejected" -> Color.Red
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
