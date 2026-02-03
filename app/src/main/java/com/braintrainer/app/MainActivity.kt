package com.braintrainer.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user exists (simple check on main thread for purely prototype speed, 
        // ideally strictly coroutine, but onCreate allowed for Splash logic if fast).
        // Better: Use a ViewModel to check.
        
        val viewModel = MainViewModel(application) // Using AndroidViewModel factory implicit
        viewModel.checkUserStatus()
        
        viewModel.userExists.observe(this) { exists ->
            if (exists) {
                com.braintrainer.app.ui.MainMenuActivity.start(this)
            } else {
                val intent = android.content.Intent(this, com.braintrainer.app.ui.ProfileSetupActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
