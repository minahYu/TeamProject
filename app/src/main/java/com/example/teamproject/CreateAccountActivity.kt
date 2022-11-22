package com.example.teamproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.teamproject.databinding.ActivityCreateAccountBinding
import com.example.teamproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class CreateAccountActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    val binding1 by lazy {ActivityCreateAccountBinding.inflate(layoutInflater)}
    val binding2 by lazy {ActivityLoginBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding1.root)

        binding1.joinButton.setOnClickListener {
            signInAndSignUp()
        }

        binding1.delete.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding1.checkButton.setOnClickListener {
            verifyDuplicatedEMail();
        }
    }
    fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(binding2.email.text.toString(), binding2.password.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    //Create an Account
                    transitionPage2(task.result?.user)
                }
                else if(task.exception?.message.isNullOrEmpty()) {
                    //Login Error
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun verifyDuplicatedEMail() {
        auth?.createUserWithEmailAndPassword(binding2.email.text.toString(), binding2.password.text.toString())
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Success Registering E-mail!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Failed To Register E-mail", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun transitionPage2(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}