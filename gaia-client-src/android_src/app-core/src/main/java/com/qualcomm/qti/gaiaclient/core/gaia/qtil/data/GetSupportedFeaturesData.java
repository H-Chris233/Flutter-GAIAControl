/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class GetSupportedFeaturesData {

    private final boolean hasMoreData;

    private final ArrayList<Feature> features = new ArrayList<>();

    private static final int MORE_DATA_OFFSET = 0;
    private static final int FEATURES_LIST_OFFSET = 1;
    private static final int FEATURE_ID_PAIR_OFFSET = 0;
    private static final int FEATURE_VERSION_PAIR_OFFSET = 1;
    private static final int MORE_DATA_VALUE = 0x01;

    public GetSupportedFeaturesData(byte[] payload) {
        this.hasMoreData = getUINT8(payload, MORE_DATA_OFFSET) == MORE_DATA_VALUE;

        for (int i = FEATURES_LIST_OFFSET; i < payload.length; i = i + 2) {
            int featureId = getUINT8(payload, FEATURE_ID_PAIR_OFFSET + i);
            int version = getUINT8(payload, FEATURE_VERSION_PAIR_OFFSET + i);
            features.add(new Feature(featureId, version));
        }
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }

    @NonNull
    public List<Feature> getFeatures() {
        return new ArrayList<>(features);
    }

    public Feature getFeature(int featureId) {
        for (Feature feature : features) {
            if (feature.getFeatureId() == featureId) {
                return feature;
            }
        }

        return null;
    }

}
