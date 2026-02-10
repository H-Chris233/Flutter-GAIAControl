/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.databinding.FragmentFeedbackFormBinding
import com.qualcomm.qti.gaiaclient.ui.Navigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackFormFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackFormBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFeedbackFormBinding.inflate(inflater)
        binding.send.setOnClickListener { onSend() }
        initTextInputLayout(binding.title.inputLayout, true)
        initTextInputLayout(binding.description.inputLayout, true)
        initTextInputLayout(binding.reporter.inputLayout, false)
        initTextInputLayout(binding.hardwareVersion.inputLayout, false)
        return binding.root
    }

    private fun initTextInputLayout(layout: TextInputLayout, required: Boolean) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(layout, s.toString(), required)
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        layout.editText?.addTextChangedListener(textWatcher)
        layout.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // covers empty required field when no modification was made
                validateInput(layout, layout.editText?.text.toString(), required)
            }
        }
    }

    private fun validateInput(layout: TextInputLayout, text: String, required: Boolean) {
        val error = getValidationError(text, layout.counterMaxLength, required)
        layout.error = error?.let { getString(it) }
        binding.send.isEnabled = isFormValid()
    }

    private fun isInputValid(layout: TextInputLayout, required: Boolean): Boolean {
        return getValidationError(layout.editText?.text.toString(), layout.counterMaxLength, required) == null
    }

    private fun getValidationError(text: String, maxLength: Int, required: Boolean): Int? {
        if (required && text.isBlank()) {
            return R.string.feedback_form_error_required
        }

        if (maxLength > 0 && text.length > maxLength) {
            return R.string.feedback_form_error_too_long
        }

        return null
    }

    private fun isFormValid(): Boolean {
        return isInputValid(binding.title.inputLayout, true)
                && isInputValid(binding.description.inputLayout, true)
                && isInputValid(binding.reporter.inputLayout, false)
                && isInputValid(binding.hardwareVersion.inputLayout, false)
    }

    private fun onSend() {
        val title: String = binding.title.input.text.toString()
        val description: String = binding.description.input.text.toString()
        val reporter: String = binding.reporter.input.text.toString()
        val hardwareVersion: String = binding.hardwareVersion.input.text.toString()
        Navigator.navigate(
            binding.root,
            FeedbackFormFragmentDirections.toSendFeedback(FeedbackForm(title, description, reporter, hardwareVersion))
        )
    }
}
