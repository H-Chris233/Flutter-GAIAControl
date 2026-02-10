/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qualcomm.qti.gaiaclient.databinding.StatisticsCategoryItemBinding
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories

private const val DEFAULT_ITEM_TYPE = 0

class StatisticsCategoriesAdapter(private val onSelected: (categoryInfo: StatisticsCategories) -> Unit) :
    ListAdapter<CategoryViewData, StatisticsCategoriesAdapter.CategoryViewHolder>(ListAdapterDataItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = StatisticsCategoryItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, onSelected)
    }

    override fun getItemViewType(position: Int): Int {
        return DEFAULT_ITEM_TYPE // all items have the same view
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode().toLong()
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item: CategoryViewData = getItem(position)
        holder.bind(item)
    }

    class CategoryViewHolder(
        private val binding: StatisticsCategoryItemBinding,
        private val onSelected: ((StatisticsCategories) -> Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewData: CategoryViewData?) {
            binding.data = viewData
            // data binding causes the indicator to flash when changing the visibility -> setting it up directly
            binding.recordingIndicator.visibility = if (viewData?.isRecording == true) View.VISIBLE else View.INVISIBLE
        }

        init {
            binding.root.setOnClickListener {
                val data: CategoryViewData = this.binding.data!!
                onSelected(data.categoryInfo)
            }
        }
    }

    init {
        setHasStableIds(true)
    }
}
