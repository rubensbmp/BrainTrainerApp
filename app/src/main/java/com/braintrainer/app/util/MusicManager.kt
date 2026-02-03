package com.braintrainer.app.util

import android.content.Context
import android.media.MediaPlayer
import com.braintrainer.app.R

object MusicManager {
    private var mediaPlayer: MediaPlayer? = null
    var musicVolume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(field, field)
        }
    
    var sfxVolume: Float = 1.0f
    var isClickSoundEnabled: Boolean = true
    var currentTrack: String = "bg_music"

    fun loadPreferences(context: Context) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        musicVolume = prefs.getFloat("music_volume", 0.5f)
        sfxVolume = prefs.getFloat("sfx_volume", 1.0f)
        isClickSoundEnabled = prefs.getBoolean("click_sfx_enabled", true)
        currentTrack = prefs.getString("current_track", "bg_music") ?: "bg_music"
    }

    private fun saveSetting(context: Context, key: String, value: Any) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            when (value) {
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
            }
            apply()
        }
    }

    fun saveAll(context: Context) {
        saveSetting(context, "music_volume", musicVolume)
        saveSetting(context, "sfx_volume", sfxVolume)
        saveSetting(context, "click_sfx_enabled", isClickSoundEnabled)
        saveSetting(context, "current_track", currentTrack)
    }

    fun changeTrack(context: Context, trackName: String) {
        if (currentTrack == trackName) return
        currentTrack = trackName
        saveSetting(context, "current_track", currentTrack)
        
        val wasPlaying = mediaPlayer?.isPlaying ?: false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        if (wasPlaying || activeActivities > 0) {
            // Restart with new track
            activeActivities-- // Temporarily decrement to reuse startMusic logic
            startMusic(context)
        }
    }
    private var soundPool: android.media.SoundPool? = null
    private var soundMap = mutableMapOf<Int, Int>()
    
    fun initSFX(context: Context) {
        if (soundPool == null) {
            soundPool = android.media.SoundPool.Builder().setMaxStreams(5).build()
        }
        // Ensure sounds are loaded (safe against process restarts or partial loads)
        if (!soundMap.containsKey(SFX.CORRECT)) loadSound(context, "sfx_correct", SFX.CORRECT)
        
        // Try to load 'erro2' first (User preference), otherwise fallback to legacy 'sfx_wrong'
        if (!soundMap.containsKey(SFX.WRONG)) {
             var resId = context.resources.getIdentifier("erro2", "raw", context.packageName)
             if (resId != 0) {
                 val soundId = soundPool?.load(context, resId, 1) ?: 0
                 soundMap[SFX.WRONG] = soundId
             } else {
                 loadSound(context, "sfx_wrong", SFX.WRONG)
             }
        }
        
        if (!soundMap.containsKey(SFX.FINISH)) loadSound(context, "sfx_finish", SFX.FINISH)
        if (!soundMap.containsKey(SFX.CLICK)) loadSound(context, "click", SFX.CLICK)
    }
    
    private fun loadSound(context: Context, name: String, key: Int) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            val soundId = soundPool?.load(context, resId, 1) ?: 0
            soundMap[key] = soundId
        }
    }
    
    private val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)

    fun playSFX(key: Int) {
        if (key == SFX.CLICK && !isClickSoundEnabled) return
        
        val soundId = soundMap[key]
        if (soundId != null && soundId != 0) {
            // Priority: 2 for Game Events (Correct/Wrong/Finish), 1 for UI Clicks
            val priority = if (key == SFX.CLICK) 1 else 2
            soundPool?.play(soundId, sfxVolume, sfxVolume, priority, 0, 1f)
        } else if (key == SFX.WRONG) {
            // Fallback for Wrong sound if file fails
            toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 150)
        } else if (key == SFX.CORRECT) {
             toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_ONE_MIN_BEEP, 150)
        }
    }

    object SFX {
        const val CORRECT = 1
        const val WRONG = 2
        const val FINISH = 3
        const val CLICK = 4
    }

    private var activeActivities = 0
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun startMusic(context: Context) {
        activeActivities++
        handler.removeCallbacks(stopRunnable)
        
        if (mediaPlayer == null) {
            try {
                val resId = context.resources.getIdentifier(currentTrack, "raw", context.packageName)
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.setVolume(musicVolume, musicVolume)
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        }
    }

    fun stopMusic() {
        activeActivities--
        if (activeActivities < 0) activeActivities = 0
        handler.postDelayed(stopRunnable, 100) // Wait 100ms (fast check)
    }
    
    private fun fadeOutAndPause() {
        val mp = mediaPlayer ?: return
        if (!mp.isPlaying) return
        
        val startVolume = musicVolume
        val fadeDuration = 300L // 0.3 second fade (very fast)
        val steps = 10
        val delayPerStep = fadeDuration / steps
        val volumeStep = startVolume / steps
        
        var currentStep = 0
        
        val fader = object : Runnable {
            override fun run() {
                if (activeActivities > 0) {
                    // Resumed during fade? Restore volume!
                    mp.setVolume(musicVolume, musicVolume)
                    return
                }
                
                currentStep++
                val newVol = startVolume - (volumeStep * currentStep)
                if (newVol <= 0) {
                    mp.pause()
                    mp.setVolume(musicVolume, musicVolume) // Reset for next start
                } else {
                    mp.setVolume(newVol, newVol)
                    handler.postDelayed(this, delayPerStep)
                }
            }
        }
        handler.post(fader)
    }
    
    // Update stopRunnable to call fadeOutAndPause
    private val stopRunnable = Runnable {
        if (activeActivities == 0) {
            fadeOutAndPause()
        }
    }
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
