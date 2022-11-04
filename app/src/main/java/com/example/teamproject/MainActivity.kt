package com.example.teamproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.auth.signInWithEmailAndPassword("pg6655@naver.com", "swjm@980605")
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    println("Login Success")
                    println(Firebase.auth.currentUser?.uid)
                }
                else {
                    println("Login Failed ${it.exception?.message}")
                }
            }

        Firebase.auth.createUserWithEmailAndPassword("hanseonwoo@gmail.com", "swjm@980605")
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    println("Login Success")
                    println(Firebase.auth.currentUser?.uid)
                }
                else  {
                    println("Login Failed ${it.exception?.message}")
                }
            }
    }
}