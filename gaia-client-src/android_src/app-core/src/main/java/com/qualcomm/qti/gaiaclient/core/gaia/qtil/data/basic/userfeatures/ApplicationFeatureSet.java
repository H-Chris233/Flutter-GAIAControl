/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import java.util.ArrayList;
import java.util.List;

/**
 * The data of an application feature contains a human readable representation of the feature.
 */
public class ApplicationFeatureSet extends UserFeatureSet {

    private static final int FEATURE_INDEX_OFFSET = 0;
    private static final int FEATURE_LENGTH_OFFSET = 1;
    private static final int FEATURE_DATA_OFFSET = 2;
    private static final int HEADER_LENGTH = 2;

    private final List<ApplicationFeature> features = new ArrayList<>();

    public ApplicationFeatureSet(FeatureType type, int size, byte[] data) {
        super(type, size, data);

        byte[] rawData = getData();
        int i = 0;
        while (i < rawData.length) {
            int index = getUINT8(rawData, FEATURE_INDEX_OFFSET + i);
            int length = getUINT8(rawData, FEATURE_LENGTH_OFFSET + i);
            byte[] rawContent = getSubArray(rawData, FEATURE_DATA_OFFSET + i, length);
            features.add(new ApplicationFeature(index, length, rawContent));
            i += length + HEADER_LENGTH;
        }
    }

    public List<ApplicationFeature> getFeatures() {
        return features;
    }
}
