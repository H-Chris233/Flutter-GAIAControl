/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

public class DataChunk {

    private final boolean isEndOfData;

    private final byte[] data;

    private final int size;

    private final double progress;

    DataChunk(boolean isEndOfData, byte[] data, int size, double progress) {
        this.isEndOfData = isEndOfData;
        this.data = data;
        this.size = size;
        this.progress = progress;
    }

    public boolean isEndOfData() {
        return isEndOfData;
    }

    public byte[] getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public double getProgress() {
        return progress;
    }
}
