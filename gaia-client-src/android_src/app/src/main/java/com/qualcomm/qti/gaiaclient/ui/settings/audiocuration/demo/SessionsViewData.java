/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import androidx.annotation.NonNull;

public class SessionsViewData {

    @NonNull
    private final String[] entries;

    @NonNull
    private final String[] values;

    public SessionsViewData(String[] entries, String[] values) {
        this.entries = entries == null ? new String[0] : entries;
        this.values = values == null ? new String[0] : values;
    }

    @NonNull
    public String[] getEntries() {
        return entries;
    }

    @NonNull
    public String[] getValues() {
        return values;
    }
}
