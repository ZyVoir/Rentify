package com.example.rentify.navigation_bar

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.example.rentify.R
import com.example.rentify.authPages.register_page

class chat_list_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list_page)
        supportActionBar?.hide()

        val backButton= findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

}