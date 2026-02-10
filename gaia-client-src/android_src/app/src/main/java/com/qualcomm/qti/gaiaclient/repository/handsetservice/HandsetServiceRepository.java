/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.handsetservice;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.MultipointType;

public interface HandsetServiceRepository {

    void init();

    @NonNull
    LiveData<MultipointType> getMultipointTypeLiveData();

    void setMultipointType(Context context, MultipointType type);

    void reset();
}
