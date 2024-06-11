package com.example.rentify.navigation_bar.transaction_folder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rentify.R

class map_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_page)
        supportActionBar?.hide()
    }
}