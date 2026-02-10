/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Objects;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

public class CVC3MicConfiguration extends VoiceEnhancementConfiguration {

    private static final int MiCROPHONE_MODE_OFFSET = 0;
    private static final int BYPASS_MODE_OFFSET = 1;
    private static final int OPERATION_MODE_OFFSET = 2;
    private static final int DATA_LENGTH = 3;

    private final int microphoneMode;

    @Nullable
    private final CVCBypassMode bypassMode;

    private final int bypassModeValue;

    @Nullable
    private final CVCOperationMode operationMode;

    private final int operationModeValue;

    protected CVC3MicConfiguration(byte[] values) {
        super(Capability.CVC_3MIC.getValue(), Capability.CVC_3MIC, values);
        this.microphoneMode = getUINT8(values, MiCROPHONE_MODE_OFFSET);
        this.bypassModeValue = getUINT8(values, BYPASS_MODE_OFFSET, -1);
        this.bypassMode = CVCBypassMode.valueOf(this.bypassModeValue);
        this.operationModeValue = getUINT8(values, OPERATION_MODE_OFFSET, -1);
        this.operationMode = CVCOperationMode.valueOf(this.operationModeValue);
    }

    public CVC3MicConfiguration(int microphoneMode, @NonNull CVCBypassMode bypassMode) {
        this(microphoneMode, bypassMode, null);
    }

    @VisibleForTesting
    public CVC3MicConfiguration(int microphoneMode, @Nullable CVCBypassMode bypassMode,
                                @Nullable CVCOperationMode operationMode) {
        super(Capability.CVC_3MIC);
        this.microphoneMode = microphoneMode;
        this.bypassMode = bypassMode;
        this.bypassModeValue = bypassMode == null ? -1 : bypassMode.getValue();
        this.operationMode = operationMode;
        this.operationModeValue = operationMode == null ? -1 : operationMode.getValue();
    }

    @Override
    public byte[] getSetterValues() {
        byte[] values = new byte[DATA_LENGTH - 1]; // operation mode cannot be set
        setUINT8(microphoneMode, values, MiCROPHONE_MODE_OFFSET);
        setUINT8(bypassModeValue, values, BYPASS_MODE_OFFSET);
        return values;
    }

    public int getMicrophoneMode() {
        return microphoneMode;
    }

    @Nullable
    public CVCBypassMode getBypassMode() {
        return bypassMode;
    }

    public int getBypassModeValue() {
        return bypassModeValue;
    }

    @Nullable
    public CVCOperationMode getOperationMode() {
        return operationMode;
    }

    public int getOperationModeValue() {
        return operationModeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CVC3MicConfiguration that = (CVC3MicConfiguration) o;
        return microphoneMode == that.microphoneMode &&
                bypassModeValue == that.bypassModeValue &&
                operationModeValue == that.operationModeValue &&
                bypassMode == that.bypassMode &&
                operationMode == that.operationMode;
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(microphoneMode, bypassMode, bypassModeValue, operationMode, operationModeValue);
    }
}
