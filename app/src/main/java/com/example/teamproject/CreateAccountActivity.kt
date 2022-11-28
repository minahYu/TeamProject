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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_account.*

class CreateAccountActivity : AppCompatActivity() {
    //var auth : FirebaseAuth? = null
    val binding1 by lazy {ActivityCreateAccountBinding.inflate(layoutInflater)}
    //val binding2 by lazy {ActivityLoginBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding1.root)

        binding1.joinButton.setOnClickListener {
            val userEmail = binding1.joinEmail.text.toString()
            val password = binding1.joinPassword.text.toString()
            signUp(userEmail, password)
        }

        binding1.delete.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        /*binding1.checkButton.setOnClickListener {
            //val userEmail = binding1.joinEmail.text.toString()
            verifyDuplicatedEMail()
        }*/
    }
    /*fun signInAndSignUp() {
        auth?.createUserWithEmailAndPassword(binding2.email.text.toString(), binding2.password.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    //Create an Account
                    Toast.makeText(this, "Create an Account!", Toast.LENGTH_SHORT).show()
                    transitionPage2(task.result?.user)
                    finish()
                }
                else if(task.exception?.message.isNullOrEmpty()) {
                    //Login Error
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }*/

    private fun signUp(userEmail: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if(binding1.joinPassword.text.toString().equals(binding1.joinPwck.text.toString()))
                {
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Create an account", Toast.LENGTH_SHORT).show()
                        //val user = auth?.currentUser
                        transitionPage2(it.result?.user)
                        //finish()
                    } else if(it.exception?.message.isNullOrEmpty() == false) {
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                    }
                    else {
                        //Log.w("LoginActivity", "signInWithEmail", it.exception)
                        //Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                        Toast.makeText(this, "Find that duplicated E-mail", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun transitionPage2(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}