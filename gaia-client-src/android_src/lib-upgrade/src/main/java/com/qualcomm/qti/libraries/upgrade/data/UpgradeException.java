/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import android.annotation.SuppressLint;

import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

/**
 * <p>A {@link UpgradeException UpgradeException} is thrown when a problem occurs with the upgrade
 * protocol.</p>
 */
public class UpgradeException extends Exception {

    /**
     * <p>The type of the {@link UpgradeException UpgradeException} .</p>
     */
    @Type
    private final int mType;
    /**
     * If the {@link UpgradeException UpgradeException} occurs during the treatment of the bytes,
     * the bytes are provided.
     */
    private final byte[] mBytes;

    /**
     * <p></p>All types of {@link UpgradeException UpgradeExceptions}.</p>
     */
    @IntDef(flag = true, value = {Type.ARRAY_TOO_SHORT})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Type {

        /**
         * <p>The exception occurs when trying to build an {@link UpgradeMessage UpgradeMessage}
         * from a byte array which
         * does not contain the minimum information it should: the operation code and the data
         * length, 3 bytes.</p>
         */
        int ARRAY_TOO_SHORT = 0;
    }

    /**
     * <p>Class constructor for this exception.</p>
     *
     * @param type
     *         the type of this exception.
     * @param bytes
     *         complementary information to the type for this exception.
     */
    public UpgradeException(@Type int type, byte[] bytes) {
        super();
        this.mType = type;
        this.mBytes = bytes;
    }

    /**
     * <p>To know the type of this exception.</p>
     *
     * @return the exception type.
     */
    public @Type
    int getType() {
        return this.mType;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();

        if (mType == Type.ARRAY_TOO_SHORT) {
            strBuilder.append("Build of an UpgradeMessage failed: the byte array does not " +
                                      "contain the minimum required" +
                                      " information");
            strBuilder.append("\nReceived bytes: ").append(Utils.getHexadecimalStringFromBytes(mBytes));
        }
        else {
            strBuilder.append("UpgradeException occurs");
        }

        return strBuilder.toString();
    }
}
