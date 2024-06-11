package com.example.rentify.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Cart
import com.example.rentify.sqlite.SQLite
import com.google.android.material.internal.ViewUtils.hideKeyboard

class cart_adapter(private var cartList: ArrayList<Cart>, var contexts: Context) :
    RecyclerView.Adapter<cart_adapter.ShowCart>() {

    private var listener : onCartItemSelected? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowCart {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.cart_recyclerview, parent, false)
        return ShowCart(itemView)
    }

    class ShowCart(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.image_item_cart)
        val itemName: TextView = itemView.findViewById(R.id.name_item_cart)
        val itemPrice: TextView = itemView.findViewById(R.id.price_item_cart)
        val timeCount: EditText = itemView.findViewById(R.id.item_count)
        val timeStamp: TextView = itemView.findViewById(R.id.cart_time_stamp)
        val checkbox: CheckBox = itemView.findViewById(R.id.checkbox_item_cart)
    }

    @SuppressLint("RestrictedApi")
    override fun onBindViewHolder(holder: ShowCart, position: Int) {
        val curr_item = cartList[position]
        holder.checkbox.isChecked = curr_item.isSelected
        val fs = Firestore(contexts)
        val sqlite = SQLite(contexts)
        fs.getItemFromDB(curr_item.itemID).addOnSuccessListener { item ->
            Glide.with(holder.itemView.context).load(item.itemImg).into(holder.image)
            holder.itemName.text = item.itemName
            holder.itemPrice.text = MainActivity.formatRupiah(item.itemPrice)
            holder.timeStamp.text = item.itemTime
            holder.timeCount.setText(curr_item.duration.toString())

            holder.timeCount.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // TODO : UPDATE THE CHANGES
                    var duration = holder.timeCount.text.toString().toInt()
                    if (duration < 1){
                        Toast.makeText(contexts , "Duration cannot be under 0", Toast.LENGTH_SHORT).show()
                    }
                    else if (duration > 30 && holder.timeStamp.text.toString() == "Day"){
                        Toast.makeText(contexts , "Max 30 Days", Toast.LENGTH_SHORT).show()
                    }
                    else if (duration > 12 && holder.timeStamp.text.toString() == "Month"){
                        Toast.makeText(contexts , "Max 12 Months", Toast.LENGTH_SHORT).show()
                    }
                    else if (duration > 10 && holder.timeStamp.text.toString() == "Year"){
                        Toast.makeText(contexts , "Max 10 Years", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        sqlite.updateDuration(curr_item,holder.timeCount.text.toString().toInt())
                        listener?.onCartSelected()
                        hideKeyboard(v)
                        holder.timeCount.clearFocus()
                    }

                    true
                } else {
                    false
                }
            }

            holder.checkbox.setOnClickListener{
                // TODO : UPDATE THE CHANGES
                val sqlite = SQLite(contexts)
                sqlite.toggleItemSelection(curr_item,holder.checkbox.isChecked)
                listener?.onCartSelected()
            }
        }
    }

    fun setOnCartClickListener(listener : onCartItemSelected){
        this.listener = listener
    }

    interface onCartItemSelected {
        fun onCartSelected()
    }

    override fun getItemCount(): Int {
        return cartList.size
    }


}
