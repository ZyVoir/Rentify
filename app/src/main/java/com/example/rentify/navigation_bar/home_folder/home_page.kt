package com.example.rentify.navigation_bar.home_folder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rentify.R
import com.example.rentify.adapter.category_adapter
import com.example.rentify.adapter.home_adapter
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Category
import com.example.rentify.model.Item
import com.example.rentify.navigation_bar.bottom_navigation_bar
import com.example.rentify.navigation_bar.chat_list_page
import com.example.rentify.navigation_bar.seller.seller_item_list
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration


class home_page : Fragment(), home_adapter.OnItemClickListener,
    category_adapter.OnCategoryClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var category: RecyclerView
    private lateinit var categoryAdapter: category_adapter
    private var categoryList = ArrayList<Category>()
    private lateinit var adapter: home_adapter
    private lateinit var chatButton: ImageButton
    private lateinit var cartButton: ImageButton
    private lateinit var sellerbtn: ImageButton
    private var itemList = mutableListOf<Item>()
    private var listenerRegistration: ListenerRegistration? = null
    private lateinit var searchBarET: EditText

    private var searchList = mutableListOf<Item>()
    private var selectedCtg: Category = Category("null", false)


    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        searchBarET = view.findViewById(R.id.search_bar)

        searchBarET.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(v)
//                searchBarET.clearFocus()
                true
            } else {
                false
            }
        }

        chatButton = view.findViewById(R.id.chat_list_page_icon)
        chatButton.setOnClickListener {
            val intent = Intent(requireContext(), chat_list_page::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        cartButton = view.findViewById(R.id.cart_button)
        cartButton.setOnClickListener {
            val intent = Intent(requireContext(), cart_page::class.java)
            startActivity(intent)
        }

        sellerbtn = view.findViewById(R.id.seller_button)
        sellerbtn.setOnClickListener {
            val intent = Intent(requireContext(), seller_item_list::class.java)
            startActivity(intent)
        }

//        category recycler VIew
        category = view.findViewById(R.id.categories_recyclerV_view)
        category.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val categoryNames = resources.getStringArray(R.array.Category)
        Log.d("", "${categoryNames.size}")
        for (name in categoryNames) {
            categoryList.add(Category(name, false))
        }
        categoryAdapter = category_adapter(requireContext(), categoryList)
        category.adapter = categoryAdapter
        categoryAdapter.setOnCategoryClickListener(this)


//        home recycler View
        recyclerView = view.findViewById(R.id.home_recycler_view)
        adapter = home_adapter(itemList)
        recyclerView.adapter = adapter
        val layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = layoutManager
        adapter.setOnItemClickListener(this)


        val fs = Firestore(requireContext())
        listenerRegistration = fs.db.collection("items")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    itemList.clear()
                    for (doc in snapshots.documents) {
                        val item = doc.toObject(Item::class.java)
                        if (item != null && !item.isRented) {
                            itemList.add(item)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        searchBarET.doOnTextChanged { text, _, _, _ ->
            if (text != null && text.isEmpty() && !selectedCtg.isClicked) {
                adapter = home_adapter(itemList)
                recyclerView.adapter = adapter
                adapter.setOnItemClickListener(this)
                return@doOnTextChanged
            }
            filter()
        }


        return view
    }

    override fun onItemClick(position: Int) {
        val searchBar = searchBarET.text.toString()
        if (searchBar.isEmpty() && !selectedCtg.isClicked) {
            val clickedItem = itemList.getOrNull(position)
            clickedItem?.let {
                val intent = Intent(requireContext(), detail_page::class.java)
                intent.putExtra("item_data", clickedItem)
                startActivity(intent)
            }
        } else {
            val clickedItem = searchList.getOrNull(position)

            clickedItem?.let {
                val intent = Intent(requireContext(), detail_page::class.java)
                intent.putExtra("item_data", clickedItem)
                startActivity(intent)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    override fun onCategoryClick(position: Int) {
        val instance = categoryList[position]
        if (!instance.isClicked && searchBarET.text.isEmpty()) {
            selectedCtg = instance
            adapter = home_adapter(itemList)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(this)
        } else {
            selectedCtg = instance
            filter()
        }
    }

    override fun onPause() {
        super.onPause()
        searchBarET.setText("")
    }

    private fun filter() {
        searchList.clear()
        val searchText = searchBarET.text.toString().toLowerCase()
        val categoryFilter = selectedCtg
        println(searchText)
        for (item in itemList) {
            val itemNameLower = item.itemName.toLowerCase()
            val categoryLower = item.itemCategory.toLowerCase()

            val textMatch = itemNameLower.startsWith(searchText)
            val ctgMatch = categoryLower == categoryFilter.buttonName.toLowerCase()

            val isUserUseSearchBar = (searchText.isNotEmpty() && !categoryFilter.isClicked) && textMatch
            val isUserUseCategory = (searchText.isEmpty() && categoryFilter.isClicked) && ctgMatch

            val isBoth = (searchText.isNotEmpty() && textMatch && categoryFilter.isClicked && ctgMatch)

            if (isUserUseSearchBar || isUserUseCategory || isBoth){
                searchList.add(item)
            }
        }

        adapter = home_adapter(searchList)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

}
