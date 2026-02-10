/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import java.io.File;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class DownloadDeviceLogsRequest extends Request<Void, Void, Reason> {

    private final File file;

    /**
     * @param file the application must have write permissions on the provided file.
     */
    public DownloadDeviceLogsRequest(@NonNull RequestListener<Void, Void, Reason> listener, File file) {
        super(listener);
        this.file = file;
    }

    @Override
    public void run(@Nullable Context context) {
        if (file == null) {
            onError(Reason.FILE_CREATION_FAILED);
            return;
        }

        DebugPlugin plugin = getQtilManager().getDebugPlugin();
        if (plugin != null) {
            plugin.downloadLogs(file);
            onComplete(null);
        }
        else {
            onError(Reason.NOT_SUPPORTED);
        }
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
