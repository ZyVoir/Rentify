package com.example.rentify.model

data class Item(
    var itemID: String = "",
    var tenantID: String = "",
    var itemImg: String = "",
    var itemName: String = "",
    var itemDesc: String = "",
    var itemCategory: String = "",
    var itemTime: String = "",
    var itemPrice: Int = 0,
    var isRented: Boolean = false
) : java.io.Serializable