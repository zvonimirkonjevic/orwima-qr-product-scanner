package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QRCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
): ImageAnalysis.Analyzer {

    private val supportedImageFormat = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,
    )

    override fun analyze(image: ImageProxy) { // this fn is called for every frame of our camera
        if(image.format in supportedImageFormat){
            val bytes = image.planes.first().buffer.toByteArray() // get raw image bytes from scanned qr code (image)

            val source  = PlanarYUVLuminanceSource( // extends LuminanceSource around an array of YUV data returned from the camera driver to make image more readable
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )

            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try{
                val result = MultiFormatReader().apply{ // stores in this case link that is derived from qr code
                    setHints( // sets supported types of code we would like to scan
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                                BarcodeFormat.QR_CODE,
                            )
                        )
                    )
                }.decode(binaryBmp)
                onQrCodeScanned(result.text)
            } catch(e: Exception){
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray{ // transforms bytebuffer to bytearray
        rewind()
        return ByteArray(remaining()).also{
            get(it)
        }
    }
}