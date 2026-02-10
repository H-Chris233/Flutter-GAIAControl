/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BatteryPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import java.util.Set;

public class GetBatteryLevelsRequest extends Request<Void, Void, Void> {

    private final Set<Battery> batteries;

    public GetBatteryLevelsRequest(Set<Battery> batteries) {
        super(null);
        this.batteries = batteries;
    }

    @Override
    public void run(@Nullable Context context) {
        BatteryPlugin plugin = getQtilManager().getBatteryPlugin();
        if (plugin != null) {
            plugin.fetchBatteryLevels(batteries);
            onComplete(null);
        }
        else {
            onError(null);
        }
    }

    @Override
    protected void onEnd() {
    }
}
