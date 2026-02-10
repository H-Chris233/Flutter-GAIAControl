/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.DataBindingUtil
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.databinding.PreferenceMediaBinding
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaEvent

class MediaPreference : ListPreference {

    var onEvent: (MediaEvent) -> Unit = {}

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context!!, attrs, defStyleAttr, defStyleRes)

    init {
        layoutResource = R.layout.preference_media
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val binding = DataBindingUtil.bind<PreferenceMediaBinding>(holder.itemView)
        if (binding != null) {
            binding.mediaPlayback.prefPlay.setOnClickListener { onEvent(MediaEvent.PLAY) }
            binding.mediaPlayback.prefPause.setOnClickListener { onEvent(MediaEvent.PAUSE) }
            binding.mediaPlayback.prefNextTrack.setOnClickListener { onEvent(MediaEvent.NEXT_TRACK) }
            binding.mediaPlayback.prefPreviousTrack.setOnClickListener { onEvent(MediaEvent.PREVIOUS_TRACK) }
            binding.mediaPlayback.prefVolumeDown.setOnClickListener { onEvent(MediaEvent.VOLUME_DOWN) }
            binding.mediaPlayback.prefVolumeDown.setOnLongClickListener { onEvent(MediaEvent.MUTE); true }
            binding.mediaPlayback.prefVolumeUp.setOnClickListener { onEvent(MediaEvent.VOLUME_UP) }
            binding.mediaPlayback.prefVolumeUp.setOnLongClickListener { onEvent(MediaEvent.UNMUTE); true }
            binding.root.isClickable = false
        }
    }
}
