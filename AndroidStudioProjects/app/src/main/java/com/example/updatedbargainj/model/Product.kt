package com.example.updatedbargainj.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "Other",
    val sellerId: String = "",
    val buyerId: String = "",
    val soldPrice: Double = 0.0,
    val status: String = "available", // "available", "sold"
    @ServerTimestamp val timestamp: Date? = null
)
