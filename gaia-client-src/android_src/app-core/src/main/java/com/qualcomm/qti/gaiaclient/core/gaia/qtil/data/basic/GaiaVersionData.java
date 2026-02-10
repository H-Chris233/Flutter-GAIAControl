/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

public class GaiaVersionData {

    private static final int GAIA_VERSION_OFFSET = 0;
    private static final int RESERVED_BYTE_OFFSET = 1;

    private final int gaiaVersion;

    public GaiaVersionData(@NonNull byte[] data) {
        this.gaiaVersion = getUINT8(data, GAIA_VERSION_OFFSET);
    }

    public int getGaiaVersion() {
        return gaiaVersion;
    }
}
