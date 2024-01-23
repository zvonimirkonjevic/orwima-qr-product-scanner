package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.Routes
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.Product
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.ProductViewModel
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data.QRCodeAnalyzer
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.accentColor
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.cardColor
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.primaryColor
import hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.ui.theme.textColor

@Composable
fun QrScannerScreen(
    navigation: NavController,
    viewModel: ProductViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        QrScanner(viewModel)
        QrScreenNavigationBar(navigation)
    }
}

@Composable
fun QrScanner(
    viewModel: ProductViewModel
){
    val qrCode = remember {
        mutableStateOf("")
    }
    val context = LocalContext.current // we need camera provider future to finally launch our camera
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    var hasCameraPermission by remember { // simple permission check if we allowed use of camera in our app
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {granted ->
            hasCameraPermission = granted

        }
    )
    LaunchedEffect(key1 = true){
        launcher.launch(Manifest.permission.CAMERA)
    }

    BackHandler {
        qrCode.value = ""
    }

    if(hasCameraPermission){
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Camera with QR detection
            AndroidView( // we use this to render our camera preview
                factory = {context->
                    val previewView = PreviewView(context) // view that renders our camera
                    val preview = Preview.Builder().build()
                    val selector = CameraSelector.Builder() // selector that selects back camera and renders it
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    @Suppress("DEPRECATION") val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(
                            Size(
                                previewView.width,
                                previewView.height
                            )
                        ) // defines how we want to analyze our image
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // defines what we should do if our frameRate/s on camera is higher than our function is able to handle
                        .build()
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        QRCodeAnalyzer {result->
                            qrCode.value = result
                        }
                    )
                    try {
                        cameraProviderFuture.get().bindToLifecycle( // launches our camera to our view
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                },
                modifier = Modifier.weight(1f)
            )
            if(viewModel.isProductInDatabase(qrCode.value.hashCode().toString())){
                ProductInfoDisplaySheet(qrCode = qrCode, product = viewModel.fetchExistingProduct(qrCode.value.hashCode().toString()))
            }
            else{
                ProductInfoInputSheet(qrCode = qrCode, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInfoInputSheet(
    qrCode: MutableState<String>,
    viewModel: ProductViewModel
) {
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }
    isSheetOpen = qrCode.value != ""
    if(isSheetOpen){
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen = false
                qrCode.value = ""
            },
            containerColor = primaryColor
        ) {
            ProductInfoSubmitForm(qrCode, viewModel)
        }
    }
}

@Composable
fun ProductInfoSubmitForm(
    qrCode: MutableState<String>,
    viewModel: ProductViewModel
) {

    val productTitle = remember {
        mutableStateOf("")
    }

    val productDescription = remember {
        mutableStateOf("")
    }

    val productQuantity = remember {
        mutableStateOf("")
    }

    val productPrice = remember {
        mutableStateOf("")
    }

    Column (
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .padding(start = 40.dp)
    ) {
        ProductInfoSubmitFormTitle(displayText = "Product Information")
        ProductInfoSubmitFormField("Title:", productTitle)
        ProductInfoSubmitFormField("Description:", productDescription)
        ProductInfoSubmitFormField("Quantity:", productQuantity)
        ProductInfoSubmitFormField("Price:", productPrice)
        ProductInfoSubmitFormButton(qrCode, viewModel, productTitle, productDescription, productQuantity, productPrice)
    }
}

@Composable
fun ProductInfoSubmitFormTitle(
    displayText: String
) {
    Text(
        text = displayText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.5.sp,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 35.dp, top = 8.dp, bottom = 25.dp),
        color = textColor,
    )
}

@Composable
fun ProductInfoSubmitFormField(
    formTitle: String,
    formValue: MutableState<String>
) {
    Text(
        text = formTitle,
        modifier = Modifier.padding(bottom = 8.dp),
        color = textColor,
    )
    TextField(value = formValue.value,
        onValueChange = { formValue.value = it },
        modifier = Modifier
            .padding(bottom = 18.dp),
        shape = RoundedCornerShape(15.dp),
        minLines = 1,
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.LightGray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            unfocusedContainerColor = cardColor,
            focusedContainerColor = cardColor
        ),
    )
}

@Composable
fun ProductInfoSubmitFormButton(
    qrCode: MutableState<String>,
    viewModel: ProductViewModel,
    productTitle: MutableState<String>,
    productDescription: MutableState<String>,
    productQuantity: MutableState<String>,
    productPrice: MutableState<String>,
) {
    Button(
        onClick = {
            viewModel.addDataToDatabase(
                Product(
                    qrCode.value.hashCode().toString(),
                    productTitle.value,
                    productDescription.value,
                    productQuantity.value,
                    productPrice.value
                )
            )
            qrCode.value = ""
            productTitle.value = ""
            productDescription.value = ""
            productQuantity.value = ""
            productPrice.value = ""
        },
        modifier = Modifier
            .padding(bottom = 50.dp, start = 95.dp),
        colors =  ButtonDefaults.buttonColors(
            containerColor = accentColor,
            contentColor = textColor
        )
    ) {
        Text(
            text = "Submit",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun QrScreenNavigationBar(
    navigation: NavController
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(bottom = 20.dp),
    ) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray.copy(alpha = 0.125f),
                contentColor = textColor
            ),
            enabled = false
        ) {
            Icon(Icons.Rounded.QrCodeScanner, null)
        }
        Button(
            onClick = {
                navigation.navigate(Routes.SCREEN_SCANNED_HISTORY)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray.copy(alpha = 0.125f),
                contentColor = textColor
            )
        ) {
            Icon(Icons.Rounded.History, null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInfoDisplaySheet(
    qrCode: MutableState<String>,
    product: Product
) {
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }
    isSheetOpen = qrCode.value != ""
    if(isSheetOpen){
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen = false
                qrCode.value = ""
            },
            containerColor = primaryColor
        ) {
            ProductInfoDisplayForm(product = product)
        }
    }
}



@Composable
fun ProductInfoDisplayForm(
    product: Product
) {
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .padding(start = 40.dp, bottom = 40.dp)
    ) {
        ProductInfoDisplayFormTitle(displayText = "Product Information")
        ProductInfoDisplayField(formTitle = "Title: ", formValue = product.title)
        ProductInfoDisplayField(formTitle = "Description: ", formValue = product.description)
        ProductInfoDisplayField(formTitle = "Quantity: ", formValue = product.quantity)
        ProductInfoDisplayField(formTitle = "Price: ", formValue = product.price + " €")
    }
}

@Composable
fun ProductInfoDisplayFormTitle(
    displayText: String
) {
    Text(
        text = displayText,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.5.sp,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 35.dp, top = 8.dp, bottom = 25.dp),
        color = textColor,
    )
}

@Composable
fun ProductInfoDisplayField(
    formTitle: String,
    formValue: String
) {
    Text(
        text = formTitle,
        modifier = Modifier
            .padding(8.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
    )
    Text(
        text = formValue,
        fontSize = 18.sp,
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
        color = Color.LightGray
    )
}