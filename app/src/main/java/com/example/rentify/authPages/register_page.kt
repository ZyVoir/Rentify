package com.example.rentify.authPages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.rentify.R
import com.example.rentify.firebase.Auth
import com.example.rentify.navigation_bar.bottom_navigation_bar


class register_page : AppCompatActivity() {

    private lateinit var nameET : EditText;
    private lateinit var usernameET : EditText
    private lateinit var emailET : EditText;
    private lateinit var passwordET : EditText
    private lateinit var Register_button : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)
        supportActionBar?.hide()

        val loginText= findViewById<TextView>(R.id.SignUpHere)
        loginText.setOnClickListener {
            val intent = Intent(this, login_page::class.java)
            startActivity(intent)
        }

        Register_button = findViewById(R.id.register_button)
        usernameET =findViewById(R.id.username_register_input)
        passwordET = findViewById(R.id.password_register_input)
        emailET = findViewById(R.id.email_register_input)
        nameET = findViewById(R.id.name_register_input)

        Register_button.setOnClickListener {
            val username = usernameET.text.toString()
            val name = nameET.text.toString()
            val email = emailET.text.toString()
            val password = passwordET.text.toString()

            if(username.isEmpty() && password.isEmpty() && email.isEmpty()&& name.isEmpty()){
                Toast.makeText(this,"all fields must be filled.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(name.isEmpty()){
                Toast.makeText(this,"please enter the name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(username.isEmpty()){
                Toast.makeText(this,"please enter the username.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(email.isEmpty()){
                Toast.makeText(this,"please enter the email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!email.endsWith("@gmail.com")){
                Toast.makeText(this,"email bust be end with '@gmail.com'.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                Toast.makeText(this,"please enter the password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.length<8){
                Toast.makeText(this, "password must be more than 8", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val auth = Auth(this, this)
            auth.register(name,username,email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    usernameET.setText("")
                    nameET.setText("")
                    emailET.setText("")
                    passwordET.setText("")

                    Toast.makeText(this, "Register Success!", Toast.LENGTH_SHORT).show()
                    val Intent = Intent(this, bottom_navigation_bar::class.java)
                    startActivity(Intent)
                }else {
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}