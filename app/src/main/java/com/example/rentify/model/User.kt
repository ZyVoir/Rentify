package com.example.rentify.model

data class User(
    var uid : String,
    var email : String,
    var name : String,
    var username : String,
    var profilePicLink : String,
    var phoneNum : String,
    var addressStreet : String,
    var addressCity : String,
    var addressProvince : String,
    var addressCountry : String,
    var addressPostcode : Int,
    var paymentMethod : String,
    var isCompletedProfile : Boolean,
    var isGoogleAuth : Boolean,
    var coordinateLatitude : Double,
    var coordinateLongitude : Double
)