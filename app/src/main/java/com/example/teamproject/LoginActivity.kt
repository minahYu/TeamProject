package com.example.teamproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.teamproject.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInAccount? = null
    val binding = ActivityLoginBinding.inflate(layoutInflater)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.signin.setOnClickListener {
            signInAndSignUp()
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
            ?.addOnCompleteListener {
                task ->
                if(task.isSuccessful) {
                    //Create an Account
                    transitionPage1(task.result?.user)
                }
                else if(task.exception?.message.isNullOrEmpty()) {
                    //Login Error
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
                else {
                    //Login Success!!!
                    signInEmail()
                }
            }
    }

    fun signInEmail() {
        auth?.signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    //Login Success
                    transitionPage1(task.result?.user)
                } else {
                    //show the error message
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

    fun transitionPage1(user:FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }



}