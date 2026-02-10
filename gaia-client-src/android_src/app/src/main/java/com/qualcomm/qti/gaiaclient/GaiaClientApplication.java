/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient;

import android.app.Application;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCRepositoryLegacy;
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepository;
import com.qualcomm.qti.gaiaclient.repository.battery.BatteryRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository;
import com.qualcomm.qti.gaiaclient.repository.discovery.DiscoveryRepository;
import com.qualcomm.qti.gaiaclient.repository.earbudfit.EarbudFitRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.repository.handsetservice.HandsetServiceRepository;
import com.qualcomm.qti.gaiaclient.repository.logs.DeviceLogsRepository;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaPlaybackRepository;
import com.qualcomm.qti.gaiaclient.repository.musicprocessing.MusicProcessingRepository;
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepository;
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepository;
import com.qualcomm.qti.gaiaclient.repository.voiceprocessing.VoiceProcessingRepository;
import com.qualcomm.qti.gaiaclient.repository.voiceui.VoiceUIRepository;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class GaiaClientApplication extends Application {

    @Inject
    public DiscoveryRepository discoveryRepository;

    @Inject
    public FeaturesRepository featuresRepository;

    @Inject
    public ConnectionRepository connectionRepository;

    @Inject
    public DeviceInformationRepository deviceInfoRepository;

    @Inject
    public UpgradeRepository upgradeRepository;

    @Inject
    public VoiceUIRepository voiceUIRepository;

    @Inject
    public ANCRepositoryLegacy ancRepositoryLegacy;

    @Inject
    public AudioCurationRepository audioCurationRepository;

    @Inject
    public SystemRepository systemRepository;

    @Inject
    public DeviceLogsRepository deviceLogsRepository;

    @Inject
    public MusicProcessingRepository musicProcessingRepository;

    @Inject
    public EarbudFitRepository earbudFitRepository;

    @Inject
    public HandsetServiceRepository handsetServiceRepository;

    @Inject
    public VoiceProcessingRepository voiceProcessingRepository;

    @Inject
    public MediaPlaybackRepository mediaPlaybackRepository;

    @Inject
    public GestureConfigurationRepository gestureConfigurationRepository;

    @Inject
    public BatteryRepository batteryRepository;

    @Inject
    public StatisticsRepository statisticsRepository;

    @Inject
    public ServiceRepository serviceRepository;

    private final ConnectionSubscriber connectionSubscriber = new ConnectionSubscriber() {
        @Override
        public void onConnectionStateChanged(Device device, ConnectionState state) {
            if (state == ConnectionState.DISCONNECTED) {
                resetRepositories(false);
            }
        }

        @Override
        public void onConnectionError(Device device, BluetoothStatus reason) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // init all singletons

        // initialising core
        GaiaClientService.prepare(this);

        // initialising repositories
        featuresRepository.init();
        deviceInfoRepository.init();
        upgradeRepository.init();
        voiceUIRepository.init();
        ancRepositoryLegacy.init();
        systemRepository.init(this);
        deviceLogsRepository.init();
        musicProcessingRepository.init();
        audioCurationRepository.init();
        earbudFitRepository.init();
        handsetServiceRepository.init();
        voiceProcessingRepository.init();
        gestureConfigurationRepository.init();
        batteryRepository.init();
        statisticsRepository.init(featuresRepository);
        serviceRepository.init();

        // subscribe the connection subscriber to reset the repositories on disconnection
        GaiaClientService.getPublicationManager().subscribe(connectionSubscriber);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // releasing core
        GaiaClientService.getPublicationManager().unsubscribe(connectionSubscriber);
        GaiaClientService.release(this);
    }

    public void resetRepositories(boolean hardReset) {
        ancRepositoryLegacy.reset();
        audioCurationRepository.reset();
        earbudFitRepository.reset();
        featuresRepository.reset();
        handsetServiceRepository.reset();
        deviceLogsRepository.reset(this);
        musicProcessingRepository.reset();
        voiceUIRepository.reset();
        voiceProcessingRepository.reset();
        gestureConfigurationRepository.reset();
        batteryRepository.reset();
        statisticsRepository.reset(hardReset);
        serviceRepository.reset();

        if (hardReset) {
            deviceInfoRepository.reset();
            upgradeRepository.reset();
        }

        // not resetting:
        //  1. ConnectionRepository: data needs to be kept through the lifecycle of the app
        //  2. SystemRepository or MediaPlaybackRepository: data not related to a device
    }
}
