package com.example.rentify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.model.Item
import com.example.rentify.model.Transaction

class transaction_adapter(private  var transactionList: MutableList<Transaction>): RecyclerView.Adapter<transaction_adapter.ShowTransaction>() {
    private var listener: OnTransactionClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowTransaction {
        val itemView= LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_recyclerview,parent,false)
        return ShowTransaction(itemView)
    }

    class ShowTransaction(itemView: View): RecyclerView.ViewHolder(itemView){
        val image: ImageView =itemView.findViewById(R.id.image_item_transaction)
        val itemName: TextView = itemView.findViewById(R.id.name_item_transaction)
        val status: TextView = itemView.findViewById(R.id.Status_transaction)
        val timeStampDay: TextView = itemView.findViewById(R.id.countdown_day_transaction)
        val timeStampMonth: TextView = itemView.findViewById(R.id.countdown_month_transaction)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: ShowTransaction, position: Int) {
        val curr_transaction = transactionList[position]

        Glide.with(holder.itemView.context).load(curr_transaction.itemImg).into(holder.image)
        holder.itemName.text = curr_transaction.itemName
        holder.status.text = curr_transaction.transactionStatus

        val totalDay=MainActivity.getTimeDifference(curr_transaction.transactionDate, curr_transaction.transactionDueDate)

        val months = totalDay / 30
        val days = totalDay % 30

        if (months > 0) {
            holder.timeStampDay.text = days.toString()
            holder.timeStampMonth.text = months.toString()
        } else {
            holder.timeStampDay.text = days.toString()
            holder.timeStampMonth.text = "--"
        }

        if (curr_transaction.transactionStatus == MainActivity.transactionStatus[MainActivity.transactionStatus.size-1]){
            holder.timeStampDay.text = "--"
            holder.timeStampMonth.text = "--"
        }

        holder.itemView.setOnClickListener{
            listener?.onTransactionClick(curr_transaction)
        }

    }

    fun setOnTransactionClickListener(listener: OnTransactionClickListener) {
        this.listener = listener
    }

    interface OnTransactionClickListener {
        fun onTransactionClick(transaction: Transaction)
    }
}