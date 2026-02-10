/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil;

import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.VoiceProcessingPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceProcessingSubscriber;

/**
 * All the subscriptions defined in the core module.
 */
public enum CoreSubscription implements Subscription {

    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ConnectionPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber}</li>
     * </ul>
     */
    CONNECTION,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.QTILFeaturesPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.QTILFeaturesSubscriber}
     * </li>
     * </ul>
     */
    FEATURES,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DeviceInformationPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DeviceInformationSubscriber}</li>
     * </ul>
     */
    DEVICE_INFORMATION,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.UpgradePublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.UpgradeSubscriber}</li>
     * </ul>
     */
    UPGRADE,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.VoiceUIPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceUISubscriber}</li>
     * </ul>
     */
    VOICE_UI,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ANCPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ANCSubscriber}</li>
     * </ul>
     */
    ANC,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.AudioCurationPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.AudioCurationSubscriber}</li>
     * </ul>
     */
    AUDIO_CURATION,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudPublisher}
     * </li>
     * <li>Subscriber:
     * <p>
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudSubscriber}
     * </li>
     * </ul>
     */
    EARBUD,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.HandoverPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandoverSubscriber}
     * </li>
     * </ul>
     */
    HANDOVER,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ProtocolPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ProtocolSubscriber}
     * </li>
     * </ul>
     */
    TRANSPORT_INFORMATION,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.BluetoothPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BluetoothSubscriber}
     * </li>
     * </ul>
     */
    BLUETOOTH,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DebugPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DebugSubscriber}
     * </li>
     * </ul>
     */
    DEBUG,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.MusicProcessingPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.MusicProcessingSubscriber}
     * </li>
     * </ul>
     */
    MUSIC_PROCESSING,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudFitPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudFitSubscriber}
     * </li>
     * </ul>
     */
    EARBUD_FIT,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.HandsetServicePublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandsetServiceSubscriber}
     * </li>
     * </ul>
     */
    HANDSET_SERVICE,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link VoiceProcessingPublisher}
     * </li>
     * <li>Subscriber:
     * {@link VoiceProcessingSubscriber}
     * </li>
     * </ul>
     */
    VOICE_PROCESSING,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.GestureConfigurationPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.GestureConfigurationSubscriber}
     * </li>
     * </ul>
     */
    GESTURE_CONFIGURATION,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.BatteryPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BatterySubscriber}
     * </li>
     * </ul>
     */
    BATTERY,
    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.StatisticsPublisher}
     * </li>
     * <li>Subscriber:
     * {@link com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.StatisticsSubscriber}
     * </li>
     * </ul>
     */
    STATISTICS

}
