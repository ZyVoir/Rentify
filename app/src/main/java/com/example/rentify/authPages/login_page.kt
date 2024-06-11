package com.example.rentify.authPages

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.firebase.Auth
import com.example.rentify.firebase.Firestore
import com.example.rentify.navigation_bar.bottom_navigation_bar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class login_page : AppCompatActivity() {

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var loginBtn: Button
    private lateinit var googleLoginBtn: Button
    val auth = Auth(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        supportActionBar?.hide()

        emailET = findViewById(R.id.email_login_input)
        passwordET = findViewById(R.id.password_login_input)
        loginBtn = findViewById(R.id.login_button)
        googleLoginBtn = findViewById(R.id.google_login)

        val registerText = findViewById<TextView>(R.id.SignUpHere)
        registerText.setOnClickListener {
            val intent = Intent(this, register_page::class.java)
            startActivity(intent)
        }


        loginBtn.setOnClickListener {
            val email = emailET.text.toString()
            val password = passwordET.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All field must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val auth = Auth(this, this)
                auth.login(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val db = Firestore(this)
                        db.setCurrentUser(auth.auth.uid).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                emailET.setText("")
                                passwordET.setText("")
                                Toast.makeText(this, "Login Sucessfull", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, bottom_navigation_bar::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }

            }
        }

        googleLoginBtn.setOnClickListener {
            auth.googleLogin()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        auth.handleActivityResult(requestCode, resultCode, data,
            onSuccess = { auth: FirebaseAuth ->
                val user = auth.currentUser
                if (user != null) {
                    val uid = user.uid
                    val name = user.displayName
                    val email = user.email
                    val img = user.photoUrl.toString()
                    val db = Firestore(this)
                    db.getUserFromDB(uid).addOnSuccessListener { task ->
                        if (task == null && name != null && email != null) {
                            db.initializeUserUponRegister(uid, name, name, email, img, true)
                        }

                        db.setCurrentUser(uid).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Google Login Succesfull", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, bottom_navigation_bar::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            },
            onFailure = { exception: Exception ->
                // Handle sign-in failure
                Log.e("MainActivity", "Sign in failed", exception)
            }
        )
    }
}