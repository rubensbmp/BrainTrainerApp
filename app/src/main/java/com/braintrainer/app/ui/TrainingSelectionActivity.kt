package com.braintrainer.app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintrainer.app.R
import com.braintrainer.app.databinding.ActivityTrainingSelectionBinding
import com.braintrainer.app.databinding.ItemGameSelectionBinding

data class GameOption(val name: String, val description: String, val typeCode: String, val iconRes: Int)

class TrainingSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrainingSelectionBinding
    private lateinit var viewModel: TrainingViewModel

        val games = listOf(
        GameOption("Tabuada", "Pratique a tabuada", "MULTIPLICATION", R.drawable.ic_game_multiplication),
        GameOption("Cálculo Simples", "Matemática básica rápida", "CALCULATION", R.drawable.ic_game_calc),
        GameOption("Símbolo Faltando", "Descubra o operador correto", "LOGIC_SYMBOL", R.drawable.ic_game_logic),
        GameOption("Poker Hand", "Identifique a melhor mão", "POKER_HAND", R.drawable.ic_game_poker),
        GameOption("Maior Número", "Reflexo e comparação de valores", "REFLEX_GREATEST", R.drawable.ic_game_reflex),
        GameOption("Bandeiras", "Adivinhe o país pela bandeira", "FLAG_QUIZ", R.drawable.ic_game_flags),
        GameOption("Mapas", "Reconheça o país pelo formato", "MAP_QUIZ", R.drawable.ic_game_map),
        GameOption("Memória Numérica", "Memorize o número que pisca", "NUMBER_MEMORY", R.drawable.ic_game_memory),
        GameOption("Contagem Visual", "Conte os círculos vermelhos", "VISUAL_COUNT", R.drawable.ic_game_visual)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = androidx.lifecycle.ViewModelProvider(this)[TrainingViewModel::class.java]

        setupRecyclerView()
        
        viewModel.bestScores.observe(this) { scores ->
            (binding.rvGames.adapter as? GameAdapter)?.updateScores(scores)
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

    private fun setupRecyclerView() {
        binding.rvGames.layoutManager = LinearLayoutManager(this)
        binding.rvGames.adapter = GameAdapter(games) { game ->
            if (game.typeCode == "MULTIPLICATION") {
                MultiplicationSelectionActivity.start(this)
            } else {
                DifficultySelectionActivity.start(this, game.typeCode, game.name)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = android.content.Intent(context, TrainingSelectionActivity::class.java)
            context.startActivity(intent)
        }
    }

    class GameAdapter(
        private val items: List<GameOption>,
        private val onClick: (GameOption) -> Unit
    ) : RecyclerView.Adapter<GameAdapter.ViewHolder>() {

        private var scores: Map<String, Int> = emptyMap()

        fun updateScores(newScores: Map<String, Int>) {
            scores = newScores
            notifyDataSetChanged()
        }

        inner class ViewHolder(val binding: ItemGameSelectionBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemGameSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvGameName.text = item.name
            holder.binding.tvGameDesc.text = item.description
            holder.binding.ivIcon.setImageResource(item.iconRes)
            
            val best = scores[item.typeCode]
            if (best != null) {
                holder.binding.tvBestScore.text = "Melhor: ${best} anos"
                holder.binding.tvBestScore.visibility = android.view.View.VISIBLE
            } else {
                holder.binding.tvBestScore.visibility = android.view.View.GONE
            }
            
            holder.binding.rootGameItem.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
