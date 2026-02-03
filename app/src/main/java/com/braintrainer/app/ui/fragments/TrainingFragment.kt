package com.braintrainer.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.braintrainer.app.R
import com.braintrainer.app.databinding.FragmentTrainingBinding
import com.braintrainer.app.ui.DifficultySelectionActivity
import com.braintrainer.app.ui.GameOption
import com.braintrainer.app.ui.TrainingSelectionActivity.GameAdapter

class TrainingFragment : Fragment() {
    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val games = listOf(
            GameOption(R.string.game_multiplication, R.string.desc_multiplication, "MULTIPLICATION", R.drawable.ic_game_multiplication),
            GameOption(R.string.game_calc, R.string.desc_calc, "CALCULATION", R.drawable.ic_game_calc),
            GameOption(R.string.game_logic, R.string.desc_logic, "LOGIC_SYMBOL", R.drawable.ic_game_logic),
            GameOption(R.string.game_poker, R.string.desc_poker, "POKER_HAND", R.drawable.ic_game_poker),
            GameOption(R.string.game_reflex, R.string.desc_reflex, "REFLEX_GREATEST", R.drawable.ic_game_reflex),
            GameOption(R.string.game_flags, R.string.desc_flags, "FLAG_QUIZ", R.drawable.ic_game_flags),
            GameOption(R.string.game_map, R.string.desc_map, "MAP_QUIZ", R.drawable.ic_game_map),
            GameOption(R.string.game_memory, R.string.desc_memory, "NUMBER_MEMORY", R.drawable.ic_game_memory),
            GameOption(R.string.game_visual, R.string.desc_visual, "VISUAL_COUNT", R.drawable.ic_game_visual)
        )
        
        binding.rvTraining.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTraining.adapter = GameAdapter(games) { game ->
            if (game.typeCode == "MULTIPLICATION") {
                com.braintrainer.app.ui.MultiplicationSelectionActivity.start(requireContext())
            } else {
                DifficultySelectionActivity.start(requireContext(), game.typeCode, getString(game.nameRes))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
