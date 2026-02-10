/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import android.os.Parcelable;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;

public interface Action extends BitItem, Parcelable {

    int getId();

    default int getPosition() {
        return getId() / BITS_IN_BYTE;
    }

    default int getMask() {
        return 1 << (getId() % BITS_IN_BYTE);
    }

}
