package com.braintrainer.app.ui.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.braintrainer.app.databinding.ActivityGameBinding
import com.braintrainer.app.R

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var viewModel: GameViewModel
    private var toneGenerator: android.media.ToneGenerator? = null
    private lateinit var gameType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize SFX
        com.braintrainer.app.util.MusicManager.initSFX(this)

        gameType = intent.getStringExtra(EXTRA_GAME_TYPE) ?: "CALCULATION"
        val difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"
        val rounds = intent.getIntExtra(EXTRA_ROUNDS, 10)
        val flashTime = intent.getLongExtra(EXTRA_FLASH_TIME, 0L)
        val isPractice = intent.getBooleanExtra(EXTRA_IS_PRACTICE, false)
        
        if (gameType == "FLAG_QUIZ" || gameType == "MAP_QUIZ") {
            binding.tvQuestion.textSize = 80f // Big Emojis
        }
        
        // Manual Factory injection
        val factory = GameViewModel.Factory(application, gameType, difficulty, rounds, flashTime, isPractice)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[GameViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        com.braintrainer.app.util.MusicManager.startMusic(this)
    }

    override fun onPause() {
        super.onPause()
        com.braintrainer.app.util.MusicManager.stopMusic()
    }

    private fun showIntroOverlay(type: String) {
        val instrResId = when(type) {
            "CALCULATION" -> R.string.instr_calc
            "LOGIC_SYMBOL" -> R.string.instr_logic
            "REFLEX_GREATEST" -> R.string.instr_reflex
            "FLAG_QUIZ" -> R.string.instr_flags
            "MAP_QUIZ" -> R.string.instr_map
            "NUMBER_MEMORY" -> R.string.instr_memory
            "VISUAL_COUNT" -> R.string.instr_visual
            "DAILY_TEST" -> R.string.instr_daily
            "MULTIPLICATION" -> R.string.instr_multiplication
            "POKER_HAND" -> R.string.instr_poker
            else -> R.string.instr_intro_title
        }
        
        binding.tvIntroText.setText(instrResId)
        binding.clGameIntro.visibility = android.view.View.VISIBLE
        viewModel.setTimerPaused(true)
        
        binding.clGameIntro.setOnClickListener {
            binding.clGameIntro.visibility = android.view.View.GONE
            
            if (!viewModel.getIsGameStarted()) {
                viewModel.startGame()
            } else {
                viewModel.acknowledgeGameType()
            }
        }
    }

    private var lastClickedButton: Button? = null

    private fun setupListeners() {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { btn ->
            btn.setOnClickListener {
                lastClickedButton = btn
                // Disable all to prevent multi-click during delay
                buttons.forEach { it.isEnabled = false }
                
                viewModel.submitAnswer(btn.text.toString())
            }
        }
    }
    

    private fun setupObservers() {
        viewModel.currentQuestion.observe(this) { question ->
            // Map Game Image Support
            if (question.imageRes != null) {
                binding.ivQuestionImage.setImageResource(question.imageRes)
                binding.ivQuestionImage.visibility = android.view.View.VISIBLE
                binding.tvQuestion.visibility = android.view.View.GONE
            } else {
                binding.ivQuestionImage.visibility = android.view.View.GONE
                binding.tvQuestion.visibility = android.view.View.VISIBLE
                binding.tvQuestion.text = question.displayContent
            }
            
            // Map options to buttons
            val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
            question.options.forEachIndexed { index, option ->
                if (index < buttons.size) {
                    buttons[index].text = option
                    buttons[index].visibility = android.view.View.VISIBLE
                    buttons[index].isEnabled = true
                    // Reset to purple for new question
                    buttons[index].setBackgroundResource(R.drawable.bg_clash_button_purple)
                    buttons[index].backgroundTintList = null
                }
            }
            
            // Handling Flash Content based on content type (Universal Support)
            val flashViews = listOf(binding.tvFlash1, binding.tvFlash2, binding.tvFlash3, binding.tvFlash4)
            
            // Reset Visibilities
            flashViews.forEach { it.visibility = android.view.View.GONE }
            binding.tvFlashCenter.visibility = android.view.View.GONE
            binding.tvFlashCenterLarge.visibility = android.view.View.GONE
            binding.visualCountView.visibility = android.view.View.GONE
            binding.pokerTableLayout.visibility = android.view.View.GONE
            binding.llHoleCards.removeAllViews()
            binding.llCommunityCards.removeAllViews()
            
            if (question.flashItems.isNotEmpty()) {
                val firstItem = question.flashItems[0]
                
                if (gameType == "POKER_HAND") {
                    binding.pokerTableLayout.visibility = android.view.View.VISIBLE
                    renderPokerGame(question.flashItems)
                } else if (firstItem.startsWith("VISUAL:")) {
                    // Visual Count Mode
                    binding.visualCountView.visibility = android.view.View.VISIBLE
                    val parts = firstItem.split(":")
                    if (parts.size >= 3) {
                        val r = parts[1].toIntOrNull() ?: 0
                        val b = parts[2].toIntOrNull() ?: 0
                        binding.visualCountView.configure(r, b)
                    }
                } else if (question.flashItems.size > 1) {
                    // Reflex Grid Mode
                    question.flashItems.forEachIndexed { index, item ->
                        if (index < flashViews.size) {
                            flashViews[index].text = item
                            flashViews[index].visibility = android.view.View.VISIBLE
                        }
                    }
                } else {
                    // Number Memory Mode (Center Text)
                    binding.tvFlashCenterLarge.visibility = android.view.View.VISIBLE
                    binding.tvFlashCenterLarge.text = firstItem
                }
            }
            
            // Redundant text set if not image, but handled above
            if (question.imageRes == null) {
                binding.tvQuestion.text = question.displayContent
            }
        }
        
        viewModel.roundsInfo.observe(this) { info ->
            binding.tvRounds.text = info
        }
        
        viewModel.answerResult.observe(this) { isCorrect ->
            if (isCorrect) {
                 lastClickedButton?.setBackgroundResource(R.drawable.bg_clash_button_green)
                 lastClickedButton?.backgroundTintList = null
                 com.braintrainer.app.util.MusicManager.playSFX(com.braintrainer.app.util.MusicManager.SFX.CORRECT)
            } else {
                 lastClickedButton?.setBackgroundResource(R.drawable.bg_clash_button_red)
                 lastClickedButton?.backgroundTintList = null
                 com.braintrainer.app.util.MusicManager.playSFX(com.braintrainer.app.util.MusicManager.SFX.WRONG)
                 
                 // Highlight correct one in green
                 val correctAns = viewModel.currentQuestion.value?.answer
                 val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
                 buttons.forEach { btn ->
                     if (btn.text == correctAns) {
                         btn.setBackgroundResource(R.drawable.bg_clash_button_green)
                         btn.backgroundTintList = null
                     }
                 }
            }
            
            // Poker "Touch to Continue" Logic
            if (gameType == "POKER_HAND") {
                binding.tvFlashCenter.text = "Toque para continuar"
                binding.tvFlashCenter.visibility = android.view.View.VISIBLE
                binding.tvFlashCenter.textSize = 24f
                binding.tvFlashCenter.setOnClickListener {
                    binding.tvFlashCenter.visibility = android.view.View.GONE
                    viewModel.forceNextQuestion()
                }
                
                // Highlight winning
                val q = viewModel.currentQuestion.value
                val winStr = q?.flashItems?.lastOrNull { it.startsWith("WIN:") } ?: ""
                 if (winStr.isNotEmpty()) {
                    val indices = winStr.removePrefix("WIN:").split(",").mapNotNull { it.trim().toIntOrNull() }
                    
                    val screenHeight = resources.displayMetrics.heightPixels
                    val moveUpAmount = -(screenHeight * 0.005f) // 0.5% as requested

                    pokerCardViews.forEachIndexed { i, view ->
                        if (indices.contains(i)) {
                            view.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .translationY(moveUpAmount)
                                .alpha(1.0f)
                                .setDuration(300)
                                .start()
                            
                            // Remove board/highlight background
                            if (view is com.braintrainer.app.ui.views.ClashImageView) {
                                view.background = null
                                view.showShadow = false
                            } else if (view is com.braintrainer.app.ui.views.ClashTextView) {
                                view.showShadow = false
                            }
                        } else {
                            view.animate()
                                .alpha(0.3f)
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .translationY(0f)
                                .setDuration(300)
                                .start()
                        }
                    }
                }
            }
        }
        
        viewModel.viewState.observe(this) { state ->
            if (state == "FLASHING") {
                binding.flashContainer.visibility = android.view.View.VISIBLE
                binding.cardQuestion.visibility = android.view.View.INVISIBLE
                binding.answersGrid.visibility = android.view.View.INVISIBLE
            } else {

                binding.answersGrid.visibility = android.view.View.VISIBLE
                
                if (gameType == "POKER_HAND") {
                    binding.flashContainer.visibility = android.view.View.VISIBLE
                    binding.cardQuestion.visibility = android.view.View.INVISIBLE
                } else {
                    binding.flashContainer.visibility = android.view.View.GONE
                    binding.cardQuestion.visibility = android.view.View.VISIBLE
                }
            }
        }

        viewModel.timeLeft.observe(this) { seconds ->
            binding.tvTime.text = "${seconds}s"
            binding.progressBar.progress = seconds
        }

        viewModel.gameFinished.observe(this) { result ->
            if (result != null) {
                com.braintrainer.app.util.MusicManager.playSFX(com.braintrainer.app.util.MusicManager.SFX.FINISH)
                showResultDialog(result)
            }
        }

        viewModel.currentGameType.observe(this) { type ->
            showIntroOverlay(type)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
        toneGenerator = null
    }

    private fun showResultDialog(result: com.braintrainer.app.game.PerformanceEvaluator.EvaluationResult) {
        val feedbackResId = when(result.feedback) {
            "FEEDBACK_EXCELLENT" -> R.string.feedback_excellent
            "FEEDBACK_GOOD" -> R.string.feedback_good
            "FEEDBACK_AVERAGE" -> R.string.feedback_average
            "FEEDBACK_POOR" -> R.string.feedback_poor
            "FEEDBACK_BAD" -> R.string.feedback_bad
            else -> R.string.feedback_bad
        }
    
        val dialogView = layoutInflater.inflate(R.layout.dialog_game_result, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        // Setup Views
        dialogView.findViewById<com.braintrainer.app.ui.views.ClashTextView>(R.id.tvResultGrade).text = result.grade
        dialogView.findViewById<com.braintrainer.app.ui.views.ClashTextView>(R.id.tvResultAge).text = getString(R.string.result_age, result.brainAge)
        dialogView.findViewById<com.braintrainer.app.ui.views.ClashTextView>(R.id.tvResultFeedback).text = getString(feedbackResId)
        
        dialogView.findViewById<com.braintrainer.app.ui.views.ClashButton>(R.id.btnResultBack).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private val pokerCardViews = mutableListOf<android.view.View>()

    private fun renderPokerGame(items: List<String>) {
        if (items.size < 4) return
        
        // Reset list
        pokerCardViews.clear()
        
        val holeCards = items[0].split(",")
        val flopCards = items[1].split(",")
        val turnCard = items[2]
        val riverCard = items[3]
        
        // 1. Render Hole Cards (Immediate)
        holeCards.forEach { cardCode ->
            val v = createCardView(cardCode)
            binding.llHoleCards.addView(v)
            pokerCardViews.add(v)
        }
        
        // 2. Prepare Community Cards
        val communityViews = mutableListOf<android.view.View>()
        
        flopCards.forEach { 
            val v = createCardView(it)
            communityViews.add(v)
            pokerCardViews.add(v) 
        }
        val tV = createCardView(turnCard)
        communityViews.add(tV)
        pokerCardViews.add(tV)
        
        val rV = createCardView(riverCard)
        communityViews.add(rV)
        pokerCardViews.add(rV)
        
        // Add all to layout but set INVISIBLE initially
        communityViews.forEach { 
            it.visibility = android.view.View.INVISIBLE
            binding.llCommunityCards.addView(it)
        }
        
        // 3. Animate Reveal
        val totalTime = intent.getLongExtra(EXTRA_FLASH_TIME, 4000L).coerceAtLeast(2000L)
        val step = totalTime / 4
        
        binding.llCommunityCards.postDelayed({
            if (communityViews.size >= 3) {
                communityViews[0].visibility = android.view.View.VISIBLE
                communityViews[1].visibility = android.view.View.VISIBLE
                communityViews[2].visibility = android.view.View.VISIBLE
                com.braintrainer.app.util.MusicManager.playSFX(com.braintrainer.app.util.MusicManager.SFX.CORRECT)
            }
        }, step)
        
        binding.llCommunityCards.postDelayed({
            if (communityViews.size >= 4) communityViews[3].visibility = android.view.View.VISIBLE
        }, step * 2)
        
        binding.llCommunityCards.postDelayed({
            if (communityViews.size >= 5) communityViews[4].visibility = android.view.View.VISIBLE
        }, step * 3)
    }

    private fun createCardView(cardCode: String): android.view.View {
        val rankRaw = cardCode.substring(0, 1)
        val suitRaw = cardCode.substring(1, 2)
        
        val rank = rankRaw.lowercase()
        val suit = suitRaw.lowercase()
        
        val resName = "card_${rank}_${suit}"
        val resId = resources.getIdentifier(resName, "drawable", packageName)
        
        if (resId != 0) {
            val iv = com.braintrainer.app.ui.views.ClashImageView(this)
            iv.setImageResource(resId)
            val params = android.widget.LinearLayout.LayoutParams(200, 300) 
            params.setMargins(6, 0, 6, 0)
            iv.layoutParams = params
            iv.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            iv.elevation = 0f
            iv.outlineProvider = null
            iv.stateListAnimator = null
            iv.showShadow = false
            return iv
        } else {
            val rankDisp = if (rankRaw == "T") "10" else rankRaw
            val suitSymbol = when(suitRaw) {
                "S" -> "♠"
                "H" -> "♥"
                "D" -> "♦"
                "C" -> "♣"
                else -> "?"
            }
            val color = if (suitRaw == "H" || suitRaw == "D") android.graphics.Color.RED else android.graphics.Color.BLACK
            
            val tv = com.braintrainer.app.ui.views.ClashTextView(this)
            tv.text = "$rankDisp\n$suitSymbol"
            tv.textSize = 36f
            tv.setTextColor(color)
            tv.gravity = android.view.Gravity.CENTER
            tv.setBackgroundResource(R.drawable.bg_clash_panel) 
            tv.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            
            val params = android.widget.LinearLayout.LayoutParams(200, 300)
            params.setMargins(6, 0, 6, 0)
            tv.layoutParams = params
            tv.elevation = 0f
            tv.outlineProvider = null
            tv.stateListAnimator = null
            tv.showShadow = false
            return tv
        }
    }

    companion object {
        private const val EXTRA_GAME_TYPE = "EXTRA_GAME_TYPE"
        private const val EXTRA_DIFFICULTY = "EXTRA_DIFFICULTY"
        private const val EXTRA_ROUNDS = "EXTRA_ROUNDS"
        private const val EXTRA_FLASH_TIME = "EXTRA_FLASH_TIME"
        private const val EXTRA_IS_PRACTICE = "EXTRA_IS_PRACTICE"

        fun start(context: Context, gameType: String, difficulty: String = "MEDIUM", rounds: Int = 10, flashTime: Long = 0L, isPractice: Boolean = false) {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra(EXTRA_GAME_TYPE, gameType)
            intent.putExtra(EXTRA_DIFFICULTY, difficulty)
            intent.putExtra(EXTRA_ROUNDS, rounds)
            intent.putExtra(EXTRA_FLASH_TIME, flashTime)
            intent.putExtra(EXTRA_IS_PRACTICE, isPractice)
            context.startActivity(intent)
        }
    }
}
