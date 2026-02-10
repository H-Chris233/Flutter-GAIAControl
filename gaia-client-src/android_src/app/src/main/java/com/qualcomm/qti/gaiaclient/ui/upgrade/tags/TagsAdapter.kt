/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.tags

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.qualcomm.qti.gaiaclient.databinding.TagItemBinding

class TagsAdapter(values: Array<String>) : Adapter<TagViewHolder>() {

    private var tags: Array<Tag> = Tag.valuesFrom(values)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val itemBinding = TagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bind(tag)
    }
}

class TagViewHolder(private val binding: TagItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(tag: Tag) {
        binding.data = TagViewData(binding.content.context, tag)
    }
}
