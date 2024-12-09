package com.example.interviewcontent.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.lifecycle.ViewModelProvider
import com.example.interviewcontent.CalanderActivity
import com.example.interviewcontent.viewmodel.CalanderViewModel
import com.example.interviewcontent.databinding.BottomsheetTaskBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class TaskBottomSheetDialog(
    context: Context,
    private val viewModel: CalanderViewModel,
    private val onTaskAdded: (String, String) -> Unit
) : BottomSheetDialog(context) {

    private lateinit var binding: BottomsheetTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomsheetTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setHalfHeight()

        binding.btnDone.setOnClickListener {
            val task = binding.etTask.text.toString()
            val title = binding.etTitle.text.toString()
            if (title.isNotEmpty()) {

                onTaskAdded(task, title)
                dismiss()
            }
        }
    }

    private fun setHalfHeight() {
        // Post-layout adjustments
        binding.root.post {
            val parent = binding.root.parent as ViewGroup
            val behavior = BottomSheetBehavior.from(parent)
            val halfScreenHeight = (binding.root.resources.displayMetrics.heightPixels * 0.5).toInt()

            behavior.peekHeight = halfScreenHeight
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}

