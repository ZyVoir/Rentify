package com.example.rentify.authPages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.rentify.R
import com.example.rentify.navigation_bar.bottom_navigation_bar


class opening : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)
        supportActionBar?.hide()

        val signInButton= findViewById<Button>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            val intent = Intent(this, login_page::class.java)
            startActivity(intent)
        }
        val signUpButton= findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            val intent = Intent(this, register_page::class.java)
            startActivity(intent)
        }
    }
}