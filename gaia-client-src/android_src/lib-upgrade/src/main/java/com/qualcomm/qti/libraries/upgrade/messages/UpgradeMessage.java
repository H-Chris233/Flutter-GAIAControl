/*
 * ************************************************************************************************
 *  Copyright 2016-2018 Qualcomm Technologies International, Ltd.                                 *
 *  © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.messages;

import android.util.Log;

import com.qualcomm.qti.libraries.upgrade.data.UpgradeException;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import androidx.annotation.NonNull;


/**
 * <p>This class allows building of a message for the upgrade as defined in the upgrade
 * documentation.</p>
 * <p>The {@link UpgradeMessage UpgradeMessage} is composed as follows:
 * <blockquote><pre>
 *      0 bytes   1        2         3         4        length+3
 *      +---------+---------+---------+ +---------+---------+
 *      | OPCODE* |      LENGTH*      | |    CONTENT...     |
 *      +---------+---------+---------+ +---------+---------+
 *      * mandatory information
 * </pre></blockquote></p>
 */

public class UpgradeMessage {

    /**
     * <p>The tag to display for logs.</p>
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "UpgradeMessage";
    /**
     * The number of bytes to define the message length information.
     */
    private static final int LENGTH_LENGTH = 2;
    /**
     * The number of bytes to define the message operation code information.
     */
    private static final int OPCODE_LENGTH = 1;
    /**
     * The offset for the operation code information.
     */
    private static final int OPCODE_OFFSET = 0;
    /**
     * The offset for the length information.
     */
    private static final int LENGTH_OFFSET = OPCODE_OFFSET + OPCODE_LENGTH;
    /**
     * The offset for the content information.
     */
    private static final int CONTENT_OFFSET = LENGTH_OFFSET + LENGTH_LENGTH;
    /**
     * The message operation code information.
     */
    private final @OpCodes.Enum int mOpCode;
    /**
     * The message content information.
     */
    private final byte[] mContent;

    /**
     * The minimum length a message should have to be a {@link UpgradeMessage UpgradeMessage}.
     */
    public static final int HEADER_LENGTH = LENGTH_LENGTH + OPCODE_LENGTH;
    /**
     * The minimum available size requires to send and receive Upgrade messages.
     */
    public static final int REQUIRED_MIN_CONTENT_LENGTH =
            HEADER_LENGTH + OpCodes.UpgradeDataBytesREQ.CONTENT_LENGTH;

    /**
     * To create a new instance of {@link UpgradeMessage UpgradeMessage}.
     * 
     * @param opCode
     *            the operation code for this message.
     * @param content
     *            the date for this message.
     */
    public UpgradeMessage(@OpCodes.Enum int opCode, byte[] content) {
        this.mOpCode = opCode;
        if (content != null) {
            this.mContent = content;
        }
        else {
            this.mContent = new byte[0];
        }
    }

    /**
     * To create a new instance of  {@link UpgradeMessage UpgradeMessage}.
     *
     * @param opCode
     *            the operation code for this message.
     */
    public UpgradeMessage(@OpCodes.Enum int opCode) {
        this.mOpCode = opCode;
        this.mContent = new byte[0];
    }

    /**
     * <p>To build an {@link UpgradeMessage UpgradeMessage} object from a byte array sent by a
     * device.</p>
     * <p>The raw content of an {@link UpgradeMessage UpgradeMessage} is as follows:
     * <blockquote><pre>
     *      0 bytes   1        2         3         4        length+3
     *      +---------+---------+---------+ +---------+---------+
     *      | OPCODE* |      LENGTH*      | |    CONTENT...     |
     *      +---------+---------+---------+ +---------+---------+
     *      * mandatory information
     * </pre></blockquote></p>
     * 
     * @param bytes
     *            The raw content of a {@link UpgradeMessage UpgradeMessage} as sent by the device.
     *            To contain all mandatory
     *            information this bytes array has to have a minimum length of
     *            {@link #HEADER_LENGTH HEADER_LENGTH}.
     *
     * @throws UpgradeException type
     * {@link UpgradeException.Type#ARRAY_TOO_SHORT ARRAY_TOO_SHORT}.</p>
     */
    public UpgradeMessage(byte[] bytes) throws UpgradeException {
        if (bytes.length >= HEADER_LENGTH) {
            this.mOpCode = OpCodes.getOpCode(bytes[OPCODE_OFFSET]);
            int length = Utils.extractIntFromByteArray(bytes, LENGTH_OFFSET, LENGTH_LENGTH, false);
            int contentLength = bytes.length - HEADER_LENGTH;

            if (length > contentLength) {
                Log.w(TAG, "Building message: the LENGTH (" + length + ") is bigger than the " +
                        "provided length(" + contentLength + ").");
            }
            else if (length < contentLength) {
                Log.w(TAG, "Building message: the LENGTH (" + length + ") is smaller than the " +
                        "provided length(" + contentLength + ").");
            }

            this.mContent = new byte[contentLength];
            if (contentLength > 0) {
                System.arraycopy(bytes, CONTENT_OFFSET, mContent, 0, contentLength);
            }
        }
        else {
            throw new UpgradeException(UpgradeException.Type.ARRAY_TOO_SHORT, bytes);
        }
    }

    /**
     * To get the byte array corresponding to this {@link UpgradeMessage UpgradeMessage}.
     *
     * @return a byte array built with these message information.
     * 
     */
    public byte[] getBytes() {
        byte[] message = new byte[mContent.length + HEADER_LENGTH];
        // opcode
        message[OPCODE_OFFSET] = (byte) mOpCode;
        // length
        Utils.copyIntIntoByteArray(mContent.length, message, LENGTH_OFFSET, LENGTH_LENGTH, false);

        // content if exists
        if (mContent.length > 0) {
            System.arraycopy(mContent, 0, message, CONTENT_OFFSET, mContent.length);
        }

        return message;
    }

    /**
     * To get the operation code.
     * 
     * @return the operation code.
     */
    public @OpCodes.Enum int getOpCode() {
        return mOpCode;
    }

    /**
     * To get the content length.
     *
     * @return the content length.
     */
    public int getLength() {
        return mContent.length;
    }

    /**
     * To get the message content.
     *
     * @return the message content.
     */
    public byte[] getContent() {
        return mContent;
    }

    @NonNull
    @Override
    public String toString() {
        return "UpgradeMessage{" +
                "code=" + OpCodes.getString(mOpCode) +
                ", content=" + Utils.getHexadecimalStringFromBytes(mContent) +
                '}';
    }
}
