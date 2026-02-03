package com.braintrainer.app.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import com.braintrainer.app.R
import com.braintrainer.app.databinding.DialogProfileEditBinding

class ProfileEditDialogFragment(
    private val currentName: String,
    private val currentAvatarId: Int,
    private val onSave: (String, Int) -> Unit
) : DialogFragment() {

    private var selectedAvatarId = currentAvatarId

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogProfileEditBinding.inflate(LayoutInflater.from(context))
        
        binding.etName.setText(currentName)
        
        // Setup Avatar Selection
        // Setup Avatar Selection
        val avatars = listOf(
            binding.av1 to R.drawable.avatar_1, 
            binding.av2 to R.drawable.avatar_2, 
            binding.av3 to R.drawable.avatar_3, 
            binding.av4 to R.drawable.avatar_4,
            binding.av5 to R.drawable.avatar_5,
            binding.av6 to R.drawable.avatar_6,
            binding.av7 to R.drawable.avatar_7,
            binding.av8 to R.drawable.avatar_8,
            binding.av9 to R.drawable.avatar_9,
            binding.av10 to R.drawable.avatar_10,
            binding.av11 to R.drawable.avatar_11,
            binding.av12 to R.drawable.avatar_12,
            binding.av13 to R.drawable.avatar_13,
            binding.av14 to R.drawable.avatar_14,
            binding.av15 to R.drawable.avatar_15
        )
        
        // Highlight current
        avatars.forEach { (view, id) -> 
            // Mapping tag/id logic is simplified here. 
            // We just use the 'tag' property in XML: 1, 2, 3, 0
            val tagId = view.tag.toString().toInt()
            
            if (tagId == currentAvatarId) {
                view.alpha = 1.0f
                view.setBackgroundResource(R.drawable.bg_clash_panel) // Highlight
            } else {
                view.alpha = 0.5f
                view.background = null
            }
            
            view.setOnClickListener {
                selectedAvatarId = tagId
                // Visual update
                avatars.forEach { (v, _) -> 
                    v.alpha = 0.6f
                    v.background = null
                }
                view.alpha = 1.0f
                view.setBackgroundResource(R.drawable.bg_clash_panel)
            }
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                onSave(newName, selectedAvatarId)
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
    }
}
