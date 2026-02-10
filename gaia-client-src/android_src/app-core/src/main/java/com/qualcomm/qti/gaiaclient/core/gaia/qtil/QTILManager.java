/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil;

import androidx.annotation.NonNull;

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
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;

public interface QTILManager {

    ANCPlugin getANCPlugin();

    AudioCurationPlugin getAudioCurationPlugin();

    BasicPlugin getBasicPlugin();

    EarbudPlugin getEarbudPlugin();

    UpgradePlugin getUpgradePlugin();

    VoiceUIPlugin getVoiceUIPlugin();

    DebugPlugin getDebugPlugin();

    MusicProcessingPlugin getMusicProcessingPlugin();

    @NonNull
    UpgradeHelper getUpgradeHelper();

    EarbudFitPlugin getEarbudFitPlugin();

    HandsetServicePlugin getHandsetServicePlugin();

    VoiceProcessingPlugin getVoiceProcessingPlugin();

    GestureConfigurationPlugin getGestureConfigurationPlugin();

    BatteryPlugin getBatteryPlugin();

    StatisticsPlugin getStatisticsPlugin();

    XpanPlugin getXpanPlugin();
}
