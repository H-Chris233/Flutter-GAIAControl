/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class SupportedEnhancements {

    private final boolean hasMoreData;

    private final ArrayList<VoiceEnhancement> enhancements = new ArrayList<>();

    private static final int MORE_DATA_OFFSET = 0;
    private static final int ENHANCEMENTS_LIST_OFFSET = 1;
    private static final int CAPABILITY_ID_PAIR_OFFSET = 0;
    private static final int VERSION_PAIR_OFFSET = 1;
    private static final int MORE_DATA_VALUE = 0x01;

    public SupportedEnhancements(byte[] payload) {
        this.hasMoreData = getUINT8(payload, MORE_DATA_OFFSET) == MORE_DATA_VALUE;

        for (int i = ENHANCEMENTS_LIST_OFFSET; i < payload.length; i = i + 2) {
            int capabilityId = getUINT8(payload, CAPABILITY_ID_PAIR_OFFSET + i);
            int version = getUINT8(payload, VERSION_PAIR_OFFSET + i);
            enhancements.add(new VoiceEnhancement(capabilityId, version));
        }
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }

    @NonNull
    public List<VoiceEnhancement> getEnhancements() {
        return new ArrayList<>(enhancements);
    }

}
