package com.example.rentify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.model.Item

class home_adapter(private  var itemList: MutableList<Item>):RecyclerView.Adapter<home_adapter.ShowItem>(){
    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): ShowItem{
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.home_item_recyclerview,parent,false)
        return ShowItem(itemView)
    }

    class ShowItem(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView=itemView.findViewById(R.id.image_item_list)
        val itemName: TextView= itemView.findViewById(R.id.name_item_list)
        val itemPrice: TextView= itemView.findViewById(R.id.price_item_list)
        val timeStamp: TextView= itemView.findViewById(R.id.time_stamp_item_list)
    }

    override fun onBindViewHolder(holder: ShowItem, position: Int) {
        val curr_item= itemList[position]
        Glide.with(holder.itemView.context).load(curr_item.itemImg).into(holder.image)
        holder.itemName.text= curr_item.itemName
        holder.itemPrice.text= MainActivity.formatRupiah(curr_item.itemPrice)
        holder.timeStamp.text= " / ${curr_item.itemTime}"

        holder.itemView.setOnClickListener{
            listener?.onItemClick(position)
        }
    }

    override fun getItemCount():Int{
        return itemList.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }




}