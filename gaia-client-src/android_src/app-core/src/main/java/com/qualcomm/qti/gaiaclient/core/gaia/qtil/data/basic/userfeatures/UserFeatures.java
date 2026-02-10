/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class UserFeatures {

    private static final int FEATURE_TYPE_OFFSET = 0;
    private static final int SIZE_OFFSET = 1;
    private static final int DATA_OFFSET = 3;
    private static final int HEADER_LENGTH = 3;

    private final List<UserFeatureSet> userFeatureSets = new ArrayList<>();

    public UserFeatures(byte[] source) {
        int i = 0;
        while (i < source.length) {
            FeatureType type = new FeatureType(getUINT8(source, FEATURE_TYPE_OFFSET + i));
            int size = getUINT16(source, SIZE_OFFSET + i);
            byte[] data = getSubArray(source, DATA_OFFSET + i, size);
            userFeatureSets.add(UserFeatureFactory.buildUserFeature(type, size, data));
            i += size + HEADER_LENGTH;
        }
    }

    @Nullable
    public UserFeatureSet getFeature(FeatureTypes type) {
        for (UserFeatureSet feature : userFeatureSets) {
            if (type.equals(feature.getType().getType())) {
                return feature;
            }
        }

        return null;
    }

    @NonNull
    public ApplicationFeatureSet getApplicationFeatureSet() {
        UserFeatureSet feature = getFeature(FeatureTypes.APPLICATION_FEATURE);

        if (feature instanceof ApplicationFeatureSet) {
            return (ApplicationFeatureSet) feature;
        }
        else {
            // returns an empty set
            FeatureType type = new FeatureType(FeatureTypes.APPLICATION_FEATURE.getValue());
            return new ApplicationFeatureSet(type, 0, new byte[]{});
        }
    }
}
