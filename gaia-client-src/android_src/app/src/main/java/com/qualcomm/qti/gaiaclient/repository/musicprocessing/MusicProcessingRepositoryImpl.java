/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.musicprocessing;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.MusicProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSetType;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.MusicProcessingSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetMusicProcessingInfoRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetUserSetBandsConfigurationRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetSelectedSetRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetUserBandGainRequest;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MusicProcessingRepositoryImpl extends MusicProcessingRepositoryData {

    private final MusicProcessingSubscriber mMusicProcessingSubscriber = new MusicProcessingSubscriber() {

        @Override
        public void onEQInfo(MusicProcessingInfo info, Object update) {
            switch (info) {
                case AVAILABLE_PRE_SETS:
                    //noinspection unchecked
                    updatePresets((List<PreSet>) update);
                    break;
                case SELECTED_SET:
                    PreSet selected = (PreSet) update;
                    updateSelectedSet(selected);
                    fetchUserSetConfiguration(); // conditions are checked by fetchUserSetConfiguration()
                    break;
                case USER_SET_BANDS_NUMBER:
                    updateBandsNumber((int) update);
                    fetchUserSetConfiguration(); // conditions are checked by fetchUserSetConfiguration()
                    break;
                case USER_SET_CONFIGURATION:
                    //noinspection unchecked
                    updateUserSetConfiguration((List<BandInfo>) update);
                    break;
                case EQ_STATE:
                    updateEQState((EQState) update);
                    fetchUserSetConfiguration(); // conditions are checked by fetchUserSetConfiguration()
                    break;
                case BAND_CHANGE:
                    int[] bands = (int[]) update;
                    updateUpdatingBands(bands);
                    fetchUserSetConfiguration(bands);
                    break;
            }
        }

        @Override
        public void onEQError(MusicProcessingInfo info, Reason reason) {
            switch (info) {
                case AVAILABLE_PRE_SETS:
                    onPreSetsError(reason);
                    break;
                case SELECTED_SET:
                    onSelectedSetError(reason);
                    break;
                case USER_SET_BANDS_NUMBER:
                    onBandsNumberError(reason);
                    break;
                case USER_SET_CONFIGURATION:
                    onUserSetConfigurationError(reason);
                    resetUpdatingBands(); // a band had an issue to be updated => others cannot be
                    break;
                case EQ_STATE:
                    onEQStateError(reason);
                    break;
                case BAND_CHANGE:
                    // unexpected as BAND_CHANGE is only sent as a notification from the device
                    resetUpdatingBands();
                    break;
            }
        }
    };

    @Inject
    public MusicProcessingRepositoryImpl() {
    }

    @Override
    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mMusicProcessingSubscriber);
    }

    @Override
    public void fetchData() {
        fetchInfo(MusicProcessingInfo.EQ_STATE);
        fetchInfo(MusicProcessingInfo.AVAILABLE_PRE_SETS);
        fetchInfo(MusicProcessingInfo.SELECTED_SET);
        fetchInfo(MusicProcessingInfo.USER_SET_BANDS_NUMBER);
        // User set bands configuration will be fetched on receiving EQ state and/or user set
    }

    @Override
    public void fetchUserSetConfiguration() {
        int bandCount = getBandsCount();
        boolean hasUserSet = getPreSets().stream().anyMatch(set -> set.getType().equals(PreSetType.USER));

        if (getEQState() == EQState.NOT_PRESENT || bandCount <= 0 || !hasUserSet) {
            // EQ is not available => User set cannot be fetched
            // No bands are available => no bands to fetch
            // user set not included => no user configuration to fetch
            return;
        }

        // add all bands to be updated to the list of updating bands
        Set<Integer> updatingBands = getUpdatingBands();
        for (int i = 0; i < bandCount; i++) {
            updatingBands.add(i);
        }
        updateUpdatingBands(updatingBands);

        // fetch the full user set configuration
        GetUserSetBandsConfigurationRequest request = new GetUserSetBandsConfigurationRequest(0, bandCount - 1);
        GaiaClientService.getRequestManager().execute(null, request);
    }

    @Override
    public void fetchUserSetConfiguration(int[] bands) {
        int bandCount = getBandsCount();
        boolean hasUserSet = getPreSets().stream().anyMatch(set -> set.getType().equals(PreSetType.USER));

        if (getEQState() == EQState.NOT_PRESENT || bandCount <= 0 || !hasUserSet) {
            // EQ is not available => User set cannot be fetched
            // No bands are available => no bands to fetch
            // user set not included => no user configuration to fetch
            return;
        }

        // add all bands to be updated to the list of updating bands
        Set<Integer> updatingBands = getUpdatingBands();
        for (int band : bands) {
            updatingBands.add(band);
        }
        updateUpdatingBands(updatingBands);

        // fetch the bands configuration
        GetUserSetBandsConfigurationRequest request = new GetUserSetBandsConfigurationRequest(bands);
        GaiaClientService.getRequestManager().execute(null, request);
    }

    @Override
    public void selectSet(PreSet set) {
        SetSelectedSetRequest request = new SetSelectedSetRequest(set);
        GaiaClientService.getRequestManager().execute(null, request);
    }

    @Override
    public void setUserSetBandGain(int band, double gain) {
        SetUserBandGainRequest request = new SetUserBandGainRequest(band, gain);
        GaiaClientService.getRequestManager().execute(null, request);
    }

    @Override
    public void setAllGains(double gain) {
        int lastBand = getBandsCount() - 1;
        SetUserBandGainRequest request = new SetUserBandGainRequest(0, lastBand, gain);
        GaiaClientService.getRequestManager().execute(null, request);
    }

    private void fetchInfo(MusicProcessingInfo info) {
        GetMusicProcessingInfoRequest request = new GetMusicProcessingInfoRequest(info, null);
        GaiaClientService.getRequestManager().execute(null, request);
    }

}
