package com.example.updatedbargainj.model

import com.google.firebase.firestore.DocumentId

data class Offer(
    @DocumentId val id: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val offerPrice: Double = 0.0,
    val status: String = "pending" // "pending", "accepted", "rejected"
)
