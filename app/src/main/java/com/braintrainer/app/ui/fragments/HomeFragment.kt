package com.braintrainer.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braintrainer.app.databinding.FragmentHomeBinding
import com.braintrainer.app.R
import com.braintrainer.app.ui.fragments.HomeViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = androidx.lifecycle.ViewModelProvider(this)[HomeViewModel::class.java]
        
        setupUserObserver()
        setupDailyResultObserver()
        
        binding.btnStartDaily.setOnClickListener {
             if (viewModel.dailyResult.value != null) {
                 // Already done today
                 showDailyLimitDialog()
             } else {
                 // Start Daily Test (Real)
                 val diff = viewModel.suggestedDifficulty.value ?: "MEDIUM"
                 com.braintrainer.app.ui.game.GameActivity.start(requireContext(), "DAILY_TEST", difficulty = diff, rounds = 32, flashTime = 1250L)
             }
        }
        
        binding.btnPractice.setOnClickListener {
             // Practice Mode
             val diff = viewModel.suggestedDifficulty.value ?: "MEDIUM"
             com.braintrainer.app.ui.game.GameActivity.start(requireContext(), "DAILY_TEST", difficulty = diff, rounds = 32, flashTime = 1250L, isPractice = true)
        }
        
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshDailyResult()
    }
    
    private fun setupDailyResultObserver() {
        viewModel.dailyResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                binding.tvBrainAge.text = "${result.brainAge}"
                binding.tvAvgGrade.text = result.grade
                
                // Keep enabled so user can click to see the limit message
                binding.btnStartDaily.isEnabled = true
                binding.btnStartDaily.text = getString(R.string.daily_btn_done)
                binding.btnStartDaily.setBackgroundResource(R.drawable.bg_clash_button_grey)
                binding.btnStartDaily.backgroundTintList = null
                
                binding.tvDailyStatus.text = getString(R.string.daily_status_done)
                binding.tvDailyStatus.setTextColor(android.graphics.Color.GRAY)
                
            } else {
                binding.tvBrainAge.text = "--"
                binding.tvAvgGrade.text = "--"
                
                // Enable Daily Test Button
                binding.btnStartDaily.isEnabled = true
                binding.btnStartDaily.text = getString(R.string.daily_test_btn)
                binding.btnStartDaily.setBackgroundResource(R.drawable.bg_clash_button_yellow_clash)
                binding.btnStartDaily.backgroundTintList = null
                
                binding.tvDailyStatus.text = getString(R.string.daily_status_ready)
                binding.tvDailyStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }
    }
    
    private fun setupUserObserver() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name
                
                val iconRes = when(user.avatarId) {
                    1 -> R.drawable.avatar_1
                    2 -> R.drawable.avatar_2
                    3 -> R.drawable.avatar_3
                    4 -> R.drawable.avatar_4
                    5 -> R.drawable.avatar_5
                    6 -> R.drawable.avatar_6
                    7 -> R.drawable.avatar_7
                    8 -> R.drawable.avatar_8
                    9 -> R.drawable.avatar_9
                    10 -> R.drawable.avatar_10
                    11 -> R.drawable.avatar_11
                    12 -> R.drawable.avatar_12
                    13 -> R.drawable.avatar_13
                    14 -> R.drawable.avatar_14
                    15 -> R.drawable.avatar_15
                    else -> R.drawable.avatar_1
                }
                binding.ivAvatar.setImageResource(iconRes)
            } else {
                binding.tvUserName.text = "Convidado"
            }
        }
    }
    
    private fun showEditProfileDialog() {
        val currentUser = viewModel.user.value
        val name = currentUser?.name ?: "Convidado"
        val avatar = currentUser?.avatarId ?: 0
        
        val dialog = ProfileEditDialogFragment(name, avatar) { newName, newAvatar ->
            viewModel.updateUser(newName, newAvatar)
        }
        dialog.show(parentFragmentManager, "ProfileEdit")
    }

    private fun showDailyLimitDialog() {
        val remaining = viewModel.getTimeRemainingUntilNextDay()
        com.braintrainer.app.util.DialogHelper.showMessageDialog(
            context = requireContext(),
            title = getString(R.string.daily_limit_title),
            message = getString(R.string.daily_limit_message, remaining),
            positiveText = getString(R.string.dialog_btn_ok)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
