/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.data.MusicProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.MusicProcessingPublisher;

public interface MusicProcessingPlugin {

    // to force the MusicProcessingPlugin to implement MusicProcessingPublisher
    // this is unused
    MusicProcessingPublisher getMusicProcessingPublisher();

    /**
     * <p>Use to fetch any Music Processing information that does not require any parameter.</p>
     * <p>Unsupported information: {@link MusicProcessingInfo#BAND_CHANGE},
     * {@link MusicProcessingInfo#USER_SET_CONFIGURATION}.</p>
     *
     * @return True if it could request the given information, false if the information is not supported by this method.
     */
    boolean fetch(MusicProcessingInfo info);

    void selectSet(PreSet set);

    void setUserSetGains(int bandStart, int bandEnd, double[] gains);

    void fetchUserSetBandConfiguration(int bandStart, int bandEnd);

    void fetchUserSetBandConfiguration(int band);
}
