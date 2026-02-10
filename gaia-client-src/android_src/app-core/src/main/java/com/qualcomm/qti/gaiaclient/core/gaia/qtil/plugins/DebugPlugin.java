/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.data.LogInfo;

import java.io.File;

public interface DebugPlugin {

    void fetchLogInfo(LogInfo info);

    void downloadLogs(File file);

    void cancelDownload();

    void initTransferSession();
}
