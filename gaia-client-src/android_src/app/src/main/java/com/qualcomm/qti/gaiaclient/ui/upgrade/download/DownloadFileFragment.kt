/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.qualcomm.qti.gaiaclient.databinding.FragmentDownloadFileBinding
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeOptions
import com.qualcomm.qti.gaiaclient.ui.Navigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadFileFragment : Fragment() {

    private lateinit var binding: FragmentDownloadFileBinding
    private val viewModel: DownloadViewModel by viewModels()

    private val backPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            viewModel.cancelDownload()
            navigateBack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, backPressedCallback)

        val fragmentArgs = arguments ?: return
        val file: FileData = DownloadFileFragmentArgs.fromBundle(fragmentArgs).file
        val options: UpgradeOptions = DownloadFileFragmentArgs.fromBundle(fragmentArgs).options
        viewModel.setData(file, options)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDownloadFileBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.action.observe(viewLifecycleOwner) { onClick(it) }
    }

    private fun onClick(action: Action) {
        when (action) {
            is Action.StartUpgrade -> Navigator.navigate(binding.root, DownloadFileFragmentDirections.toUpgrade())
            is Action.TryAgain -> {} // unexpected
        }
    }

    private fun navigateBack() {
        Navigator.navigateBack(binding.root)
    }

}
