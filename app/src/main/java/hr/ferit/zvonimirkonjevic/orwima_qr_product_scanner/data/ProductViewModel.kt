package hr.ferit.zvonimirkonjevic.orwima_qr_product_scanner.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class ProductViewModel: ViewModel() {
    private val db = Firebase.firestore
    val productsData = mutableStateListOf<Product>()

    init {
        fetchDatabaseData()
    }

    private fun fetchDatabaseData() {
        productsData.clear()
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (data in result.documents) {
                    val product = data.toObject(Product::class.java)
                    if (product != null) {
                        product.id = data.id
                        productsData.add(product)
                    }
                }
            }
    }


    fun removeDataFromDatabase(product: Product) {
        db.collection("products")
            .document(product.id)
            .delete()
        productsData.remove(product)
    }

    fun addDataToDatabase(product: Product) {
        val productToAdd = hashMapOf(
            "title" to product.title,
            "description" to product.description,
            "quantity" to product.quantity,
            "price" to product.price
        )

        db.collection("products")
            .document(product.id)
            .set(productToAdd)
        productsData.add(product)
    }

    fun sortByPrice(asc: Boolean){
        productsData.clear()
        if(asc){
            db.collection("products").orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (data in result.documents) {
                        val product = data.toObject(Product::class.java)
                        if (product != null) {
                            product.id = data.id
                            productsData.add(product)
                        }
                    }
                }
        }
        else{
            db.collection("products").orderBy("price", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    for (data in result.documents) {
                        val product = data.toObject(Product::class.java)
                        if (product != null) {
                            product.id = data.id
                            productsData.add(product)
                        }
                    }
                }
        }
    }

    fun isProductInDatabase(id: String): Boolean{
        return productsData.find {it.id == id} != null
    }

    fun fetchExistingProduct(id: String): Product{
        for(product in productsData){
            if(product.id == id){
                return product
            }
        }
        throw IllegalArgumentException("Object not found.")
    }
}