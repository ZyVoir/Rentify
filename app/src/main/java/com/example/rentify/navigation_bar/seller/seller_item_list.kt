package com.example.rentify.navigation_bar.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.adapter.home_adapter
import com.example.rentify.adapter.seller_item_adapter
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Item
import com.example.rentify.navigation_bar.bottom_navigation_bar
import com.google.firebase.firestore.ListenerRegistration

class seller_item_list : AppCompatActivity(), seller_item_adapter.OnSellerListClickListener {
    private lateinit var backButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: seller_item_adapter
    private var seller_itemList = mutableListOf<Item>()
    private var listenerRegistration: ListenerRegistration? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_item_list)
        supportActionBar?.hide()

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        addButton = findViewById(R.id.add_item_button)
        addButton.setOnClickListener {
            val intent = Intent(this, seller_add_item::class.java)
            startActivity(intent)
        }


        recyclerView = findViewById(R.id.seller_recycler_view)
        adapter = seller_item_adapter(seller_itemList, this)
        adapter.SetOnSellerListClickListener(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        val fs = Firestore(this)
        listenerRegistration = fs.db.collection("items")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    seller_itemList.clear()
                    for (doc in snapshots.documents) {
                        val item = doc.toObject(Item::class.java)
                        if (item != null && item.tenantID == MainActivity.currentUser.uid) {
                            seller_itemList.add(item)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

    }


    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    override fun onSellerListClick(position: Int) {
        recreate()
    }
}