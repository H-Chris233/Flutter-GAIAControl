/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.logs;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadingState {

    private static final int DATA_TRANSFER_HEADER_LENGTH = 2;

    private static final int CHUNK_SIZE_DEFAULT = GaiaPdu.Payload.minLength - DATA_TRANSFER_HEADER_LENGTH;

    private final AtomicBoolean isDownloading = new AtomicBoolean(false);

    private final AtomicInteger sessionId = new AtomicInteger(0);

    private final AtomicLong size = new AtomicLong(0);

    private final AtomicLong offset = new AtomicLong(0);

    private final AtomicInteger chunkSize = new AtomicInteger(CHUNK_SIZE_DEFAULT);

    private final ConcurrentLinkedQueue<byte[]> data = new ConcurrentLinkedQueue<>();

    public boolean setIsDownloading(boolean isDownloading) {
        return this.isDownloading.getAndSet(isDownloading);
    }

    public void setSessionId(int sessionId) {
        this.sessionId.set(sessionId);
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDownloading() {
        return isDownloading.get();
    }

    public void setChunkSize(int payloadSize) {
        this.chunkSize.set(payloadSize - DATA_TRANSFER_HEADER_LENGTH);
    }

    public void resetChunkSize() {
        this.chunkSize.set(CHUNK_SIZE_DEFAULT);
    }

    public int getSessionId() {
        return sessionId.get();
    }

    public double getProgress() {
        // calculate upload progress
        double progress = getOffset() * 100.0 / getSize();
        return (progress < 0) ? 0 : (progress > 100) ? 100 : progress;
    }

    public long getSize() {
        return size.get();
    }

    public long getOffset() {
        return offset.get();
    }

    public int getChunkSize() {
        return chunkSize.get();
    }

    public int getChunkLength() {
        int max = chunkSize.get();
        long offset = this.offset.get();
        long dataLength = this.size.get();

        return max <= dataLength - offset ? max : (int) (dataLength - offset);
    }

    public boolean hasMore() {
        return getOffset() < getSize();
    }

    public void add(@NonNull byte[] data) {
        increaseOffset(data.length);
        this.data.add(data);
    }

    public byte[] pollData() {
        return this.data.poll();
    }

    public boolean resetDownloading() {
        boolean wasDownloading = setIsDownloading(false);
        sessionId.set(0);
        size.set(0);
        offset.set(0);
        data.clear();
        return wasDownloading;
    }

    public boolean resetAll() {
        boolean wasDownloading = resetDownloading();
        resetChunkSize();
        return wasDownloading;
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadingState{" +
                "isDownloading=" + isDownloading +
                ", session=" + sessionId +
                ", size=" + size +
                ", offset=" + offset +
                '}';
    }

    private void increaseOffset(int increase) {
        this.offset.addAndGet(increase);
    }
}
