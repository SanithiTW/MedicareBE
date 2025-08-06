package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import java.util.Locale
import com.example.donorlk.R

class ForgotPasswordController : BaseActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var verifyButton: Button
    private lateinit var timerText: TextView
    private lateinit var resendButton: TextView
    private lateinit var backButton: ImageView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        // Initialize views first
        initializeViews()
        // Then set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        verifyButton = findViewById(R.id.verifyButton)
        timerText = findViewById(R.id.timerText)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        verifyButton.setOnClickListener {
            if (validateEmail()) {
                val intent = Intent(this, VerificationController::class.java)
                intent.putExtra("email", emailEditText.text.toString())
                startActivity(intent)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        resendButton.setOnClickListener {
            if (validateEmail()) {
                Toast.makeText(this, getString(R.string.resend_code), Toast.LENGTH_SHORT).show()
                startResendTimer()
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = emailEditText.text.toString()
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email"
            return false
        }
        return true
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        resendButton.isEnabled = false

        countDownTimer = object : CountDownTimer(5 * 60 * 1000, 1000) { // 5 minutes
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                timerText.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.timer_format),
                    minutes,
                    seconds
                )
            }

            override fun onFinish() {
                timerText.text = getString(R.string.timer_finished)
                resendButton.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
