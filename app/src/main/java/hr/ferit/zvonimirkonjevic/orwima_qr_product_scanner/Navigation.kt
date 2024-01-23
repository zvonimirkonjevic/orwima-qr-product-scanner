package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.ProductViewModel
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.HistoryScreen
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.QrScannerScreen

object Routes {
    const val SCREEN_QR_SCANNER = "qrScanner"
    const val SCREEN_SCANNED_HISTORY = "scannedHistory"
}

@Composable
fun NavigationController(
    viewModel: ProductViewModel
) {
    val navController = rememberNavController()
    NavHost(navController, Routes.SCREEN_QR_SCANNER) {
        composable(Routes.SCREEN_QR_SCANNER) {
            QrScannerScreen(
                navController,
                viewModel
            )
        }
        composable(Routes.SCREEN_SCANNED_HISTORY){
            HistoryScreen(
                navController,
                viewModel
            )
        }
    }
}