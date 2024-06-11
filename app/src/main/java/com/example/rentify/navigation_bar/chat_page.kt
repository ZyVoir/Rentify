package com.example.rentify.navigation_bar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rentify.R

class chat_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_page)
        supportActionBar?.hide()
    }
}