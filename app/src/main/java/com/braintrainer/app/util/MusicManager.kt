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

    private var soundPool: android.media.SoundPool? = null
    private var soundMap = mutableMapOf<Int, Int>()
    
    fun initSFX(context: Context) {
        if (soundPool == null) {
            soundPool = android.media.SoundPool.Builder().setMaxStreams(5).build()
            // Load sounds
            loadSound(context, "sfx_correct", 1)
            loadSound(context, "sfx_wrong", 2)
            loadSound(context, "sfx_finish", 3)
        }
    }
    
    private fun loadSound(context: Context, name: String, key: Int) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) {
            val soundId = soundPool?.load(context, resId, 1) ?: 0
            soundMap[key] = soundId
        }
    }
    
    fun playSFX(key: Int) {
        val soundId = soundMap[key] ?: return
        soundPool?.play(soundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    object SFX {
        const val CORRECT = 1
        const val WRONG = 2
        const val FINISH = 3
    }

    private var activeActivities = 0
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun startMusic(context: Context) {
        activeActivities++
        handler.removeCallbacks(stopRunnable)
        
        if (mediaPlayer == null) {
            try {
                val resId = context.resources.getIdentifier("bg_music", "raw", context.packageName)
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
