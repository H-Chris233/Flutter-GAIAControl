/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.settings.main

import android.app.Application
import android.content.Context
import androidx.collection.ArrayMap
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures.UserFeatures
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import com.qualcomm.qti.gaiaclient.repository.service.ServiceAvailabilityImpl.isAvailable
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository
import com.qualcomm.qti.gaiaclient.repository.voiceui.VoiceUIRepository
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainSettingsViewModel @Inject constructor(
    application: Application,
    private val connectionRepository: ConnectionRepository,
    private val deviceInfoRepository: DeviceInformationRepository,
    private val featuresRepository: FeaturesRepository,
    private val voiceUIRepository: VoiceUIRepository,
    private val systemRepository: SystemRepository
) : SettingsViewModel<MainSettings, MainSettingsViewData>(application, connectionRepository) {

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
    }

    override fun initViewData(context: Context): MainSettingsViewData {
        return MainSettingsViewData(context)
    }

    override fun onConnectionStateUpdated(state: ConnectionState) {
        val connected = state == ConnectionState.CONNECTED
        for (key in MainSettings.values()) {
            if (key.requireConnected) {
                setSettingEnable(key, connected)
            }
        }
    }

    fun onResumed() {
        val context = getApplication<Application>().applicationContext
        systemRepository.checkAndroidDeveloperModeState(context)
        setSettingEnable(MainSettings.FEEDBACK, isAvailable(context))
    }

    fun disconnect() {
        connectionRepository.disconnect(getApplication())
    }

    fun setAssistant(assistant: VoiceAssistant) {
        voiceUIRepository.setAssistant(getApplication(), assistant)
    }

    private fun onSupportedFeatures(supportedFeatures: ArrayMap<QTILFeature, Int>?) {
        // get the data from the result
        val features = supportedFeatures?.keys ?: emptySet()

        // update settings visibilities
        updateSettingSupport(features)

        // fetch data for supported features
        val context = getApplication<Application>().applicationContext
        fetchData(context, features)
    }

    private fun updateSettingSupport(features: Set<QTILFeature>) {
        val visibilities = ArrayMap<MainSettings, Boolean>()
        visibilities[MainSettings.ANC] = features.contains(QTILFeature.ANC)
        visibilities[MainSettings.AUDIO_CURATION] = features.contains(QTILFeature.AUDIO_CURATION)
        visibilities[MainSettings.MUSIC_PROCESSING] = features.contains(QTILFeature.MUSIC_PROCESSING)
        visibilities[MainSettings.VOICE_UI] = features.contains(QTILFeature.VOICE_UI)
        visibilities[MainSettings.UPGRADE] = features.contains(QTILFeature.UPGRADE)
        visibilities[MainSettings.LOGS] =
            systemRepository.isAndroidDeveloperModeOn || features.contains(QTILFeature.DEBUG)
        visibilities[MainSettings.EARBUD_FIT] = features.contains(QTILFeature.EARBUD_FIT)
        visibilities[MainSettings.HANDSET_SERVICE] = features.contains(QTILFeature.HANDSET_SERVICE)
        visibilities[MainSettings.VOICE_PROCESSING] = features.contains(QTILFeature.VOICE_PROCESSING)
        visibilities[MainSettings.GESTURES] = features.contains(QTILFeature.GESTURE_CONFIGURATION)
        visibilities[MainSettings.STATISTICS] = features.contains(QTILFeature.STATISTICS)
        setVisibilities(visibilities)
    }

    private fun fetchData(context: Context, features: Set<QTILFeature>) {
        if (features.contains(QTILFeature.VOICE_UI)) {
            voiceUIRepository.fetchSelectedAssistant(context)
            voiceUIRepository.fetchSupportedAssistants(context)
        }
        if (features.contains(QTILFeature.BASIC)) {
            deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.USER_FEATURES)
        }
    }

    private fun updateLogSupport(isAndroidDeveloperModeOn: Boolean) {
        setSettingVisibility(
            MainSettings.LOGS,
            isAndroidDeveloperModeOn || featuresRepository.isFeatureAvailable(QTILFeature.DEBUG)
        )
    }

    private fun updateUserFeatures(features: UserFeatures) {
        val label = buildApplicationFeaturesLabel(features)
        setSettingVisibility(MainSettings.DEVICE_FEATURES, label.isNotEmpty())
        setSettingSummary(MainSettings.DEVICE_FEATURES, label)
    }

    private fun updateSelectedAssistant(assistant: VoiceAssistant?) {
        val summary = assistant?.getLabel(getApplication()) ?: ""
        setSettingSummary(MainSettings.VOICE_UI, summary)
        val value = assistant?.toString() ?: ""
        setSettingValue(MainSettings.VOICE_UI, value)
    }

    private fun updateAssistants(assistants: List<VoiceAssistant>) {
        setSettingList(MainSettings.VOICE_UI, getEntries(assistants))
    }

    private fun getEntries(assistants: List<VoiceAssistant>): Pair<Array<String>, Array<String>> {
        if (assistants.isEmpty()) return Pair(emptyArray(), emptyArray())
        val entries = assistants.map { it.getLabel(getApplication()) }.toTypedArray()
        val values = assistants.map { it.value.toString() }.toTypedArray()
        return Pair(entries, values)
    }

    private fun buildApplicationFeaturesLabel(userFeatures: UserFeatures?): String {
        if (userFeatures == null) {
            return ""
        }
        val builder = StringBuilder()
        val appFeatures = userFeatures.applicationFeatureSet
        val features = appFeatures.features ?: return ""
        if (features.isEmpty()) {
            return ""
        }
        for ((i, feature) in appFeatures.features.withIndex()) {
            if (i != 0) {
                builder.append("\n")
            }
            builder.append(feature.data)
        }
        return builder.toString()
    }

    private inner class DataObservers : Observers() {
        private val featuresObserver = this@MainSettingsViewModel::onSupportedFeatures
        private val userFeaturesObserver = this@MainSettingsViewModel::updateUserFeatures
        private val androidDeveloperModeObserver = this@MainSettingsViewModel::updateLogSupport
        private val assistantsObserver = this@MainSettingsViewModel::updateAssistants
        private val selectedAssistantObserver = this@MainSettingsViewModel::updateSelectedAssistant

        override fun registerObservers() {
            featuresRepository.featuresLiveData.observeForever(featuresObserver)
            deviceInfoRepository.userFeatures.observeForever(userFeaturesObserver)
            systemRepository.androidDeveloperModeLiveData.observeForever(androidDeveloperModeObserver)
            voiceUIRepository.assistants.observeForever(assistantsObserver)
            voiceUIRepository.selectedAssistant.observeForever(selectedAssistantObserver)
        }

        override fun unregisterObservers() {
            featuresRepository.featuresLiveData.removeObserver(featuresObserver)
            deviceInfoRepository.userFeatures.removeObserver(userFeaturesObserver)
            systemRepository.androidDeveloperModeLiveData.removeObserver(androidDeveloperModeObserver)
            voiceUIRepository.assistants.removeObserver(assistantsObserver)
            voiceUIRepository.selectedAssistant.removeObserver(selectedAssistantObserver)
        }
    }
}

private fun VoiceAssistant.getLabel(context: Context?): String {
    if (context == null) return ""
    return when (this) {
        VoiceAssistant.None -> context.getString(R.string.assistant_none)
        VoiceAssistant.AMA -> context.getString(R.string.assistant_ama)
        VoiceAssistant.GAA -> context.getString(R.string.assistant_gaa)
        VoiceAssistant.Reserved -> context.getString(R.string.assistant_reserved)
        is VoiceAssistant.Unknown -> context.getString(R.string.assistant_unknown_format, value)
    }
}
