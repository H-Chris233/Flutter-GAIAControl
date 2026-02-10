/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet
import com.qualcomm.qti.gaiaclient.repository.Resource
import com.qualcomm.qti.gaiaclient.repository.ResourceType
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository
import com.qualcomm.qti.gaiaclient.repository.connection.Device
import com.qualcomm.qti.gaiaclient.repository.musicprocessing.MusicProcessingRepository
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MusicProcessingViewModel @Inject constructor(
    application: Application,
    private val connectionRepository: ConnectionRepository,
    private val musicProcessingRepository: MusicProcessingRepository
) : AndroidViewModel(application) {

    private var mViewData: StickyLiveData<MusicProcessingViewData> =
        StickyLiveData()

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
    }

    fun setObservers(owner: LifecycleOwner, dataObserver: Observer<MusicProcessingViewData>) {
        mViewData.observe(owner, dataObserver)
    }

    fun fetchData() {
        musicProcessingRepository.fetchData()
    }

    fun selectPresetAtIndex(index: Int) {
        val presets = mViewData.value?.presets ?: emptyList()

        if (index < 0 || index >= presets.size) return
        musicProcessingRepository.selectSet(presets[index])
    }

    fun setBandGain(band: Int, gain: Double) {
        musicProcessingRepository.setUserSetBandGain(band, gain)
    }

    fun setAllGains(gain: Double) {
        musicProcessingRepository.setAllGains(gain)
    }

    private fun onConnectionStateUpdated(resource: Resource<Device?, BluetoothStatus?>?) {
        val state = resource?.data?.state

        val oldData = mViewData.value ?: MusicProcessingViewData()
        val newData = MusicProcessingViewData(
            state == ConnectionState.CONNECTED,
            oldData.eqEnabled,
            oldData.presets,
            oldData.selectedPreset,
            oldData.userBands
        )
        mViewData.postValue(newData)
    }

    private fun onState(resource: Resource<EQState?, Reason?>?) {
        if (resource == null) return

        val state = resource.data
        when (resource.type) {
            ResourceType.DATA -> {
                val oldData = mViewData.value ?: MusicProcessingViewData()
                val newData = MusicProcessingViewData(
                    oldData.deviceConnected,
                    state == EQState.PRESENT,
                    oldData.presets,
                    oldData.selectedPreset,
                    oldData.userBands
                )
                mViewData.postValue(newData)
            }
            ResourceType.ERROR -> {
            }
        }
    }

    private fun onAvailablePreSets(resource: Resource<List<PreSet>?, Reason>?) {
        if (resource == null) return

        val presets = resource.data ?: emptyList()
        when (resource.type) {
            ResourceType.DATA -> {
                val oldData = mViewData.value ?: MusicProcessingViewData()
                val newData = MusicProcessingViewData(
                    oldData.deviceConnected,
                    oldData.eqEnabled,
                    presets,
                    oldData.selectedPreset,
                    oldData.userBands
                )
                mViewData.postValue(newData)
            }
            ResourceType.ERROR -> {
            }
        }
    }

    private fun onSelectedSet(resource: Resource<PreSet?, Reason?>?) {
        if (resource == null) return

        val preset = resource.data
        when (resource.type) {
            ResourceType.DATA -> {
                val oldData = mViewData.value ?: MusicProcessingViewData()
                val newData = MusicProcessingViewData(
                    oldData.deviceConnected,
                    oldData.eqEnabled,
                    oldData.presets,
                    preset,
                    oldData.userBands
                )
                mViewData.postValue(newData)
            }
            ResourceType.ERROR -> {
            }
        }
    }

    private fun onUserSetConfiguration(resource: Resource<Set<BandInfo>?, Reason?>?) {
        if (resource == null) return

        val bands = resource.data ?: emptySet()
        when (resource.type) {
            ResourceType.DATA -> {
                val oldData = mViewData.value ?: MusicProcessingViewData()
                val newData = MusicProcessingViewData(
                    oldData.deviceConnected,
                    oldData.eqEnabled,
                    oldData.presets,
                    oldData.selectedPreset,
                    bands
                )
                mViewData.postValue(newData)
            }
            ResourceType.ERROR -> {
            }
        }
    }

    private inner class DataObservers : Observers() {
        private val connectedDeviceObserver = this@MusicProcessingViewModel::onConnectionStateUpdated
        private val eqStateObserver = this@MusicProcessingViewModel::onState
        private val preSetsObserver = this@MusicProcessingViewModel::onAvailablePreSets
        private val selectedSetObserver = this@MusicProcessingViewModel::onSelectedSet
        private val userSetConfigurationObserver = this@MusicProcessingViewModel::onUserSetConfiguration

        override fun registerObservers() {
            connectionRepository.connectedDeviceLiveData.observeForever(connectedDeviceObserver)
            musicProcessingRepository.eqStateLiveData.observeForever(eqStateObserver)
            musicProcessingRepository.preSetsLiveData.observeForever(preSetsObserver)
            musicProcessingRepository.selectedSetLiveData.observeForever(selectedSetObserver)
            musicProcessingRepository.userSetConfigurationLiveData.observeForever(userSetConfigurationObserver)
        }

        override fun unregisterObservers() {
            connectionRepository.connectedDeviceLiveData.removeObserver(connectedDeviceObserver)
            musicProcessingRepository.eqStateLiveData.removeObserver(eqStateObserver)
            musicProcessingRepository.preSetsLiveData.removeObserver(preSetsObserver)
            musicProcessingRepository.selectedSetLiveData.removeObserver(selectedSetObserver)
            musicProcessingRepository.userSetConfigurationLiveData.removeObserver(userSetConfigurationObserver)
        }
    }
}
