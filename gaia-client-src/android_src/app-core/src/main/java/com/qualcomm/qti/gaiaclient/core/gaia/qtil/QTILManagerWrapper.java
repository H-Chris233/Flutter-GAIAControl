/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.GaiaManager;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.ANCPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.AudioCurationPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BasicPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BatteryPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudFitPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.GestureConfigurationPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.HandsetServicePlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.MusicProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.StatisticsPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.UpgradePlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceUIPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.XpanPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;

public final class QTILManagerWrapper implements QTILManager {

    private final QTILManagerImpl mManager;

    public QTILManagerWrapper(@NonNull GaiaManager manager,
                              @NonNull PublicationManager publicationManager) {
        this.mManager = new QTILManagerImpl(manager, publicationManager);
    }

    @Override
    public ANCPlugin getANCPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.ANC);
        return plugin instanceof ANCPlugin ? (ANCPlugin) plugin : null;
    }

    @Override
    public AudioCurationPlugin getAudioCurationPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.AUDIO_CURATION);
        return plugin instanceof AudioCurationPlugin ? (AudioCurationPlugin) plugin : null;
    }

    @Override
    public BasicPlugin getBasicPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.BASIC);
        return plugin instanceof BasicPlugin ? (BasicPlugin) plugin : null;
    }

    @Override
    public EarbudPlugin getEarbudPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.EARBUD);
        return plugin instanceof EarbudPlugin ? (EarbudPlugin) plugin : null;
    }

    @Override
    public UpgradePlugin getUpgradePlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.UPGRADE);
        return plugin instanceof UpgradePlugin ? (UpgradePlugin) plugin : null;
    }

    @Override
    public MusicProcessingPlugin getMusicProcessingPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.MUSIC_PROCESSING);
        return plugin instanceof MusicProcessingPlugin ? (MusicProcessingPlugin) plugin : null;
    }

    @NonNull
    @Override
    public UpgradeHelper getUpgradeHelper() {
        return mManager.getUpgradeHelper();
    }

    @Override
    public VoiceUIPlugin getVoiceUIPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.VOICE_UI);
        return plugin instanceof VoiceUIPlugin ? (VoiceUIPlugin) plugin : null;
    }

    @Override
    public DebugPlugin getDebugPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.DEBUG);
        return plugin instanceof DebugPlugin ? (DebugPlugin) plugin : null;
    }

    @Override
    public EarbudFitPlugin getEarbudFitPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.EARBUD_FIT);
        return plugin instanceof EarbudFitPlugin ? (EarbudFitPlugin) plugin : null;
    }

    @Override
    public HandsetServicePlugin getHandsetServicePlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.HANDSET_SERVICE);
        return plugin instanceof HandsetServicePlugin ? (HandsetServicePlugin) plugin : null;
    }

    @Override
    public VoiceProcessingPlugin getVoiceProcessingPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.VOICE_PROCESSING);
        return plugin instanceof VoiceProcessingPlugin ? (VoiceProcessingPlugin) plugin : null;
    }

    @Override
    public GestureConfigurationPlugin getGestureConfigurationPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.GESTURE_CONFIGURATION);
        return plugin instanceof GestureConfigurationPlugin ? (GestureConfigurationPlugin) plugin : null;
    }

    @Override
    public BatteryPlugin getBatteryPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.BATTERY);
        return plugin instanceof BatteryPlugin ? (BatteryPlugin) plugin : null;
    }

    @Override
    public StatisticsPlugin getStatisticsPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.STATISTICS);
        return plugin instanceof StatisticsPlugin ? (StatisticsPlugin) plugin : null;
    }

    @Override
    public XpanPlugin getXpanPlugin() {
        Plugin plugin = mManager.getPlugin(QTILFeature.XPAN);
        return plugin instanceof XpanPlugin ? (XpanPlugin) plugin : null;
    }

    public void release() {
        mManager.release();
    }
}
