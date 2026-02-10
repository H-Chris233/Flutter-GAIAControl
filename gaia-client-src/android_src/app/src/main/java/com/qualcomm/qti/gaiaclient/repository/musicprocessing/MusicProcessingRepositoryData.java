/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.musicprocessing;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSetType;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

@Singleton
public abstract class MusicProcessingRepositoryData implements MusicProcessingRepository {

    private final StickyLiveData<Resource<EQState, Reason>> mState = new StickyLiveData<>();

    private final StickyLiveData<Resource<List<PreSet>, Reason>> mPreSets = new StickyLiveData<>();

    private final StickyLiveData<Resource<PreSet, Reason>> mSelectedSet = new StickyLiveData<>();

    private final StickyLiveData<Resource<Integer, Reason>> mNumberOfBands = new StickyLiveData<>();

    private final StickyLiveData<Resource<Set<BandInfo>, Reason>> mUserSetConfiguration = new StickyLiveData<>();

    private final StickyLiveData<Set<Integer>> mUpdatingBands = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Resource<EQState, Reason>> getEQStateLiveData() {
        return mState;
    }

    @NonNull
    @Override
    public LiveData<Resource<List<PreSet>, Reason>> getPreSetsLiveData() {
        return mPreSets;
    }

    @NonNull
    @Override
    public LiveData<Resource<PreSet, Reason>> getSelectedSetLiveData() {
        return mSelectedSet;
    }

    @NonNull
    @Override
    public LiveData<Resource<Integer, Reason>> getNumberOfBandsLiveData() {
        return mNumberOfBands;
    }

    @NonNull
    @Override
    public LiveData<Resource<Set<BandInfo>, Reason>> getUserSetConfigurationLiveData() {
        return mUserSetConfiguration;
    }

    @NonNull
    @Override
    public LiveData<Set<Integer>> getUpdatingBandsLiveData() {
        return mUpdatingBands;
    }

    @Override
    public void reset() {
        mState.setValue(null);
        mNumberOfBands.setValue(null);
        mPreSets.setValue(null);
        mSelectedSet.setValue(null);
        mUpdatingBands.setValue(null);
        mUserSetConfiguration.setValue(null);
    }

    protected final void updatePresets(List<PreSet> presets) {
        mPreSets.postValue(Resource.data(presets));

        if (!hasUserPreset(presets)) {
            mUpdatingBands.postValue(new HashSet<>());
        }
    }

    protected final void updateSelectedSet(PreSet selected) {
        mSelectedSet.postValue(Resource.data(selected));
    }

    protected final void updateBandsNumber(int count) {
        mNumberOfBands.postValue(Resource.data(count));
    }

    protected final void updateUserSetConfiguration(List<BandInfo> bands) {
        // update the bands information
        Set<BandInfo> configuration = getUserSetConfiguration();
        // replace bands with their updated configuration
        bands.forEach(configuration::remove); // no duplicates in a set => remove corresponding bands first
        configuration.addAll(bands);
        // update the live data
        mUserSetConfiguration.postValue(Resource.data(configuration));

        // bands have been updated -> to be removed from the updating field
        Set<Integer> updatingBands = getUpdatingBands();
        for (BandInfo band : bands) {
            updatingBands.remove(band.getId());
        }
        mUpdatingBands.postValue(updatingBands);
    }

    protected final void updateUpdatingBands(int[] bands) {
        Set<Integer> updatingBands = getUpdatingBands();
        for (int band : bands) {
            updatingBands.add(band); // a set does not contain duplicates
        }
        mUpdatingBands.postValue(updatingBands);
    }

    protected final void updateUpdatingBands(Set<Integer> updatingBands) {
        mUpdatingBands.postValue(updatingBands);
    }

    protected final void resetUpdatingBands() {
        mUpdatingBands.postValue(new HashSet<>());
    }

    protected final void updateEQState(EQState state) {
        mState.postValue(Resource.data(state));
    }

    protected final void onEQStateError(Reason reason) {
        mState.postValue(Resource.error(getEQState(), reason));
    }

    protected final void onPreSetsError(Reason reason) {
        mPreSets.postValue(Resource.error(getPreSets(), reason));
    }

    protected final void onSelectedSetError(Reason reason) {
        mSelectedSet.postValue(Resource.error(getSelectedSet(), reason));
    }

    protected final void onBandsNumberError(Reason reason) {
        mNumberOfBands.postValue(Resource.error(getBandsCount(), reason));
    }

    protected final void onUserSetConfigurationError(Reason reason) {
        mUserSetConfiguration.postValue(Resource.error(getUserSetConfiguration(), reason));
    }

    private boolean hasUserPreset(List<PreSet> presets) {
        for (PreSet info : presets) {
            if (info.getType() == PreSetType.USER) {
                return true;
            }
        }
        return false;
    }

}
