/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.MusicProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.MusicProcessingSubscriber;

import java.util.List;

public class MusicProcessingPublisher extends Publisher<MusicProcessingSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.MUSIC_PROCESSING;
    }

    public void publishEqState(EQState state) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.EQ_STATE, state));
    }

    public void publishAvailablePreSets(List<PreSet> presets) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.AVAILABLE_PRE_SETS, presets));
    }

    public void publishSelectedSet(PreSet set) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.SELECTED_SET, set));
    }

    public void publishBandsNumber(int count) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.USER_SET_BANDS_NUMBER, count));
    }

    public void publishUserSetConfiguration(List<BandInfo> bands) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.USER_SET_CONFIGURATION, bands));
    }

    public void publishBandChanged(int[] bands) {
        forEachSubscriber(subscriber -> subscriber.onEQInfo(MusicProcessingInfo.BAND_CHANGE, bands));
    }

    public void publishError(MusicProcessingInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onEQError(info, reason));
    }
}
