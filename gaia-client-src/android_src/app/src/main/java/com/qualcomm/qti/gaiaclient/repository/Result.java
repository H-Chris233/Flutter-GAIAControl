/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Result<Data, Reason> {

    @NonNull
    private final ResultStatus mStatus;

    @Nullable
    private final Data mData;

    @Nullable
    private final Reason mReason;

    public static <Data, Reason> Result<Data, Reason> success() {
        return new Result<>(ResultStatus.SUCCESS, null);
    }

    public static <Data, Reason> Result<Data, Reason> success(@NonNull Data data) {
        return new Result<>(ResultStatus.SUCCESS, data);
    }

    public static <Data, Reason> Result<Data, Reason> error(@NonNull Data data, Reason reason) {
        return new Result<>(ResultStatus.FAIL, data, reason);
    }

    public static <Data, Reason> Result<Data, Reason> error(Reason reason) {
        return new Result<>(ResultStatus.FAIL, null, reason);
    }

    public static <Data, Reason> Result<Data, Reason> inProgress(@NonNull Data data) {
        return new Result<>(ResultStatus.IN_PROGRESS, data);
    }

    // private constructors

    private Result(@NonNull ResultStatus status, @Nullable Data data, @NonNull Reason reason) {
        this.mStatus = status;
        this.mReason = reason;
        this.mData = data;
    }

    private Result(@NonNull ResultStatus status, @Nullable Data data) {
        this.mStatus = status;
        this.mReason = null;
        this.mData = data;
    }

    @NonNull
    public ResultStatus getStatus() {
        return mStatus;
    }

    @Nullable
    public Data getData() {
        return mData;
    }

    @Nullable
    public Reason getReason() {
        return mReason;
    }

    @Override
    @NonNull
    public String toString() {
        return "Result{" +
                "status=" + mStatus +
                ", reason=" + mReason +
                ", data=" + mData +
                '}';
    }
}
