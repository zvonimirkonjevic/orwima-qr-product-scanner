package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.ProductViewModel
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.OrwimaqrproductscannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<ProductViewModel>()

        setContent {
            NavigationController(viewModel)
        }
    }
}