package com.braintrainer.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.braintrainer.app.MainActivity
import com.braintrainer.app.R
import com.braintrainer.app.databinding.ActivityIntroBinding
import com.braintrainer.app.databinding.FragmentIntroSettingsBinding
import com.braintrainer.app.util.MusicManager
import com.google.android.material.tabs.TabLayoutMediator

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val adapter = IntroAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // No text, just dots
        }.attach()
    }
    
    class IntroAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            return if (position == 0) WelcomeFragment() else SettingsFragment()
        }
    }
}

class WelcomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_intro_welcome, container, false)
    }
}

class SettingsFragment : Fragment() {
    private var _binding: FragmentIntroSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.seekMusic.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: android.widget.SeekBar?, p1: Int, p2: Boolean) {
                MusicManager.musicVolume = p1 / 100f
            }
            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {}
        })
        
        binding.seekSfx.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: android.widget.SeekBar?, p1: Int, p2: Boolean) {
                MusicManager.sfxVolume = p1 / 100f
            }
            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {}
        })
        
        binding.btnEnterApp.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
        
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = when(checkedId) {
                R.id.rbPt -> "pt"
                R.id.rbEn -> "en"
                R.id.rbFr -> "fr"
                R.id.rbEs -> "es"
                else -> "pt"
            }
            setLocale(lang)
        }
    }
    
    private fun setLocale(languageCode: String) {
        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
