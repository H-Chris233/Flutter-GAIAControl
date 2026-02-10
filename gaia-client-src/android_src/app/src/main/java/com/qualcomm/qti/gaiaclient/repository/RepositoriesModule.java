/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository;

import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCRepositoryLegacy;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCRepositoryLegacyImpl;
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepository;
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.battery.BatteryRepository;
import com.qualcomm.qti.gaiaclient.repository.battery.BatteryRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository;
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.discovery.DiscoveryRepository;
import com.qualcomm.qti.gaiaclient.repository.discovery.DiscoveryRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.earbudfit.EarbudFitRepository;
import com.qualcomm.qti.gaiaclient.repository.earbudfit.EarbudFitRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.handsetservice.HandsetServiceRepository;
import com.qualcomm.qti.gaiaclient.repository.handsetservice.HandsetServiceRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.logs.DeviceLogsRepository;
import com.qualcomm.qti.gaiaclient.repository.logs.DeviceLogsRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaPlaybackRepository;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaPlaybackRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.musicprocessing.MusicProcessingRepository;
import com.qualcomm.qti.gaiaclient.repository.musicprocessing.MusicProcessingRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepository;
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository;
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepository;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.voiceprocessing.VoiceProcessingRepository;
import com.qualcomm.qti.gaiaclient.repository.voiceprocessing.VoiceProcessingRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.voiceui.VoiceUIRepository;
import com.qualcomm.qti.gaiaclient.repository.voiceui.VoiceUIRepositoryImpl;
import com.qualcomm.qti.gaiaclient.repository.xpan.XpanRepository;
import com.qualcomm.qti.gaiaclient.repository.xpan.XpanRepositoryImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoriesModule {

    @Binds
    @Singleton
    public abstract ANCRepositoryLegacy bindANCRepository(ANCRepositoryLegacyImpl impl);

    @Binds
    @Singleton
    public abstract AudioCurationRepository bindAudioCurationRepository(AudioCurationRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract ConnectionRepository bindConnectionRepository(ConnectionRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract DeviceInformationRepository bindDeviceInformationRepository(DeviceInformationRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract DeviceLogsRepository bindDeviceLogsRepository(DeviceLogsRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract DiscoveryRepository bindDiscoveryRepository(DiscoveryRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract FeaturesRepository bindFeaturesRepository(FeaturesRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract HandsetServiceRepository bindHandsetServiceRepository(HandsetServiceRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract MediaPlaybackRepository bindMediaPlaybackRepository(MediaPlaybackRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract MusicProcessingRepository bindMusicProcessingRepository(MusicProcessingRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract SystemRepository bindSystemRepository(SystemRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract UpgradeRepository bindUpgradeRepository(UpgradeRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract VoiceUIRepository bindVoiceUIRepository(VoiceUIRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract VoiceProcessingRepository bindVoiceProcessingRepository(VoiceProcessingRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract EarbudFitRepository bindEarbudFitRepository(EarbudFitRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract GestureConfigurationRepository bindGestureConfigurationRepository(
            GestureConfigurationRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract BatteryRepository bindBatteryRepository(BatteryRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract StatisticsRepository bindStatisticsRepository(StatisticsRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract ServiceRepository bindServiceRepository(ServiceRepositoryImpl impl);

    @Binds
    @Singleton
    public abstract XpanRepository bindXpanRepository(XpanRepositoryImpl impl);

}
