package com.example.firebase_auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firebase_auth.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //auth Firebase
        auth = FirebaseAuth.getInstance()

        //Username validation
        val usernameStream = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { username ->
                username.trim().isEmpty()
            }
        usernameStream.subscribe{
            showTextMinimalAlert(it, "Email/Username")
        }

        //Password validation
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password ->
                password.trim().isEmpty()
            }
        passwordStream.subscribe{
            showTextMinimalAlert(it, "Password")
        }
        //Button Enabled true or false
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if(isValid){
                binding.btnLogin.isEnabled = true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }else{
                binding.btnLogin.isEnabled = false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            loginUser(email, password)
        }
        binding.tvHaventAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPw.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }


    private fun showTextMinimalAlert(isNotValid: Boolean, text:String){
        if (text == "Email/Username")
            binding.etEmail.error = if (isNotValid) "$text tidak boleh kosong!" else null
        else if (text == "Password")
            binding.etPassword.error = if (isNotValid) "$text tidak boleh kosong!" else null
    }

    private fun loginUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) { login->
                if (login.isSuccessful){
                    Intent(this, HomeActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, login.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

}