/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getString;

import androidx.annotation.NonNull;

public class ApplicationFeature {

    private final int index;

    private final int length;

    private final String data;

    public ApplicationFeature(int index, int length, byte[] content) {
        this.index = index;
        this.length = length;
        this.data = getString(content);
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    public String getData() {
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "ApplicationFeature{" +
                "index=" + index +
                ", length=" + length +
                ", data='" + data + '\'' +
                '}';
    }
}
