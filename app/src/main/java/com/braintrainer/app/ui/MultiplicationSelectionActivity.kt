package com.braintrainer.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityMultiplicationSelectionBinding
import com.braintrainer.app.ui.game.GameActivity
import com.braintrainer.app.R

class MultiplicationSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplicationSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplicationSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvTableLabel.text = "${getString(R.string.dialog_multiplication_table)} ${binding.sbTable.progress + 1}"
        binding.tvRoundsLabel.text = "${getString(R.string.dialog_multiplication_rounds)} ${binding.sbRounds.progress + 10}"
    }

    private fun setupListeners() {
        binding.sbTable.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTableLabel.text = "${getString(R.string.dialog_multiplication_table)} ${progress + 1}"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.sbRounds.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvRoundsLabel.text = "${getString(R.string.dialog_multiplication_rounds)} ${progress + 10}"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.btnStart.setOnClickListener {
            val table = binding.sbTable.progress + 1
            val rounds = binding.sbRounds.progress + 10
            GameActivity.start(
                this,
                "MULTIPLICATION",
                "TABLE_$table",
                rounds,
                0L,
                true // isPractice
            )
            finish()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MultiplicationSelectionActivity::class.java)
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
