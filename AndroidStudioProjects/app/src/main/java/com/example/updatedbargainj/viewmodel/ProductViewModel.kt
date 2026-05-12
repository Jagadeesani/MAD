package com.example.updatedbargainj.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.updatedbargainj.model.Offer
import com.example.updatedbargainj.model.Product
import com.example.updatedbargainj.repository.BargainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = BargainRepository()

    // Simulation Users
    val user1 = "User_A (Seller)"
    val user2 = "User_B (Buyer)"

    private val _currentUser = MutableStateFlow(user1)
    val currentUser: StateFlow<String> = _currentUser

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _allProducts = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = combine(_allProducts, _searchQuery, _selectedCategory) { products, query, category ->
        products.filter { product ->
            val matchesQuery = product.title.contains(query, ignoreCase = true) || 
                               product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    val categories = listOf("All", "Electronics", "Furniture", "Fashion", "Vehicles", "Other")

    fun switchUser() {
        _currentUser.value = if (_currentUser.value == user1) user2 else user1
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addProduct(title: String, description: String, price: Double, category: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val product = Product(
                title = title,
                description = description,
                price = price,
                category = category,
                sellerId = _currentUser.value
            )
            val result = repository.addProduct(product)
            _uiState.value = if (result.isSuccess) UiState.Success("Product posted successfully")
            else UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun getOffers(productId: String): Flow<List<Offer>> {
        return repository.getOffers(productId)
    }

    fun makeOffer(productId: String, offerPrice: Double) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val offer = Offer(
                productId = productId,
                buyerId = _currentUser.value,
                offerPrice = offerPrice
            )
            val result = repository.makeOffer(offer)
            _uiState.value = if (result.isSuccess) UiState.Success("Offer sent")
            else UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun buyNow(productId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val product = _allProducts.value.find { it.id == productId }
            val result = repository.buyNow(productId, _currentUser.value, product?.price ?: 0.0)
            _uiState.value = if (result.isSuccess) UiState.Success("Item purchased!")
            else UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun updateOfferStatus(offerId: String, productId: String, status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val offerList = repository.getOffers(productId).first()
            val currentOffer = offerList.find { it.id == offerId }
            if (currentOffer != null) {
                val result = repository.updateOfferStatus(
                    offerId,
                    productId,
                    status,
                    currentOffer.buyerId,
                    currentOffer.offerPrice
                )
                _uiState.value = if (result.isSuccess) UiState.Success("Offer $status")
                else UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}
