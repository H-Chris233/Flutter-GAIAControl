/*
 * ************************************************************************************************
 * * © 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

/**
 * {@link MutableLiveData} with {@link #getValue()} that returns the latest value set with {@link #setValue(Object)} or
 * {@link #postValue(Object)} regardless of the {@link androidx.lifecycle.LifecycleOwner} state or the observers.
 */
public class StickyLiveData<T> extends MutableLiveData<T> {

    private T data = null;

    public StickyLiveData(T value) {
        super(value);
        data = value;
    }

    public StickyLiveData() {
        super();
    }

    @Override
    public void postValue(T value) {
        data = value;
        super.postValue(data); // latest known value
    }

    @Override
    public void setValue(T value) {
        data = value;
        super.setValue(value);
    }

    @Nullable
    @Override
    public T getValue() {
        return data;
    }
}
