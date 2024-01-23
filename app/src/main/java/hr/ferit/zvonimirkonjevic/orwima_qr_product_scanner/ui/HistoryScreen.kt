package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.Routes
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.Product
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.ProductViewModel
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.cardColor
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.primaryColor
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.textColor

@Composable
fun HistoryScreen(
    navigation: NavController,
    viewModel: ProductViewModel
) {

    BackHandler {
        navigation.navigate(Routes.SCREEN_QR_SCANNER)
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier.background(primaryColor)
        ) {
            HistoryScreenHeader(viewModel)
            HistoryScreenProductList(viewModel)
        }
        HistoryScreenNavigationBar(navigation)
    }
}

@Composable
fun HistoryScreenHeader(
    viewModel: ProductViewModel
) {
    var asc = false;
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 70.dp, bottom = 15.dp, start = 25.dp, end = 25.dp)
    ) {
        Text(
            text = "History",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 1.sp
        )
        Button(
            onClick = {
                asc = !asc
                viewModel.sortByPrice(asc)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = textColor,
            ),
        ) {
            Icon(Icons.Rounded.Sort, null)
        }
    }
}

@Composable
fun HistoryScreenProductList(
    viewModel: ProductViewModel
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp)
    ){
        items(viewModel.productsData.size){
            ProductCard(viewModel.productsData[it], viewModel)
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    viewModel: ProductViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
        shape = RoundedCornerShape(15.dp) ,
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
        ) {
            ProductInfo(product.title, product.quantity, product.price)
            Button(
                onClick = {
                    viewModel.removeDataFromDatabase(product)
                    viewModel.productsData.remove(product)
                },
                modifier = Modifier
                    .padding(end = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor,
                ),
            ) {
                Icon(Icons.Rounded.Close, null)
            }
        }
    }
}

@Composable
fun ProductInfo(
    productTitle: String,
    productQuantity: String,
    productPrice: String
) {
    Column(
        modifier = Modifier
            .padding(start = 25.dp, end = 25.dp, top = 15.dp, bottom = 15.dp)
    ) {
        ProductInfoRow("Title: ", productTitle)
        ProductInfoRow("Quantity: ", productQuantity)
        ProductInfoRow("Price: ", productPrice + " €")
    }
}

@Composable
fun ProductInfoRow(
    categoryName: String,
    productInfo: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = categoryName,
            color = textColor,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.75.sp
        )
        Text(
            text = productInfo,
            color = textColor,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.75.sp
        )
    }
}

@Composable
fun HistoryScreenNavigationBar(
    navigation: NavController
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(bottom = 5.dp),
    ) {
        Button(
            onClick = {
                navigation.navigate(Routes.SCREEN_QR_SCANNER)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray.copy(alpha = 0.125f),
                contentColor = textColor
            )
        ) {
            Icon(Icons.Rounded.QrCodeScanner, null)
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray.copy(alpha = 0.125f),
                contentColor = textColor
            ),
            enabled = false
        ) {
            Icon(Icons.Rounded.History, null)
        }
    }
}