/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<Data, Error> {

    @Nullable
    private final Data data;

    @Nullable
    private final Error error;

    @NonNull
    private final ResourceType type;

    public static <D, E> Resource<D, E> data(@Nullable D data) {
        return new Resource<>(data);
    }

    public static <D, E> Resource<D, E> error(@Nullable D data, E error) {
        return new Resource<>(data, error);
    }

    public static <D, E> Resource<D, E> error(@Nullable Resource<D, E> previous, E error) {
        return new Resource<>(previous == null ? null : previous.getData(), error);
    }

    // private constructors

    private Resource(@Nullable Data data) {
        this.error = null;
        this.data = data;
        this.type = ResourceType.DATA;
    }

    private Resource(@Nullable Data data, @NonNull Error error) {
        this.error = error;
        this.data = data;
        this.type = ResourceType.ERROR;
    }

    public boolean hasData() {
        return data != null;
    }

    @Nullable
    public Data getData() {
        return data;
    }

    @Nullable
    public Error getError() {
        return error;
    }

    @NonNull
    public ResourceType getType() {
        return type;
    }

    @Override
    @NonNull
    public String toString() {
        return "Result{" +
                "type=" + type +
                ", data=" + data +
                ", error=" + error +
                '}';
    }
}
