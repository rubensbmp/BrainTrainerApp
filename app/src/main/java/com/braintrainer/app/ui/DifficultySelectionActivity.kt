package com.braintrainer.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityDifficultySelectionBinding
import com.braintrainer.app.ui.game.GameActivity
import com.braintrainer.app.R

class DifficultySelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDifficultySelectionBinding
    private lateinit var gameType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDifficultySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) ?: "CALCULATION"
        val gameName = intent.getStringExtra(EXTRA_GAME_NAME) ?: "Jogo"

        binding.tvGameTitle.text = gameName.uppercase()
        
        // Setup initial views
        if (gameType == "REFLEX_GREATEST" || gameType == "NUMBER_MEMORY" || gameType == "VISUAL_COUNT" || gameType == "POKER_HAND") {
            binding.containerFlashTime.visibility = android.view.View.VISIBLE
            
            if (gameType == "POKER_HAND") {
               // Initial values for Poker: range 2s to 6s
               binding.sbFlashTime.max = 8 // 0..8 steps -> 2.0s .. 6.0s (0.5s steps)
               binding.sbFlashTime.progress = 4 // Default 4.0s
            }
        }

        binding.tvRoundsLabel.text = getString(R.string.diff_rounds_label, binding.sbRounds.progress + 5)
        updateFlashLabel()

        setupListeners()
    }

    private fun updateFlashLabel() {
        if (gameType == "POKER_HAND") {
            // Logic: 2000ms base + progress * 500ms
            val timeSec = 2.0f + (binding.sbFlashTime.progress * 0.5f)
            binding.tvFlashLabel.text = getString(R.string.diff_poker_speed, timeSec)
        } else {
             // Logic: 0.1s base + progress * 0.1s
            val time = (binding.sbFlashTime.progress + 1) / 10f
            binding.tvFlashLabel.text = getString(R.string.diff_flash_label, time)
        }
    }

    private fun setupListeners() {
        // Rounds Slider: 5 to 20
        binding.sbRounds.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val rounds = progress + 5
                binding.tvRoundsLabel.text = getString(R.string.diff_rounds_label, rounds)
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Flash Time Slider
        binding.sbFlashTime.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
               updateFlashLabel()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // Default Flash Time Logic handled in onCreate


        if (gameType == "FLAG_QUIZ" || gameType == "MAP_QUIZ") {
            binding.containerDifficulty.visibility = android.view.View.GONE
            binding.containerRegions.visibility = android.view.View.VISIBLE
            
            // Map Quiz: Hide Difficulty Selector (RadioGroup)
            if (gameType == "MAP_QUIZ") {
                binding.rgDifficulty.visibility = android.view.View.GONE
            } else {
                binding.rgDifficulty.visibility = android.view.View.VISIBLE
            }

            val regions = mapOf(
                binding.btnWorld to "WORLD",
                binding.btnAmericas to "AMERICAS",
                binding.btnEurope to "EUROPE",
                binding.btnAsia to "ASIA",
                binding.btnAfrica to "AFRICA",
                binding.btnOceania to "OCEANIA"
            )
            
            regions.forEach { (btn, p) ->
                btn.setOnClickListener { startFlagGame(p) }
            }
        } else {
            binding.containerDifficulty.visibility = android.view.View.VISIBLE
            binding.containerRegions.visibility = android.view.View.GONE
            
            binding.btnEasy.setOnClickListener { startGame("EASY") }
            binding.btnMedium.setOnClickListener { startGame("MEDIUM") }
            binding.btnHard.setOnClickListener { startGame("HARD") }
        }
    }
    
    private fun startFlagGame(region: String) {
        // Calculate max rounds possible for this region logic
        // We know counts from CountryData (simplified logic here)
        val maxAvailable = when(region) {
            "WORLD" -> 195
            "AMERICAS" -> 35
            "EUROPE" -> 45
            "ASIA" -> 48
            "AFRICA" -> 54
            "OCEANIA" -> 17
            else -> 10
        }
        
        // Cap selected rounds
        var selected = binding.sbRounds.progress + 5
        if (selected > maxAvailable) selected = maxAvailable
        
        // Get difficulty from RADIO GROUP (to actually be implemented) or UI.
        // For now I'll check if I should add a radio group or just assume MEDIUM default?
        // User asked "countries harder should appear only in hard difficulty".
        // I need to add that UI element or use existing buttons?
        // Let's assume I'll add the UI in next step.
        // For now: I will default to MEDIUM, but prepare strings.
        
        // Wait, I need to know the difficulty selected.
        // I will add a method to get selected difficulty from a new view.
        val diff = getSelectedDifficultyCode() // Helper
        
        GameActivity.start(this, gameType, "$region|$diff", selected, 0L)
        finish()
    }

    private fun getSelectedDifficultyCode(): String {
        return when (binding.rgDifficulty.checkedRadioButtonId) {
            R.id.rbEasy -> "EASY"
            R.id.rbHard -> "HARD"
            else -> "MEDIUM"
        }
    }

    private fun startGame(difficulty: String) {
        val rounds = binding.sbRounds.progress + 5
        val flashTime = if (gameType == "REFLEX_GREATEST" || gameType == "NUMBER_MEMORY" || gameType == "VISUAL_COUNT") {
            (binding.sbFlashTime.progress + 1) * 100L // convert 1-10 to 100ms-1000ms
        } else if (gameType == "POKER_HAND") {
            // 2000ms to 6000ms
            2000L + (binding.sbFlashTime.progress * 500L)
        } else {
            0L
        }

        GameActivity.start(this, gameType, difficulty, rounds, flashTime) 
        finish()
    }

    companion object {
        private const val EXTRA_GAME_TYPE = "EXTRA_GAME_TYPE"
        private const val EXTRA_GAME_NAME = "EXTRA_GAME_NAME"

        fun start(context: Context, gameType: String, gameName: String) {
            val intent = Intent(context, DifficultySelectionActivity::class.java)
            intent.putExtra(EXTRA_GAME_TYPE, gameType)
            intent.putExtra(EXTRA_GAME_NAME, gameName)
            context.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        com.braintrainer.app.util.MusicManager.startMusic(this)
    }

    override fun onPause() {
        super.onPause()
        com.braintrainer.app.util.MusicManager.stopMusic()
    }
}
