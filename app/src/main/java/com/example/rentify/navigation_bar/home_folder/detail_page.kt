package com.example.rentify.navigation_bar.home_folder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Cart
import com.example.rentify.model.Item
import com.example.rentify.model.Transaction
import com.example.rentify.sqlite.SQLite
import com.google.android.material.internal.ViewUtils
import com.google.android.material.internal.ViewUtils.hideKeyboard
import java.util.*

class detail_page : AppCompatActivity() {

    private lateinit var imageIV: ImageView
    private lateinit var nameTV: TextView
    private lateinit var descTV: TextView
    private lateinit var priceTV: TextView
    private lateinit var profileImage: ImageView
    private lateinit var profileUsername: TextView
    private lateinit var profileLocation: TextView
    private lateinit var cartBtn: Button
    private lateinit var cartIcon: ImageView
    private lateinit var instance: Item
    private lateinit var checkOut_button: Button
    private lateinit var totalPrice : TextView
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var timeStamp: TextView
    private lateinit var itemName: TextView
    private lateinit var duration: EditText



    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_page)
        supportActionBar?.hide()

        imageIV = findViewById(R.id.item_image)
        nameTV = findViewById(R.id.item_name)
        descTV = findViewById(R.id.item_description)
        priceTV = findViewById(R.id.item_price)
        profileImage = findViewById(R.id.profile_image)
        profileUsername = findViewById(R.id.about_renter_username)
        profileLocation = findViewById(R.id.about_renter_location)
        cartBtn = findViewById(R.id.add_cart_button)
        cartIcon = findViewById(R.id.cart_button)
        checkOut_button = findViewById(R.id.check_out_button)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        cartIcon.setOnClickListener{
            val intent = Intent(this, cart_page::class.java)
            startActivity(intent)
        }

        instance = intent.getSerializableExtra("item_data") as Item

        if (instance != null) {
            val db = Firestore(this)
            db.getUserFromDB(instance.tenantID).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result
                    Glide.with(this).load(user.profilePicLink).into(profileImage)
                    profileUsername.text = user.username
                    profileLocation.text = user.addressCity

                }
            }
            Glide.with(this).load(instance.itemImg).into(imageIV)
            nameTV.text = instance.itemName
            descTV.text = instance.itemDesc
            priceTV.text = MainActivity.formatRupiah(instance.itemPrice) + " / " + instance.itemTime

        }


        cartBtn.setOnClickListener {
            if (instance.tenantID == MainActivity.currentUser.uid){
                Toast.makeText(this, "Don't rent your own item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val db = SQLite(this)
            db.addToCart(Cart(MainActivity.currentUser.uid, instance.itemID, 1, false))
            Toast.makeText(this, "${instance.itemName} has been added to cart", Toast.LENGTH_SHORT)
                .show()
        }

        checkOut_button.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.custom_detail_checkout_dialog, null)
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            val alertDialog = dialogBuilder.create()

            noButton = dialogView.findViewById(R.id.checkoutBtn_NO)
            yesButton = dialogView.findViewById(R.id.checkoutBtn_YES)
            timeStamp = dialogView.findViewById(R.id.time_stamp_textView)
            itemName = dialogView.findViewById(R.id.item_name_textView)
            duration = dialogView.findViewById(R.id.rent_duration_editText)
            totalPrice = dialogView.findViewById(R.id.totalPrice_textview)

            duration.setText(1.toString())

            itemName.text = instance.itemName
            timeStamp.text = instance.itemTime
            setPrice(instance.itemPrice * duration.text.toString().toInt());

            duration.doOnTextChanged { text, _, _, _ ->
                if (text != null && text.isNotEmpty() && text.length < 6) {
                   setPrice(instance.itemPrice * text.toString().toInt())
                }else {
                    setPrice(0)
                }
            }

            noButton.setOnClickListener {
                alertDialog.dismiss()
            }

            yesButton.setOnClickListener {
                if (duration.text.toString().isEmpty() || !duration.text.toString().isDigitsOnly()){
                    Toast.makeText(this, "Please fill a number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (instance.tenantID == MainActivity.currentUser.uid){
                    Toast.makeText(this, "Don't rent your own item", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val qty = duration.text.toString().toInt()
                val time = timeStamp.text.toString()
                if (!MainActivity.currentUser.isCompletedProfile) {
                    Toast.makeText(this, "User must complete profile first!", Toast.LENGTH_SHORT)
                        .show()
                } else if (qty < 1) {
                    Toast.makeText(this, "Must be more than 0", Toast.LENGTH_SHORT)
                        .show()
                } else if (qty > 30 && time == "Day") {
                    Toast.makeText(this, "Max 30 Days", Toast.LENGTH_SHORT).show()
                } else if (qty > 12 && time == "Month") {
                    Toast.makeText(this, "Max 12 Months", Toast.LENGTH_SHORT).show()
                } else if (qty > 10 && time == "Year") {
                    Toast.makeText(this, "Max 10 Years", Toast.LENGTH_SHORT).show()
                } else {
                    val renterInstance = MainActivity.currentUser
                    val fs = Firestore(this)
                    fs.getUserFromDB(instance.tenantID).addOnSuccessListener { tenantInstance ->
                        val tid = UUID.randomUUID().toString()
                        val transactionInstance = Transaction(
                            tid,
                            MainActivity.getCurrentTime(),
                            MainActivity.getDueDate(qty, time),
                            MainActivity.transactionStatus[0],
                            instance.itemPrice * qty,
                            tenantInstance.uid,
                            tenantInstance.coordinateLatitude,
                            tenantInstance.coordinateLongitude,
                            renterInstance.uid,
                            renterInstance.coordinateLatitude,
                            renterInstance.coordinateLongitude,
                            instance.itemID,
                            instance.itemImg,
                            instance.itemName,
                            instance.itemDesc,
                            instance.itemCategory,
                            instance.itemTime,
                            instance.itemPrice,
                            false
                        )
                        fs.insertTransaction(transactionInstance).addOnCompleteListener {
                            if (it.isSuccessful) {
                                fs.updateItemRent(instance.itemID, true)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val sqLite = SQLite(this)
                                            sqLite.deleteItemCascadeCart(instance.itemID)
                                            Toast.makeText(
                                                this,
                                                "${instance.itemName} rent successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                    }
                            }
                        }
                    }

                    alertDialog.dismiss()
                }


            }

            alertDialog.show()
        }
    }

     fun setPrice(price : Int) {
        totalPrice.text =  MainActivity.formatRupiah(price)
    }
}