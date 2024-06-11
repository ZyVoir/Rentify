package com.example.rentify.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.rentify.MainActivity
import com.example.rentify.R
import com.example.rentify.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider


// class that handles auth (email password and Google OAuth)
class Auth(context: Context, private val activity : Activity) {
    val context = context
    val auth = FirebaseAuth.getInstance()
    var db = Firestore(context)

    fun register(name : String, username : String, email : String, password : String ) : Task<Boolean> {
        var isSuccess = TaskCompletionSource<Boolean>();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                db.initializeUserUponRegister(auth.uid,name,username,email,"",false)
                isSuccess.setResult(true)
            }
            else {
                val exception = task.exception
                if (exception is FirebaseAuthUserCollisionException) {
                    if (exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                        Toast.makeText(context, "This email address is already in use.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        return isSuccess.task;
    }

    fun login (email : String, password : String) : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                isSuccess.setResult(true)
            }else {
                Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }

        return isSuccess.task
    }

    fun signOut () : Task<Boolean>{
        var isSuccess = TaskCompletionSource<Boolean>()
        auth.signOut()
        MainActivity.currentUser = User("", "", "", "", "", "", "","","","",0,"",false, false, 0.0,0.0)
        isSuccess.setResult(true)
        return isSuccess.task
    }


    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    fun googleLogin(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, onSuccess: (FirebaseAuth) -> Unit, onFailure: (Exception) -> Unit) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account, onSuccess, onFailure)
            } catch (e: ApiException) {
                onFailure(e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onSuccess: (FirebaseAuth) -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess(auth)
                } else {
                    onFailure(task.exception ?: Exception("Sign in failed"))
                }
            }
    }
}