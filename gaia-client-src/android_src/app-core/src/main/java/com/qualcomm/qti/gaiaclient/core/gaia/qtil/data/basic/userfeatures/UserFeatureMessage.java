/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;

/**
 * <blockquote><pre>
 * bytes  0         1      2      3      4                     len+3
 *        +---------+------+------+------+--------- ... ---------+
 *   ...  |  FLAGS  |   READING STATUS   |         DATA          |
 *        +---------+------+------+------+--------- ... ---------+
 * </pre></blockquote></p>
 */
public class UserFeatureMessage {

    private static final int FLAGS_OFFSET = 0;
    private static final int READING_STATUS_OFFSET = 1;
    private static final int READING_STATUS_LENGTH = 3;
    private static final int DATA_OFFSET = 4;

    private final byte flags;

    private final byte[] readingStatus;

    private final byte[] data;

    public UserFeatureMessage(byte[] data) {
        flags = data.length >= 1 ? data[FLAGS_OFFSET] : 0;
        this.readingStatus = getSubArray(data, READING_STATUS_OFFSET, READING_STATUS_LENGTH);
        this.data = getSubArray(data, DATA_OFFSET);
    }

    public byte[] getData() {
        return data;
    }

    public boolean hasMoreData() {
        return (flags & FLAGS.HAS_MORE_DATA) != 0;
    }

    public byte[] getReadingStatus() {
        return readingStatus;
    }

    private static final class FLAGS {
        static final int HAS_MORE_DATA = 0b1;
    }
}
