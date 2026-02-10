/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.musicprocessing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.repository.Resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface MusicProcessingRepository {

    void init();

    @NonNull
    LiveData<Resource<EQState, Reason>> getEQStateLiveData();

    @NonNull
    LiveData<Resource<List<PreSet>, Reason>> getPreSetsLiveData();

    @NonNull
    LiveData<Resource<PreSet, Reason>> getSelectedSetLiveData();

    @NonNull
    LiveData<Resource<Integer, Reason>> getNumberOfBandsLiveData();

    @NonNull
    LiveData<Resource<Set<BandInfo>, Reason>> getUserSetConfigurationLiveData();

    @NonNull
    LiveData<Set<Integer>> getUpdatingBandsLiveData();

    @NonNull
    default EQState getEQState() {
        Resource<EQState, Reason> resource = getEQStateLiveData().getValue();
        EQState state = resource != null ? resource.getData() : null;
        return state != null ? state : EQState.NOT_PRESENT;
    }

    default int getBandsCount() {
        Resource<Integer, Reason> resource = getNumberOfBandsLiveData().getValue();
        Integer count = resource != null ? resource.getData() : null;
        return count != null ? count : 0;
    }

    @NonNull
    default List<PreSet> getPreSets() {
        Resource<List<PreSet>, Reason> resource = getPreSetsLiveData().getValue();
        List<PreSet> presets = resource == null ? null : resource.getData();
        return presets == null ? new ArrayList<>() : presets;
    }

    @Nullable
    default PreSet getSelectedSet() {
        Resource<PreSet, Reason> resource = getSelectedSetLiveData().getValue();
        return resource == null ? null : resource.getData();
    }

    @NonNull
    default Set<BandInfo> getUserSetConfiguration() {
        Resource<Set<BandInfo>, Reason> resource = getUserSetConfigurationLiveData().getValue();
        Set<BandInfo> configuration = resource != null ? resource.getData() : null;
        return configuration == null ? new HashSet<>() : configuration;
    }

    @NonNull
    default Set<Integer> getUpdatingBands() {
        Set<Integer> bands = getUpdatingBandsLiveData().getValue();
        return bands == null ? new HashSet<>() : bands;
    }

    void fetchData();

    void fetchUserSetConfiguration();

    void fetchUserSetConfiguration(int[] bands);

    void selectSet(PreSet set);

    void setUserSetBandGain(int band, double gain);

    void setAllGains(double gain);

    void reset();
}
