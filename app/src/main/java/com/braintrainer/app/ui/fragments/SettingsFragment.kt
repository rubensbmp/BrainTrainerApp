package com.braintrainer.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.braintrainer.app.R
import com.braintrainer.app.databinding.FragmentSettingsBinding
import com.braintrainer.app.util.MusicManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initial State
        binding.seekMusic.progress = (MusicManager.musicVolume * 100).toInt()
        binding.seekSfx.progress = (MusicManager.sfxVolume * 100).toInt()
        
        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentLang.contains("en")) binding.rbEn.isChecked = true
        else if (currentLang.contains("fr")) binding.rbFr.isChecked = true
        else if (currentLang.contains("es")) binding.rbEs.isChecked = true
        else binding.rbPt.isChecked = true
        
        // Listeners
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
        
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = when(checkedId) {
                R.id.rbPt -> "pt"
                R.id.rbEn -> "en"
                R.id.rbFr -> "fr"
                R.id.rbEs -> "es"
                else -> "pt"
            }
            // Apply Locale
            val appLocale = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
        
        binding.btnAbout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_about_title))
                .setMessage(getString(R.string.dialog_about_message))
                .setPositiveButton("OK", null)
                .show()
        }

        // Logout
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnLogout.setOnClickListener {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.dialog_confirm_title))
                    .setMessage(getString(R.string.dialog_confirm_logout))
                    .setPositiveButton(getString(R.string.dialog_btn_yes)) { _, _ ->
                         com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                         binding.btnLogout.visibility = View.GONE
                    }
                    .setNegativeButton(getString(R.string.dialog_btn_cancel), null)
                    .show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
