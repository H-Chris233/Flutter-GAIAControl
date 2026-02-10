/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.qualcomm.qti.gaiaclient.databinding.FragmentFeedbackBinding
import com.qualcomm.qti.gaiaclient.ui.Navigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SendFeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private val viewModel: SendFeedbackViewModel by viewModels()

    private val backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, backPressedCallback)
        val fragmentArgs = arguments ?: return
        viewModel.setContent(SendFeedbackFragmentArgs.fromBundle(fragmentArgs).form)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFeedbackBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.action.observe(viewLifecycleOwner) { onClick(it) }
        sendFeedback()
    }

    private fun sendFeedback() {
        viewModel.sendFeedback()
    }

    private fun onClick(action: Action) {
        when (action) {
            is Action.CopyLink -> copyToClipboard(viewModel.link)
            is Action.CopyFeedback -> copyToClipboard(viewModel.content.value?.toClipboard(context))
            is Action.Done -> navigateBack()
            is Action.TryAgain -> {} // unexpected
        }
    }

    private fun copyToClipboard(text: String?) {
        val content: String = text ?: return
        val activity = activity ?: return
        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData: ClipData = ClipData.newPlainText(content, content)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun navigateBack() {
        Navigator.navigate(binding.root, SendFeedbackFragmentDirections.back())
    }

}
