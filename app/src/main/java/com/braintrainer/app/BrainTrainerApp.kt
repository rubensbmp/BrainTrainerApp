package com.braintrainer.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class BrainTrainerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Apply saved language
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "pt") ?: "pt"
        applyLocale(this, lang)
    }

    private fun applyLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
