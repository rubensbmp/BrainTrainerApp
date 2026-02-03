package com.braintrainer.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
    }

    override fun onResume() {
        super.onResume()
        com.braintrainer.app.util.MusicManager.startMusic(this)
    }

    override fun onPause() {
        super.onPause()
        com.braintrainer.app.util.MusicManager.stopMusic()
    }
    
    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        // Set default to Home (Position 2)
        // Order: Settings (0) <- Stats (1) <- Home (2) -> Training (3)
        binding.viewPager.currentItem = 2
        binding.viewPager.offscreenPageLimit = 3
    }

    private inner class MainPagerAdapter(fa: androidx.fragment.app.FragmentActivity) : androidx.viewpager2.adapter.FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): androidx.fragment.app.Fragment {
            return when (position) {
                0 -> com.braintrainer.app.ui.fragments.SettingsFragment()
                1 -> com.braintrainer.app.ui.fragments.StatsFragment()
                2 -> com.braintrainer.app.ui.fragments.HomeFragment()
                3 -> com.braintrainer.app.ui.fragments.TrainingFragment()
                4 -> com.braintrainer.app.ui.fragments.SocialFragment()
                else -> com.braintrainer.app.ui.fragments.HomeFragment()
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MainMenuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
}

