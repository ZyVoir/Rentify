package com.example.rentify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rentify.authPages.opening
import com.example.rentify.model.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        // global var for practical use
        lateinit var currentUser : User

        val transactionStatus = arrayListOf("Waiting for Payment", "Delivering Item", "On Rent", "Returning Item", "Transaction Done")
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun formatRupiah(price: Int): String {
            val localeID = Locale("in", "ID")
            val numberFormat = NumberFormat.getCurrencyInstance(localeID)
            val formattedPrice = numberFormat.format(price)
            // Remove the currency symbol and replace it with "Rp."
            return formattedPrice.replace(NumberFormat.getCurrencyInstance(localeID).currency.symbol, "Rp.")
        }

        fun getCurrentTime() : String {
            val currentDate = Date()
            return formatter.format(currentDate)
        }

        fun convertToDays( qty : Int, frame : String) : Int {
            when (frame){
                "Day" -> return qty
                "Month" -> return qty * 30
                "Year" -> return qty * 365
            }
            return qty
        }

        fun getDueDate(qty: Int, frame: String) : String {
            val days = convertToDays(qty, frame)
            val currentDate = Date()
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val dueDate = calendar.time
            return formatter.format(dueDate)
        }

        fun getExtendedDueDate(date : String, extend: Int, timeframe : String) : String {
            val days = convertToDays(extend,timeframe)
            val initialDueDate : Date = formatter.parse(date) as Date
            val calendar = Calendar.getInstance()
            calendar.time = initialDueDate
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val newDueDate = calendar.time
            return formatter.format(newDueDate)
        }

        fun getTimeDifference(start : String, end : String) : Int {
            val startDate: Date = formatter.parse(start) as Date
            val endDate: Date = formatter.parse(end) as Date

            val diffInMillies = endDate.time - startDate.time
            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS).toInt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val intent = Intent(this, opening::class.java)
        startActivity(intent)
        finish()
    }
}