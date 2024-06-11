package com.example.rentify.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Firestore
import com.example.rentify.model.Item
import com.example.rentify.sqlite.SQLite

class seller_item_adapter(private  var seller_itemList: MutableList<Item>, private val context: Context,): RecyclerView.Adapter<seller_item_adapter.ShowItem>() {

    private var listener: OnSellerListClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowItem {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.seller_item_recyclerview,parent,false)
        return ShowItem(itemView)
    }

    override fun getItemCount(): Int {
        return seller_itemList.size
    }

    class ShowItem(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.image_seller_item)
        val itemName: TextView = itemView.findViewById(R.id.nama_item)
        val itemPrice: TextView = itemView.findViewById(R.id.price)
        val timeStamp: TextView = itemView.findViewById(R.id.time_stamp_seller_item)
        val description: TextView = itemView.findViewById(R.id.description)
        val deleteButon: ImageButton = itemView.findViewById(R.id.delete_item_button)
    }

    override fun onBindViewHolder(holder: ShowItem, position: Int) {
        val curr_seller_item= seller_itemList[position]
        Glide.with(holder.itemView.context).load(curr_seller_item.itemImg).into(holder.image)
        holder.itemName.text= curr_seller_item.itemName
        holder.itemPrice.text= MainActivity.formatRupiah(curr_seller_item.itemPrice)
        holder.timeStamp.text= curr_seller_item.itemTime
        holder.description.text= curr_seller_item.itemDesc
        holder.deleteButon.setOnClickListener{
            val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_delete_dialog, null)
            val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
            val alertDialog = dialogBuilder.create()

            val noButton = dialogView.findViewById<Button>(R.id.deleteBtn_NO)
            val yesButton = dialogView.findViewById<Button>(R.id.deleteBtn_YES)

            noButton.setOnClickListener {
                alertDialog.dismiss()
            }

            yesButton.setOnClickListener {
                if (curr_seller_item.isRented){
                    Toast.makeText(context, "On going rented item cannot be deleted", Toast.LENGTH_SHORT).show()
                }
                else {
                    val db = Firestore(context)
                    db.deleteItem(curr_seller_item.itemID).addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            // DELETE DATA FROM CART IF THERES ANY IN SQLITE
                            val sqlite = SQLite(context)
                            sqlite.deleteItemCascadeCart(curr_seller_item.itemID)
                            Toast.makeText(context, "${curr_seller_item.itemName} successfully deleted", Toast.LENGTH_SHORT).show()
                            listener?.onSellerListClick(position)
                        }
                        else {
                            Toast.makeText(context, "${curr_seller_item.itemName} deletion failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }


                alertDialog.dismiss()
            }

            alertDialog.show()


            val width = (330 * context.resources.displayMetrics.density).toInt()
            val height = (170 * context.resources.displayMetrics.density).toInt()
            alertDialog.window?.setLayout(width, height)

        }
    }

    fun SetOnSellerListClickListener(listener : OnSellerListClickListener){
        this.listener = listener
    }

    interface OnSellerListClickListener {
        fun onSellerListClick(position: Int)
    }
}