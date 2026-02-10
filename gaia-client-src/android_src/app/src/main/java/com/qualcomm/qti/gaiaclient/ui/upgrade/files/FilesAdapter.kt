/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qualcomm.qti.gaiaclient.databinding.FileItemBinding
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback

internal class FilesAdapter(private val onSelected: (FileViewData) -> Unit) :
    ListAdapter<FileViewData, RecyclerView.ViewHolder>(ListAdapterDataItemCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemBinding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(itemBinding) { onSelected(it) }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as FileViewHolder).bind(item as FileViewData)
    }
}

private class FileViewHolder constructor(
    private val binding: FileItemBinding,
    private val callback: (FileViewData) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        with(binding.fileTags) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    fun bind(file: FileViewData) {
        binding.file = file
        binding.root.setOnClickListener {
            callback(file)
        }
    }
}
