package com.braintrainer.app.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.braintrainer.app.R
import com.braintrainer.app.databinding.DialogClashMessageBinding

object DialogHelper {

    fun showMessageDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String? = null,
        negativeText: String? = null,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ) {
        val inflater = LayoutInflater.from(context)
        val binding = DialogClashMessageBinding.inflate(inflater)
        
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()
            
        binding.tvDialogTitle.text = title
        binding.tvDialogMessage.text = message
        
        binding.btnPositive.text = positiveText ?: context.getString(R.string.dialog_btn_ok)
        binding.btnPositive.setOnClickListener {
            onPositive?.invoke()
            dialog.dismiss()
        }
        
        if (negativeText != null) {
            binding.btnNegative.visibility = View.VISIBLE
            binding.btnNegative.text = negativeText
            binding.btnNegative.setOnClickListener {
                onNegative?.invoke()
                dialog.dismiss()
            }
        } else {
            binding.btnNegative.visibility = View.GONE
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
