/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BasicPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetDeviceInformationRequest extends Request<Void, Void, Void> {

    @NonNull
    private final DeviceInfo info;

    public GetDeviceInformationRequest(@NonNull DeviceInfo info) {
        super(null);
        this.info = info;
    }

    @Override
    public void run(@Nullable Context context) {
        BasicPlugin basicPlugin = getQtilManager().getBasicPlugin();
        if (basicPlugin != null) {
            basicPlugin.fetchDeviceInfo(info);
        }

        onComplete(null);
    }

    @Override
    protected void onEnd() {
    }
}
