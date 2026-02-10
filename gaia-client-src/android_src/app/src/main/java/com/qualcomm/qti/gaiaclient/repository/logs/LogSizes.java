/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.LogInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.LogSize;

public class LogSizes {

    private final ArrayMap<LogInfo, Long> mSizes = new ArrayMap<>();

    public long getSize(LogInfo info) {
        Long value = mSizes.get(info);
        return value != null ? value : 0;
    }

    public void setSize(LogInfo info, long value) {
        mSizes.put(info, value);
    }

    public static LogSizes addLogSize(@Nullable LogSizes origin, LogSize size) {
        if (origin == null) {
            origin = new LogSizes();
        }

        origin.setSize(size.getInfo(), size.getSize());
        return origin;
    }

    public String getLabel(@NonNull Context context) {
        long partitionSize = getSize(LogInfo.PARTITION_SIZE);
        long fileSize = getSize(LogInfo.LOG_FILE);
        return context.getString(R.string.settings_logs_sizes_values, partitionSize, fileSize);
    }

}
