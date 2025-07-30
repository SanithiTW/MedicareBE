package com.example.donorlk.controllers

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import com.example.donorlk.R
import java.util.Locale

class VerificationController : BaseActivity() {
    private lateinit var emailDisplay: TextView
    private lateinit var verificationInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var timerText: TextView
    private lateinit var resendButton: TextView
    private lateinit var backButton: ImageView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_page)

        // Initialize views
        initializeViews()
        setupClickListeners()

        // Get email from intent and display it
        emailDisplay.text = intent.getStringExtra("email") ?: ""

        // Start initial timer
        startResendTimer()
    }

    private fun initializeViews() {
        emailDisplay = findViewById(R.id.emailDisplay)
        verificationInput = findViewById(R.id.verificationCodeInput)
        verifyButton = findViewById(R.id.verifyButton)
        timerText = findViewById(R.id.timerText)
        resendButton = findViewById(R.id.resendButton)
        backButton = findViewById(R.id.backButton)
    }

    private fun setupClickListeners() {
        verifyButton.setOnClickListener {
            if (validateCode()) {
                // For display purposes, proceed to DonationForm
                startActivity(Intent(this, DonationFormController::class.java))
                // Clear all previous activities from stack
                finishAffinity()
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        resendButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.resend_code), Toast.LENGTH_SHORT).show()
            startResendTimer()
        }
    }

    private fun validateCode(): Boolean {
        val code = verificationInput.text.toString()
        if (code.isEmpty()) {
            verificationInput.error = "Verification code is required"
            return false
        }
        if (code.length != 6) {
            verificationInput.error = "Invalid verification code"
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
