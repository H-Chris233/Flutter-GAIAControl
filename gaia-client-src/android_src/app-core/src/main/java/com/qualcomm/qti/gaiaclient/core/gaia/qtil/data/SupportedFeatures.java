/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SupportedFeatures {

    private final List<Feature> features;

    public SupportedFeatures(List<Feature> features) {
        this.features = features;
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
