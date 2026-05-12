package com.example.updatedbargainj.repository

import com.example.updatedbargainj.model.Offer
import com.example.updatedbargainj.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BargainRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val subscription = firestore.collection("products")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Product::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addProduct(product: Product): Result<Unit> = try {
        firestore.collection("products").add(product).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getOffers(productId: String): Flow<List<Offer>> = callbackFlow {
        val subscription = firestore.collection("offers")
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Offer::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun makeOffer(offer: Offer): Result<Unit> = try {
        firestore.collection("offers").add(offer).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun buyNow(productId: String, buyerId: String, price: Double): Result<Unit> = try {
        firestore.collection("products").document(productId)
            .update(
                mapOf(
                    "status" to "sold",
                    "buyerId" to buyerId,
                    "soldPrice" to price
                )
            ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateOfferStatus(
        offerId: String,
        productId: String,
        status: String,
        buyerId: String = "",
        soldPrice: Double = 0.0
    ): Result<Unit> = try {
        firestore.collection("offers").document(offerId).update("status", status).await()
        if (status == "accepted") {
            firestore.collection("products").document(productId).update(
                mapOf(
                    "status" to "sold",
                    "buyerId" to buyerId,
                    "soldPrice" to soldPrice
                )
            ).await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getProduct(productId: String): Product? {
        return firestore.collection("products").document(productId).get().await().toObject(Product::class.java)
    }
}
