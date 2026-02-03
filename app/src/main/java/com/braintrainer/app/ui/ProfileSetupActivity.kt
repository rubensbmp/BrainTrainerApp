package com.braintrainer.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityProfileSetupBinding

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private val viewModel: ProfileSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener {
            val name = binding.etName.text.toString()
            val dob = binding.etDob.text.toString()
            viewModel.saveProfile(name, dob)
        }
        
        binding.etDob.addTextChangedListener(object : android.text.TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = java.util.Calendar.getInstance()

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]|\\.".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]|\\.".toRegex(), "")

                    val cl = clean.length
                    var sel = cl
                    var i = 2
                    while (i <= cl && i < 6) {
                        sel++
                        i += 2
                    }
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length)
                    } else {
                        // This part makes sure that when we finish entering numbers
                        // the date is correct, fixing it if needed
                        var day = Integer.parseInt(clean.substring(0, 2))
                        var mon = Integer.parseInt(clean.substring(2, 4))
                        var year = Integer.parseInt(clean.substring(4, 8))

                        mon = if (mon < 1) 1 else if (mon > 12) 12 else mon
                        cal.set(java.util.Calendar.MONTH, mon - 1)
                        year = if (year < 1900) 1900 else if (year > 2100) 2100 else year
                        cal.set(java.util.Calendar.YEAR, year)
                        
                        day = if (day > cal.getActualMaximum(java.util.Calendar.DATE)) cal.getActualMaximum(java.util.Calendar.DATE) else day
                        clean = String.format("%02d%02d%02d", day, mon, year)
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8))

                    sel = if (sel < 0) 0 else sel
                    current = clean
                    binding.etDob.setText(current)
                    binding.etDob.setSelection(if (sel < current.length) sel else current.length)
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable) {}
        })

        binding.btnConnectApple.setOnClickListener {
             startProviderLogin("apple.com")
        }
        
        binding.btnConnectFacebook.setOnClickListener {
             startProviderLogin("facebook.com")
        }
    }
    
    private fun startProviderLogin(providerId: String) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val provider = com.google.firebase.auth.OAuthProvider.newBuilder(providerId)
        
        // Custom scopes if needed
        if (providerId == "facebook.com") {
            provider.addCustomParameter("display", "popup")
        }
        if (providerId == "apple.com") {
            provider.addCustomParameter("locale", "pt")
        }

        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // User is signed in.
                val user = authResult.user
                if (user != null) {
                    val name = user.displayName ?: "User ${user.uid.take(4)}"
                    val dob = "01/01/2000" // Default or fetch if possible (usually not via simple scope)
                    viewModel.saveProfile(name, dob)
                    Toast.makeText(this, "Conectado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure.
                if (e is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                     Toast.makeText(this, "Esta conta já está vinculada.", Toast.LENGTH_LONG).show()
                } else {
                     Toast.makeText(this, "Erro ao conectar: ${e.localizedMessage}\nVerifique o console do Firebase.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setupObservers() {
        viewModel.navigateToMenu.observe(this) { navigate ->
            if (navigate) {
                MainMenuActivity.start(this)
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
