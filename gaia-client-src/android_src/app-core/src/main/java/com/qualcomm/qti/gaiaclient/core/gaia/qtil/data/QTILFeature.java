/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum QTILFeature {
    BASIC(0x00),
    EARBUD(0x01),
    ANC(0x02),
    VOICE_UI(0x03),
    DEBUG(0x04),
    MUSIC_PROCESSING(0x05),
    UPGRADE(0x06),
    HANDSET_SERVICE(0x07),
    AUDIO_CURATION(0x08),
    EARBUD_FIT(0x09),
    VOICE_PROCESSING(0x0A),
    GESTURE_CONFIGURATION(0x0B),
    STATISTICS(0x0C),
    BATTERY(0x0D),
    XPAN(0x0E);

    private final int value;

    private static final QTILFeature[] VALUES = values();

    QTILFeature(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static QTILFeature[] getValues() {
        return VALUES;
    }

    public static QTILFeature valueOf(int value) {
        for (QTILFeature feature : VALUES) {
            if (feature.value == value) {
                return feature;
            }
        }

        return null;
    }
}
