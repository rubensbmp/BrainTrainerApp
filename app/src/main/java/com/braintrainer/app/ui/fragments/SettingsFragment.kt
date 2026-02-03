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
            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {
                MusicManager.saveAll(requireContext())
            }
        })
        
        binding.seekSfx.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: android.widget.SeekBar?, p1: Int, p2: Boolean) {
                MusicManager.sfxVolume = p1 / 100f
            }
            override fun onStartTrackingTouch(p0: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(p0: android.widget.SeekBar?) {
                MusicManager.saveAll(requireContext())
            }
        })
        
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = when(checkedId) {
                R.id.rbPt -> "pt"
                R.id.rbEn -> "en"
                R.id.rbFr -> "fr"
                R.id.rbEs -> "es"
                else -> "pt"
            }
            // Save settings before locale change restarts activity
            MusicManager.saveAll(requireContext())
            
            // Apply Locale
            val appLocale = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        // Music Tracks Initialization
        when(MusicManager.currentTrack) {
            "bg_music" -> binding.rbTrackDefault.isChecked = true
            "bg_lofi" -> binding.rbTrackLoFi.isChecked = true
            "bg_chuva" -> binding.rbTrackChuva.isChecked = true
            "bg_brown_noise" -> binding.rbTrackBrown.isChecked = true
        }

        binding.rgMusicTrack.setOnCheckedChangeListener { _, checkedId ->
            val track = when(checkedId) {
                R.id.rbTrackDefault -> "bg_music"
                R.id.rbTrackLoFi -> "bg_lofi"
                R.id.rbTrackChuva -> "bg_chuva"
                R.id.rbTrackBrown -> "bg_brown_noise"
                else -> "bg_music"
            }
            MusicManager.changeTrack(requireContext(), track)
        }

        // Click SFX Toggle
        updateClickSfxButton()
        binding.btnToggleClickSFX.setOnClickListener {
            MusicManager.isClickSoundEnabled = !MusicManager.isClickSoundEnabled
            MusicManager.saveAll(requireContext())
            updateClickSfxButton()
        }
        
        // Reminder Toggle
        updateReminderButton()
        binding.btnDailyReminder.setOnClickListener {
            val isEnabled = isReminderEnabled()
            val newState = !isEnabled
            setReminderEnabled(newState)
            updateReminderButton()
            
            if (newState) {
                checkNotificationPermission()
                scheduleDailyReminder()
            } else {
                cancelDailyReminder()
            }
        }
        
        binding.btnAbout.setOnClickListener {
            com.braintrainer.app.util.DialogHelper.showMessageDialog(
                context = requireContext(),
                title = getString(R.string.dialog_about_title),
                message = getString(R.string.dialog_about_message)
            )
        }

        // Logout
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnLogout.setOnClickListener {
                com.braintrainer.app.util.DialogHelper.showMessageDialog(
                    context = requireContext(),
                    title = getString(R.string.dialog_confirm_title),
                    message = getString(R.string.dialog_confirm_logout),
                    positiveText = getString(R.string.dialog_btn_yes),
                    negativeText = getString(R.string.dialog_btn_cancel),
                    onPositive = {
                         com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                         binding.btnLogout.visibility = View.GONE
                    }
                )
            }
        }
    }
    
    private fun updateClickSfxButton() {
        binding.btnToggleClickSFX.text = if (com.braintrainer.app.util.MusicManager.isClickSoundEnabled) {
            "Sons de Clique: ON"
        } else {
            "Sons de Clique: OFF"
        }
    }


    // Basic shared pref for reminder state (could be in MusicManager or separate PrefsManager)
    private fun isReminderEnabled(): Boolean {
        return requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .getBoolean("daily_reminder_enabled", false)
    }

    private fun setReminderEnabled(enabled: Boolean) {
        requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("daily_reminder_enabled", enabled)
            .apply()
    }

    private fun updateReminderButton() {
        val enabled = isReminderEnabled()
        binding.btnDailyReminder.text = if (enabled) "Lembrete Diário: ON" else "Lembrete Diário: OFF"
    }

    private fun scheduleDailyReminder() {
        val context = requireContext()
        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, com.braintrainer.app.util.DailyReminderReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, 1001, intent, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set for 10:00 AM
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 10)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Tomorrow
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                     alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                     // Request permission intent if needed, simplifies for now to non-exact or toast
                     // For MVP, if permission missing, just try setExact... it might crash or throw SecurityException, so we catch
                     alarmManager.set(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            android.widget.Toast.makeText(context, "Lembrete agendado para 10:00!", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // In case of missing SCHEDULE_EXACT_ALARM on Android 12+
             android.widget.Toast.makeText(context, "Permissão necessária para alarmes exatos.", android.widget.Toast.LENGTH_LONG).show()
             startActivity(android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }

    private fun cancelDailyReminder() {
        val context = requireContext()
        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, com.braintrainer.app.util.DailyReminderReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, 1001, intent, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        android.widget.Toast.makeText(context, "Lembrete cancelado.", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 2002)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
