/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import com.qualcomm.qti.libraries.upgrade.messages.OpCodes;

import androidx.annotation.NonNull;

/**
 * A class to synchronously read some data.
 */
public final class DataReader {

    /**
     * The data to read.
     */
    @NonNull
    private byte[] data = new byte[0];
    /**
     * The offset to read the data from.
     */
    private int offset = 0;
    /**
     * The maximum length to use when getting a chunk of data.
     */
    private int maxChunkSize = OpCodes.UpgradeDataBytesREQ.CONTENT_LENGTH;
    /**
     * Keep a count of the bytes that still needs to be taken from the data since the last call
     * to {@link #readNext()}.
     */
    private int remainingBytes = 0;
    /**
     * The ID to identify the data.
     */
    private byte[] dataID = new byte[0];

    /**
     * To instantiate this class.
     */
    public DataReader() {
    }

    /**
     * To set the data to read and its identifier.
     *
     * @param data
     *         The bytes to manage.
     * @param dataID
     *         The ID for the data.
     */
    public synchronized void setData(@NonNull byte[] data, @NonNull byte[] dataID) {
        this.data = data;
        this.dataID = dataID;
    }

    /**
     * To get the ID that was set for the data of this reader.
     *
     * @return the ID of the data.
     */
    @NonNull
    public synchronized byte[] getDataID() {
        return this.dataID;
    }

    /**
     * <p>To set the maximum length when creating data chunks.</p>
     *
     * @param maxChunkSize
     *         the maximum length of a chunk of data.
     *
     * @return the length that has been set.
     */
    public synchronized int setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        return this.maxChunkSize;
    }

    /**
     * <p>To know if the reader has still some bytes to read.</p>
     *
     * @return True if it hasn't read all the bytes that were requested, False otherwise.
     */
    public boolean hasNext() {
        return remainingBytes > 0;
    }

    /**
     * <p>This method reads a chunk of data from the set data.</p>
     * <p>This method calculates the number of bytes that can be read depending on the bytes it
     * was requested to send with {@link #set(int, int)} and the maximum length it can read, set
     * wit {@link #setMaxChunkSize(int)}.</p>
     * <p>Once the data is read, this reader increases its offset by the numbers of bytes read.</p>
     *
     * @return A chunk of data depending on the requested bytes and the cursor of this reader.
     */
    public synchronized DataChunk readNext() {
        // get length
        int chunkSize = remainingBytes <= maxChunkSize ? remainingBytes : maxChunkSize;

        // get the data to send
        byte[] nextData = new byte[chunkSize];
        System.arraycopy(data, offset, nextData, 0, nextData.length);

        // update the upload progress
        offset += chunkSize;
        remainingBytes -= chunkSize;

        // calculate upload progress
        double progress = offset * 100.0 / data.length;
        progress = (progress < 0) ? 0 : (progress > 100) ? 100 : progress;

        // check if the end of the data is reached
        boolean isEndOfData = offset == data.length;

        return new DataChunk(isEndOfData, nextData, chunkSize, progress);
    }

    /**
     * <p>To move the reader to a new offset and request a number of bytes from the data.</p>
     * <p>Call {@link #readNext()} to get the first data chunk that corresponds to the request.
     * Check with {@link #hasNext()} to know if the reader has more bytes to read to fulfil the
     * request.</p>
     *
     * @param move
     *         The number of bytes the offset of the reader should jump by.
     * @param requested
     *         The number of bytes the reader should read.
     */
    public synchronized void set(int move, int requested) {
        // moving the data offset
        if (move > 0 && move + offset < data.length) {
            offset += move;
        }

        // updating the remaining bytes to send
        // if the requested length is not in {0, remainingLength} the maximum length is used
        int remainingLength = data.length - offset;
        requested = (0 <= requested && requested < remainingLength) ? requested : remainingLength;
        remainingBytes = requested;
    }

    /**
     * <p>To reset this reader. Its offset moves back to 0 and it sets its bytes to read to 0.</p>
     */
    public synchronized void reset() {
        offset = 0;
        remainingBytes = 0;
    }
}
