/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import androidx.annotation.NonNull;

import java.util.Objects;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;

public class BitItemDefault implements BitItem {

    private final int id;
    private final int byteOffset;
    private final int bitOffset;

    /**
     * Explicit constructor to have the values in a human readable way.
     */
    BitItemDefault(int byteOffset, int bitOffset) {
        this.id = byteOffset * BITS_IN_BYTE + bitOffset;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getByteOffset() {
        return byteOffset;
    }

    @Override
    public int getBitOffset() {
        return bitOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BitItemDefault that = (BitItemDefault) o;
        return id == that.id &&
                byteOffset == that.byteOffset &&
                bitOffset == that.bitOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, byteOffset, bitOffset);
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "id=" + id +
                ", offsets=(" + byteOffset + ", " + bitOffset + ')' +
                '}';
    }
}
