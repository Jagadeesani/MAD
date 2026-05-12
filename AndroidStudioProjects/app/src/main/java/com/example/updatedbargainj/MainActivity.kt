package com.example.updatedbargainj

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.updatedbargainj.ui.screens.AddProductScreen
import com.example.updatedbargainj.ui.screens.OffersScreen
import com.example.updatedbargainj.ui.screens.ProductDetailScreen
import com.example.updatedbargainj.ui.screens.ProductListScreen
import com.example.updatedbargainj.ui.theme.BargainXTheme
import com.example.updatedbargainj.viewmodel.ProductViewModel
import com.example.updatedbargainj.viewmodel.UiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BargainXTheme {
                val navController = rememberNavController()
                val viewModel: ProductViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(uiState) {
                    when (val state = uiState) {
                        is UiState.Success -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                        is UiState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        else -> {}
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "product_list") {
                        composable("product_list") {
                            ProductListScreen(
                                viewModel = viewModel,
                                onAddProductClick = { navController.navigate("add_product") },
                                onProductClick = { productId -> navController.navigate("product_detail/$productId") }
                            )
                        }
                        composable("add_product") {
                            AddProductScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("product_detail/{productId}") { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            ProductDetailScreen(
                                productId = productId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onShowOffers = { navController.navigate("offers/$productId") }
                            )
                        }
                        composable("offers/{productId}") { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            OffersScreen(
                                productId = productId,
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
