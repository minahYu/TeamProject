package com.example.teamproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.teamproject.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.signin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            signInEmail(email, password)
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun signInEmail(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    //Login Success
                    moveMainPage(task.result?.user)
                } else {
                    //show the error message
                    Log.w("LoginActivity", "signInWithEmail", task.exception)
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}