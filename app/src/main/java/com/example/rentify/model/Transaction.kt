package com.example.rentify.model

data class Transaction(
    //Transaction unique properties
    var transacionID : String = "",
    var transactionDate : String  = "",
    var transactionDueDate : String = "",
    var transactionStatus : String = "",
    var transactionPrice : Int = 0,
    // tenant -> the one who lease the item
    // renter -> the one who rent the item
    // both tenant and renter can be search later using firestore so no need to store
    // exception : the tenant and renter lat lng to keep track for delivering
    var tenantID : String = "",
    var tenantLat : Double = 0.0,
    var tenantLng : Double = 0.0,

    var renterID : String = "",
    var renterLat : Double = 0.0,
    var renterLng : Double = 0.0,

    // item
    // all item Data needs to be in the transaction model (just in case if the real item in the DB gets deleted
    var itemID : String = "",
    var itemImg: String = "",
    var itemName: String = "",
    var itemDesc: String = "",
    var itemCategory: String = "",
    var itemTime: String = "",
    var itemPrice: Int = 0,
    var isExtend: Boolean = false


)
