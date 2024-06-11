package com.example.rentify.navigation_bar.seller

import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.NumberFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Item
import com.google.firebase.storage.FirebaseStorage
import java.util.Locale
import java.util.UUID

class seller_add_item : AppCompatActivity() {

    var uri : Uri? = null
    private lateinit var backButton : AppCompatImageButton
    private lateinit var imageET : ImageButton
    private lateinit var nameET : TextView
    private lateinit var descET : TextView
    private lateinit var category : Spinner
    private lateinit var timeStamp : Spinner
    private lateinit var priceEditText : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_add_item)
        supportActionBar?.hide()

        backButton = findViewById(R.id.back_button)
        imageET = findViewById(R.id.add_item_image)
        nameET = findViewById(R.id.add_item_name)
        descET = findViewById(R.id.add_item_desc)
        timeStamp = findViewById(R.id.spinner_timeStamp)
        category = findViewById(R.id.spinner_category)
        priceEditText = findViewById(R.id.add_item_price)

        backButton.setOnClickListener {
            finish()
        }

        // image button on click -> select image
        imageET.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose image to upload"),0)
        }

//        time stamp
        val adapter_timeStamp = ArrayAdapter.createFromResource(
            this,
            R.array.timeStamp, android.R.layout.simple_spinner_item
        )
        adapter_timeStamp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeStamp.adapter = adapter_timeStamp

//        category
        val adapter_category = ArrayAdapter.createFromResource(
            this,
            R.array.Category, android.R.layout.simple_spinner_item
        )
        adapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category.adapter = adapter_category


//        Price
        priceEditText.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    priceEditText.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[Rp. ]".toRegex(), "").replace("[,.]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = formatPrice(parsed)
                        current = formatted
                        priceEditText.setText(formatted)
                        priceEditText.setSelection(formatted.length)
                    }

                    priceEditText.addTextChangedListener(this)
                }
            }
        })

        val doneButton: Button = findViewById(R.id.done_buttom)
        doneButton.setOnClickListener{
            if (validateItem()){
                val name = nameET.text.toString()
                val desc = descET.text.toString()
                val ctg = category.selectedItem.toString()
                val time = timeStamp.selectedItem.toString()
                val price = convertFormattedStringtoInt(priceEditText.text.toString())


                // TODO
                // 1 : Upload image to Firebase Storage
                val storage = FirebaseStorage.getInstance().reference
                val randomUUID = UUID.randomUUID().toString()
                val fileRef = storage.child("items/${randomUUID}")
                val uploadTask = fileRef.putFile(uri!!)
                uploadTask.addOnSuccessListener{
                    fileRef.downloadUrl.addOnSuccessListener {
                        // 2. get the link and then put store it in firestore
                        val url = it.toString()
                        val instance = Item(randomUUID,MainActivity.currentUser.uid, url, name,desc,ctg,time,price,false)
                        val db = Firestore(this)
                        db.insertItem(instance).addOnSuccessListener {
                            Toast.makeText(this, "item successfully added!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.data != null){
            uri = data.data
            try {
                val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                imageET.setImageBitmap(bitmap)
            }catch (e : java.lang.Exception){
                Log.e("Exception", "Error" + e.message.toString())
            }
        }
    }

    fun convertFormattedStringtoInt(rupiah: String): Int {
        val sanitizedString = rupiah.replace(".", "")
        return sanitizedString.toInt()
    }

    private fun validateItem(): Boolean {

        // check if the user already set up their profile
        // if not yet, then finish the activity and toast
        if (!MainActivity.currentUser.isCompletedProfile){
            Toast.makeText(this, "Please fill the profile first!", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val name = nameET.text.toString()
        val desc = descET.text.toString()
        val priceText = priceEditText.text.toString()
        val price = convertFormattedStringtoInt(priceText)

        if (uri == null){
            Toast.makeText(this,"Image must be provided", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (name.isEmpty( ) || desc.isEmpty() || priceText.isEmpty()){
            Toast.makeText(this,"All field must be filled!", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (name.length <= 6){
            Toast.makeText(this,"Name must be more than 6 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        else if (name.length >= 35){
            Toast.makeText(this,"Name must be less than 35 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (desc.length <= 15){
            Toast.makeText(this,"Description must be more than 15 characters long", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (price < 10000){
            Toast.makeText(this,"Minimum price : Rp. 10.000", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun formatPrice(value: Double): String {
        val symbols = DecimalFormatSymbols(Locale.GERMANY)
        val formatter: NumberFormat = DecimalFormat("#,###", symbols)
        return formatter.format(value)
    }

}
