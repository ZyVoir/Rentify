package com.example.rentify.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rentify.R
import com.example.rentify.model.Category

class category_adapter(private val context: Context, private val categoryList: ArrayList<Category>) :
    RecyclerView.Adapter<category_adapter.CategoryViewHolder>() {
    private var listener: category_adapter.OnCategoryClickListener? = null

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.category_name_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.categories_button, parent, false)
        return CategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryName.text = category.buttonName

        setCategoryColors(holder.categoryName, category.isClicked)

        holder.itemView.setOnClickListener {

            if(category.isClicked){
                for (cat in categoryList) {
                    cat.isClicked = false
                }
            }else {
                for (cat in categoryList) {
                    cat.isClicked = false
                }
                category.isClicked = true
            }
            notifyDataSetChanged()
            listener?.onCategoryClick(position)
        }
    }

    fun setOnCategoryClickListener(listener: OnCategoryClickListener) {
        this.listener = listener
    }

    interface OnCategoryClickListener {
        fun onCategoryClick(position: Int)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    private fun setCategoryColors(categoryName: TextView, isClicked: Boolean) {
        val colorRes = if (isClicked) {
            R.color.selected_color
        } else {
            R.color.Unselected_color
        }
        categoryName.setBackgroundColor(ContextCompat.getColor(context, colorRes))

        val textColorRes = if (isClicked) {
            R.color.white
        } else {
            R.color.selected_color
        }
        categoryName.setTextColor(ContextCompat.getColor(context, textColorRes))
    }
}
