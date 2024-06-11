package com.example.rentify.navigation_bar.home_folder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.adapter.cart_adapter
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Cart
import com.example.rentify.model.Transaction
import com.example.rentify.sqlite.SQLite
import java.util.UUID

class cart_page : AppCompatActivity(), cart_adapter.onCartItemSelected {

    private lateinit var cartList: ArrayList<Cart>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: cart_adapter
    private lateinit var backButton: ImageButton
    private lateinit var selectAllBtn: CheckBox
    private lateinit var checkOut_Button: Button
    private lateinit var totalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_page)
        supportActionBar?.hide()

        totalPrice = findViewById(R.id.total_price)


        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val db = SQLite(this)


        cartList = db.getAllCartByUserID(MainActivity.currentUser.uid)
        recyclerView = findViewById(R.id.Cart_recycler_view)
        adapter = cart_adapter(cartList, this)
        adapter.setOnCartClickListener(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        selectAllBtn = findViewById(R.id.select_all_checkbox)

        var isCheckable = true
        for (i in 0 until cartList.size) {
            if (!cartList[i].isSelected) {
                isCheckable = false
                break
            }
        }
        if (cartList.size != 0) selectAllBtn.isChecked = isCheckable
        else selectAllBtn.isChecked = !isCheckable

        selectAllBtn.setOnClickListener {

            for (i in 0 until cartList.size) {
                db.toggleItemSelection(cartList[i], selectAllBtn.isChecked)
                cartList[i].isSelected = selectAllBtn.isChecked
            }
            db.getTotalPrice(MainActivity.currentUser.uid) { grandTotal ->
                totalPrice.text = MainActivity.formatRupiah(grandTotal)
            }
            adapter.notifyDataSetChanged()
        }


        db.getTotalPrice(MainActivity.currentUser.uid) { grandTotal ->
            totalPrice.text = MainActivity.formatRupiah(grandTotal)
        }

        checkOut_Button = findViewById(R.id.checkout_button)
        checkOut_Button.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.custom_cart_checkout_dialog, null)
            val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
            val alertDialog = dialogBuilder.create()


            val noButton = dialogView.findViewById<Button>(R.id.checkoutBtn_NO)
            val yesButton = dialogView.findViewById<Button>(R.id.checkoutBtn_YES)

            noButton.setOnClickListener {
                alertDialog.dismiss()
            }

            yesButton.setOnClickListener {
                if (MainActivity.currentUser.isCompletedProfile) {
                    // Handle checkout logic here
                    val checkOutList = db.getAllCartByUserID(MainActivity.currentUser.uid)
                    val fs = Firestore(this)
                    val db = SQLite(this)

                    if (checkOutList.size == 0){
                        Toast.makeText(this, "There's no selected items", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                        return@setOnClickListener
                    }
                    for (i in 0 until checkOutList.size) {
                        if (checkOutList[i].isSelected) {
                            val cartInstance = checkOutList[i]
                            val renterInstance = MainActivity.currentUser
                            fs.getItemFromDB(cartInstance.itemID)
                                .addOnSuccessListener { itemInstance ->
                                    fs.getUserFromDB(itemInstance.tenantID)
                                        .addOnSuccessListener { tenantInstance ->
                                            val trid = UUID.randomUUID().toString()
                                            val transactionInstance = Transaction(
                                                trid,
                                                MainActivity.getCurrentTime(),
                                                MainActivity.getDueDate(
                                                    cartInstance.duration,
                                                    itemInstance.itemTime
                                                ),
                                                MainActivity.transactionStatus[0],
                                                itemInstance.itemPrice * cartInstance.duration,
                                                tenantInstance.uid,
                                                tenantInstance.coordinateLatitude,
                                                tenantInstance.coordinateLongitude,
                                                renterInstance.uid,
                                                renterInstance.coordinateLatitude,
                                                renterInstance.coordinateLongitude,
                                                itemInstance.itemID,
                                                itemInstance.itemImg,
                                                itemInstance.itemName,
                                                itemInstance.itemDesc,
                                                itemInstance.itemCategory,
                                                itemInstance.itemTime,
                                                itemInstance.itemPrice,
                                                false
                                            )

                                            fs.insertTransaction(transactionInstance)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        fs.updateItemRent(itemInstance.itemID, true)
                                                            .addOnCompleteListener { task2 ->
                                                                if (task2.isSuccessful) {
                                                                    db.deleteItemCascadeCart(
                                                                        itemInstance.itemID
                                                                    )
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                }
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Selected Items Rent Successfully!", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                } else {
                    Toast.makeText(this, "User must complete profile first!", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }


                alertDialog.dismiss()
            }

            alertDialog.show()

            val width = (350 * resources.displayMetrics.density).toInt()
            val height = (220 * resources.displayMetrics.density).toInt()
            alertDialog.window?.setLayout(width, height)


        }

    }

    override fun onCartSelected() {
        val db = SQLite(this)
        val tempCartList = db.getAllCartByUserID(MainActivity.currentUser.uid)
        if (selectAllBtn.isChecked) {
            for (i in 0 until tempCartList.size) {
                if (!tempCartList[i].isSelected) {
                    selectAllBtn.isChecked = false
                    break
                }
            }
        } else if (!selectAllBtn.isChecked) {
            var isSelectAllChecked = true
            for (i in 0 until tempCartList.size) {
                if (!tempCartList[i].isSelected) {
                    isSelectAllChecked = false
                    break
                }
            }
            selectAllBtn.isChecked = isSelectAllChecked
        }

        db.getTotalPrice(MainActivity.currentUser.uid) { grandTotal ->
            totalPrice.text = MainActivity.formatRupiah(grandTotal)
        }

    }
}