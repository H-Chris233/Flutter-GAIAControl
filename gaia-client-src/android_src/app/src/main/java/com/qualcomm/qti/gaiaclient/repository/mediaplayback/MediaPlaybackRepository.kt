/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.mediaplayback

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface MediaPlaybackRepository {
    fun onEvent(event: MediaEvent)
}

abstract class MediaPlaybackRepositoryData : MediaPlaybackRepository

@Singleton
class MediaPlaybackRepositoryImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    MediaPlaybackRepositoryData() {

    override fun onEvent(event: MediaEvent) {
        when (event) {
            MediaEvent.PLAY -> dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY)
            MediaEvent.PAUSE -> dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE)
            MediaEvent.PREVIOUS_TRACK -> dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            MediaEvent.NEXT_TRACK -> dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
            MediaEvent.VOLUME_UP -> adjustVolume(AudioManager.ADJUST_RAISE)
            MediaEvent.VOLUME_DOWN -> adjustVolume(AudioManager.ADJUST_LOWER)
            MediaEvent.MUTE -> adjustVolume(AudioManager.ADJUST_MUTE)
            MediaEvent.UNMUTE -> adjustVolume(AudioManager.ADJUST_UNMUTE)
        }
    }

    private fun dispatchMediaKeyEvent(key: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val time = System.currentTimeMillis()
        val event = KeyEvent(time, time, KeyEvent.ACTION_DOWN, key, 0)
        audioManager.dispatchMediaKeyEvent(event)
    }

    private fun adjustVolume(key: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(key, 0)
    }
}
