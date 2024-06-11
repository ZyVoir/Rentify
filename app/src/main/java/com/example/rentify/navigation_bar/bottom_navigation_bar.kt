package com.example.rentify.navigation_bar

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.navigation_bar.home_folder.home_page
import com.example.rentify.navigation_bar.profile_folder.profile_page
import com.example.rentify.navigation_bar.transaction_folder.transaction_page
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class bottom_navigation_bar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation_bar)
        supportActionBar?.hide()


        currentPage(home_page())

        var navBar= findViewById<ChipNavigationBar>(R.id.bot_NavBar)
        navBar.setItemSelected(R.id.home_nav)

        navBar.setOnItemSelectedListener {
            when(it){
                R.id.home_nav -> currentPage(home_page())
                R.id.transaction_nav-> currentPage(transaction_page())
                R.id.profile_nav-> currentPage(profile_page())
            }
            true
        }
    }

    private fun currentPage(fragment : Fragment) {
        fragment.arguments= Bundle()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_Layout, fragment)
            commit()
        }
    }
}