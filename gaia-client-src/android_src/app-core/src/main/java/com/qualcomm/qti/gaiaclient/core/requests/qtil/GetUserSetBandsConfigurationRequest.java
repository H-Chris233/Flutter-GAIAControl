/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.MusicProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetUserSetBandsConfigurationRequest extends Request<Void, Void, Void> {

    private final int[] bands;

    private final int bandStart;

    private final int bandEnd;

    public GetUserSetBandsConfigurationRequest(int[] bands) {
        this(bands, 0, 0);
    }

    public GetUserSetBandsConfigurationRequest(int bandStart, int bandEnd) {
        this(null, bandStart, bandEnd);
    }

    private GetUserSetBandsConfigurationRequest(int[] bands, int bandStart, int bandEnd) {
        super(null);
        this.bands = bands;
        this.bandStart = bandStart;
        this.bandEnd = bandEnd;
    }

    @Override
    public void run(@Nullable Context context) {
        MusicProcessingPlugin plugin = getQtilManager().getMusicProcessingPlugin();
        if (plugin != null) {
            if (bands != null) {
                for (int band : bands) {
                    plugin.fetchUserSetBandConfiguration(band);
                }
            }
            else {
                plugin.fetchUserSetBandConfiguration(bandStart, bandEnd);
            }
        }
        onComplete(null);
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
