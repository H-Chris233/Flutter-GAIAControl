/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.features;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public abstract class FeaturesRepositoryData implements FeaturesRepository {

    private final StickyLiveData<ArrayMap<QTILFeature, Integer>> mFeatures = new StickyLiveData<>();

    private final StickyLiveData<FeatureReason> mFeatureError = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<ArrayMap<QTILFeature, Integer>> getFeaturesLiveData() {
        return mFeatures;
    }

    @NonNull
    @Override
    public LiveData<FeatureReason> getFeatureErrorLiveData() {
        return mFeatureError;
    }

    @Override
    public void reset() {
        mFeatures.postValue(new ArrayMap<>());
    }

    protected void addFeature(@NonNull QTILFeature feature, int version) {
        @NonNull ArrayMap<QTILFeature, Integer> map = getFeatures();

        if (map.containsKey(feature)) {
            // feature is already known as supported
            return;
        }

        map.put(feature, version);

        mFeatures.postValue(map);
    }

    protected void removeFeature(@NonNull QTILFeature feature) {
        @NonNull ArrayMap<QTILFeature, Integer> map = getFeatures();

        map.remove(feature);

        mFeatures.postValue(map);
    }

    protected void onError(FeatureReason reason) {
        mFeatureError.postValue(reason);
    }

}
