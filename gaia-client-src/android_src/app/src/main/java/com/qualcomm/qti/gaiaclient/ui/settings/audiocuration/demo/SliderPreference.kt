/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.databinding.PreferenceSliderBinding

data class SliderViewData(var progress: Int, var label: String)

class SliderPreference : Preference {
    var binding: PreferenceSliderBinding? = null

    var onProgressChanged: (progress: Int) -> Unit = {}

    var data: SliderViewData = SliderViewData(0, "")
        set(value) {
            field = value
            binding?.let { it.data = value }
        }

    var max = 100 // default as percentage
        set(value) {
            field = value
            binding?.let { it.max = value }
        }

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context!!, attrs, defStyleAttr, defStyleRes)

    init {
        layoutResource = R.layout.preference_slider
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val binding: PreferenceSliderBinding = DataBindingUtil.bind(holder.itemView) ?: return
        this.binding = binding
        binding.data = data
        binding.max = max
        binding.slider.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) onProgressChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}
