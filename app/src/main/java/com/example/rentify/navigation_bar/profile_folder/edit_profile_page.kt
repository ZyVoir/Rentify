package com.example.rentify.navigation_bar.profile_folder

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.User
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class edit_profile_page : AppCompatActivity() {

    var uri : Uri? = null
    private lateinit var profilePicture : ImageButton
    private lateinit var usernameET : EditText
    private lateinit var emailET : EditText
    private lateinit var phoneNumET : EditText
    private lateinit var streetET : EditText
    private lateinit var cityET : EditText
    private lateinit var provinceET : EditText
    private lateinit var countryET : EditText
    private lateinit var postcodeET : EditText
    private lateinit var paymentMethod : Spinner
    private lateinit var saveBtn : Button

    private lateinit var coordinate : kotlin.collections.ArrayList<Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_page)
        supportActionBar?.hide()

        profilePicture = findViewById(R.id.Profile_picture)
        usernameET = findViewById(R.id.username_edit_profile)
        emailET = findViewById(R.id.email_edit_profile)
        phoneNumET = findViewById(R.id.Phone_Number_edit_profile)
        streetET = findViewById(R.id.Street_editText)
        cityET = findViewById(R.id.city_EditText)
        provinceET = findViewById(R.id.province_EditText)
        countryET = findViewById(R.id.countryEditText)
        postcodeET = findViewById(R.id.postCode_editText)
        paymentMethod = findViewById(R.id.spinner_payment_method)
        saveBtn = findViewById(R.id.save_button_edit)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // if google -> not allowed to change profile pic, email, username
        val isGoogle = MainActivity.currentUser.isGoogleAuth
        if (!isGoogle){
            //        profile picture
            profilePicture.setOnClickListener{
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Choose image to upload"),0)
            }
        }else {
            usernameET.isEnabled = false
            usernameET.isFocusable = false
            usernameET.isFocusableInTouchMode = false
        }

        // both user role cannot change email
        emailET.setText(MainActivity.currentUser.email)

        //set initial
        usernameET.setText(MainActivity.currentUser.username)
        phoneNumET.setText(MainActivity.currentUser.phoneNum)
        streetET.setText(MainActivity.currentUser.addressStreet)
        cityET.setText(MainActivity.currentUser.addressCity)
        provinceET.setText(MainActivity.currentUser.addressProvince)
        countryET.setText(MainActivity.currentUser.addressCountry)
        if(MainActivity.currentUser.addressPostcode != 0){
            postcodeET.setText(MainActivity.currentUser.addressPostcode.toString())
        }


        if (MainActivity.currentUser.profilePicLink.isNotEmpty()){
            Glide.with(this).load(MainActivity.currentUser.profilePicLink).into(profilePicture)
        }

        val adapterPaymentMethod = ArrayAdapter.createFromResource(
            this,
            R.array.paymentMethod, android.R.layout.simple_spinner_item
        )
        adapterPaymentMethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentMethod.adapter = adapterPaymentMethod
        if (MainActivity.currentUser.paymentMethod.isNotEmpty()){
            paymentMethod.setSelection(adapterPaymentMethod.getPosition(MainActivity.currentUser.paymentMethod))
        }


//      save button
        saveBtn.setOnClickListener{

            if (validateProfile()){
                val uid = MainActivity.currentUser.uid
                val email = MainActivity.currentUser.email
                val username = usernameET.text.toString()
                val phoneNum = phoneNumET.text.toString()
                val street = streetET.text.toString()
                val city = cityET.text.toString()
                val province = provinceET.text.toString()
                val country = countryET.text.toString()
                val postCode = postcodeET.text.toString().toInt()
                val payment = paymentMethod.selectedItem.toString()


                val toUpdate = User(uid,email,MainActivity.currentUser.name,
                    username,MainActivity.currentUser.profilePicLink, phoneNum,
                    street,city,province,country,postCode,payment,true, isGoogle, coordinate[0], coordinate[1])
                val db = Firestore(this)
                if (uri != null){ // the normal user wants to upload image
                    val storage = FirebaseStorage.getInstance().reference
                    val fileRef = storage.child("users/${uid}")
                    val uploadTask = fileRef.putFile(uri!!)
                    uploadTask.addOnSuccessListener {
                        fileRef.downloadUrl.addOnSuccessListener {url ->
                            MainActivity.currentUser.profilePicLink = url.toString()
                            // set toupdate's urlpic to the newest
                            toUpdate.profilePicLink = MainActivity.currentUser.profilePicLink
                            db.updateProfile(toUpdate).addOnCompleteListener {
                                db.setCurrentUser(uid).addOnCompleteListener { task->
                                    if (it.isSuccessful){
                                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    }else {
                                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }else {
                    db.updateProfile(toUpdate).addOnCompleteListener {
                        db.setCurrentUser(uid).addOnCompleteListener { task->
                            if (it.isSuccessful){
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }else {
                                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }


            }
        }
    }

    private fun validateProfile(): Boolean {
        val username = usernameET.text.toString()
        val phoneNum = phoneNumET.text.toString()
        val street = streetET.text.toString()
        val city = cityET.text.toString()
        val province = provinceET.text.toString()
        val country = countryET.text.toString()
        val postCode = postcodeET.text.toString()
        val payment = paymentMethod.selectedItem.toString()

        val address = "$street, $city, $province, $country, $postCode"

        coordinate  = getLatLngFromAddress(address);

        if (username.isEmpty() || phoneNum.isEmpty() || street.isEmpty() || city.isEmpty() || province.isEmpty() || country.isEmpty() || postCode.isEmpty() || payment.isEmpty()){
            Toast.makeText(this, "all field must be filled!", Toast.LENGTH_SHORT).show()
            return false;
        }
        else if (phoneNum.length < 11 || phoneNum.length > 13){
            Toast.makeText(this, "phone number must between 11-13 digits",Toast.LENGTH_SHORT).show()
            return false
        }
        else if (street.length < 10){
            Toast.makeText(this , "Street Address must be longer than 10 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (postCode.length < 5){
            Toast.makeText(this , "PostCode must be longer than 4 digit", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (coordinate.isEmpty()){
            Toast.makeText(this , "Address must be valid", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.data != null){
            uri = data.data
            try {
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                profilePicture.setImageBitmap(bitmap)
            }catch (e : java.lang.Exception){
                Log.e("Exception", "Error" + e.message.toString())
            }
        }
    }

    private fun getLatLngFromAddress(address: String) : ArrayList<Double> {
        val geocoder = Geocoder(this, Locale.getDefault())
        var lat : Double = 0.0
        var lng : Double = 0.0
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                lat = location.latitude
                lng = location.longitude
                return arrayListOf(lat,lng)
            } else {
                Log.e("Geocoding", "No location found for the address")
                return arrayListOf()
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Geocoding failed: ${e.message}")
        }
        return arrayListOf(lat,lng)
    }
}