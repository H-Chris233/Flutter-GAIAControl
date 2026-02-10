/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class SupportedAssistants {

    @NonNull
    private final List<VoiceAssistant> assistants = new ArrayList<>();

    private static final int ASSISTANTS_COUNT_OFFSET = 0;
    private static final int ASSISTANT_OFFSET = 1;

    public SupportedAssistants(byte[] payload) {
        int count = getUINT8(payload, ASSISTANTS_COUNT_OFFSET);

        for (int i = 0; i < count; i++) {
            assistants.add(VoiceAssistant.valueOf(getUINT8(payload, i + ASSISTANT_OFFSET)));
        }
    }

    @NonNull
    public List<VoiceAssistant> getAssistants() {
        return assistants;
    }

}
