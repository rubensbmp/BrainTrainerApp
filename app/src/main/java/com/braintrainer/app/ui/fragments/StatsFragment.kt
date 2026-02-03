package com.braintrainer.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braintrainer.app.R
import com.braintrainer.app.databinding.FragmentStatsBinding
import com.braintrainer.app.databinding.ItemStatHistoryBinding
import com.braintrainer.app.ui.stats.StatsViewModel
import com.braintrainer.app.data.local.GameResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: StatsViewModel
    private val adapter = StatsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = androidx.lifecycle.ViewModelProvider(this)[StatsViewModel::class.java]
        
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }
    
    private fun setupListeners() {
        binding.btnClearHistory.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_confirm_title))
                .setMessage(getString(R.string.dialog_confirm_clear_history))
                .setPositiveButton(getString(R.string.dialog_btn_yes)) { _, _ -> 
                    viewModel.clearHistory()
                }
                .setNegativeButton(getString(R.string.dialog_btn_cancel), null)
                .show()
        }
    }
    
    private fun setupRecyclerView() {
        binding.rvStats.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStats.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.averageBrainAge.observe(viewLifecycleOwner) { age ->
            binding.tvAvgAge.text = age
        }
        
        viewModel.totalGames.observe(viewLifecycleOwner) { total ->
            binding.tvTotalGames.text = total.toString()
        }
        
        viewModel.recentHistory.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        
        viewModel.dailyTestHistory.observe(viewLifecycleOwner) { list ->
           val data = list.mapNotNull { 
               if (it.brainAge != null) it.date to it.brainAge else null 
           }
           binding.lineChart.setData(data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    inner class StatsAdapter : RecyclerView.Adapter<StatsAdapter.ViewHolder>() {
        private var items: List<GameResult> = emptyList()
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        
        fun submitList(newItems: List<GameResult>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        inner class ViewHolder(val binding: ItemStatHistoryBinding) : RecyclerView.ViewHolder(binding.root)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            
            holder.binding.tvGameTitle.text = getFriendlyName(item.gameType)
            holder.binding.tvDate.text = dateFormat.format(Date(item.date))
            holder.binding.tvGrade.text = item.grade
            
            // Icon
            // Icon
            val iconRes = when(item.gameType) {
                "CALCULATION" -> R.drawable.ic_game_calc
                "LOGIC_SYMBOL" -> R.drawable.ic_game_logic
                "REFLEX_GREATEST" -> R.drawable.ic_game_reflex
                "FLAG_QUIZ" -> R.drawable.ic_game_flags
                "MAP_QUIZ" -> R.drawable.ic_game_map
                "NUMBER_MEMORY" -> R.drawable.ic_game_memory
                "VISUAL_COUNT" -> R.drawable.ic_game_visual
                "DAILY_TEST" -> R.drawable.ic_daily_test_new
                "MULTIPLICATION" -> R.drawable.ic_game_calc // Fallback or new icon
                "POKER_HAND" -> R.drawable.ic_game_poker
                else -> R.drawable.ic_launcher_foreground
            }
            holder.binding.ivGameIcon.setImageResource(iconRes)
        }
        
        private fun getFriendlyName(type: String): String {
            val context = binding.root.context
            return when(type) {
                "CALCULATION" -> context.getString(R.string.game_calc)
                "LOGIC_SYMBOL" -> context.getString(R.string.game_logic)
                "REFLEX_GREATEST" -> context.getString(R.string.game_reflex)
                "FLAG_QUIZ" -> context.getString(R.string.game_flags)
                "MAP_QUIZ" -> context.getString(R.string.game_map)
                "NUMBER_MEMORY" -> context.getString(R.string.game_memory)
                "VISUAL_COUNT" -> context.getString(R.string.game_visual)
                "DAILY_TEST" -> context.getString(R.string.game_daily)
                "MULTIPLICATION" -> context.getString(R.string.game_multiplication)
                "POKER_HAND" -> context.getString(R.string.game_poker)
                else -> "Outro"
            }
        }
        
        override fun getItemCount() = items.size
    }
}
