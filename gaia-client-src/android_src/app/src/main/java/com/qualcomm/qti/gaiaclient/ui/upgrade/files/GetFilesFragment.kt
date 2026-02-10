/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.qualcomm.qti.gaiaclient.databinding.FragmentFilesBinding
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.ui.Navigator
import com.qualcomm.qti.gaiaclient.ui.common.MessageData
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class GetFilesFragment : Fragment() {

    private val viewModel: GetFilesViewModel by viewModels()
    private lateinit var binding: FragmentFilesBinding
    private val args: GetFilesFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFilesBinding.inflate(inflater, container, false)

        val adapter = FilesAdapter { downloadFile(it.file) }
        with(binding.devicesList) {
            val divider = DividerItemDecoration(context, (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(divider)
            setAdapter(adapter)
        }

        binding.swipeRefreshLayout.setOnRefreshListener { refreshList() }
        subscribeUI(adapter)

        refreshList()

        return binding.root
    }

    private fun subscribeUI(adapter: FilesAdapter) {
        val owner = viewLifecycleOwner
        viewModel.files.observe(owner) { adapter.submitList(it) }
        viewModel.isRefreshing.observe(owner) { updateRefreshingState(it) }
        viewModel.message.observe(owner) { updateMessage(it) }
    }

    private fun updateMessage(message: MessageData) {
        binding.message = message
    }

    private fun updateRefreshingState(isRefreshing: Boolean?) {
        binding.swipeRefreshLayout.isRefreshing = isRefreshing != null && isRefreshing
    }

    private fun downloadFile(file: FileData) {
        Navigator.navigate(binding.root, GetFilesFragmentDirections.toDownloadFile(file, args.options))
    }

    private fun refreshList() {
        viewModel.fetchFiles(args.filters)
    }
}
