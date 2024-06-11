package com.example.rentify.model

data class Cart(
    var userID : String = "",
    var itemID : String = "",
    var duration : Int = 0,
    var isSelected: Boolean= false,
)
