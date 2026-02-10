/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public interface BitItem {

    int getId();

    int getByteOffset();

    int getBitOffset();

    default int getMask() {
        return 1 << getBitOffset();
    }

    default boolean isPresent(byte[] data) {
        if (data == null) {
            return false;
        }

        int value = getUINT8(data, getByteOffset(), 0);
        return (value & getMask()) != 0;
    }

}
