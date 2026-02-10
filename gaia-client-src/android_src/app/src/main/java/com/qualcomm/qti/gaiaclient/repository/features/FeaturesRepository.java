/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.features;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;

public interface FeaturesRepository {

    void init();

    void reset();

    @NonNull
    LiveData<ArrayMap<QTILFeature, Integer>> getFeaturesLiveData();

    @NonNull
    LiveData<FeatureReason> getFeatureErrorLiveData();

    @NonNull
    default ArrayMap<QTILFeature, Integer> getFeatures() {
        ArrayMap<QTILFeature, Integer> map = getFeaturesLiveData().getValue();
        return map != null ? map : new ArrayMap<>();
    }

    default boolean isFeatureAvailable(QTILFeature feature) {
        return getFeatures().containsKey(feature);
    }

    default int getFeatureVersion(QTILFeature feature) {
        Integer version = getFeatures().get(feature);
        return version == null ? 0 : version;
    }

}
