package com.tawesstore.app.data.model

data class CartItem(
    val product: Product,
    val quantity: Int,
    val isWholesale: Boolean
) {
    val unitPrice: Double
        get() = if (isWholesale) product.wholesalePrice else product.price

    val totalPrice: Double
        get() = unitPrice * quantity
}
