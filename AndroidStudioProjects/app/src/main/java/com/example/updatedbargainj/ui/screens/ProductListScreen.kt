package com.example.updatedbargainj.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.updatedbargainj.model.Product
import com.example.updatedbargainj.viewmodel.ProductViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onAddProductClick: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("BargainX") },
                    actions = {
                        TextButton(onClick = { viewModel.switchUser() }) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(currentUser)
                        }
                    }
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                CategorySelector(
                    categories = viewModel.categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategory(it) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProductClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No products found matching your search.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        currentUser = currentUser,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search products...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) }
            )
        }
    }
}

@Composable
fun ProductItem(product: Product, currentUser: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (product.buyerId == currentUser) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = product.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            
            val priceColor = if (product.status == "sold") Color.Red else MaterialTheme.colorScheme.primary
            val priceText = if (product.status == "sold") "₹${product.soldPrice}" else "₹${product.price}"
            
            Text(
                text = priceText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )

            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = getTimeAgo(product.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            if (product.buyerId == currentUser) {
                Text(
                    text = "You bought this!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (product.status == "sold") {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.Red.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "SOLD",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

fun getTimeAgo(date: Date?): String {
    if (date == null) return "Just now"
    return DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
}
