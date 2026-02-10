/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PendingData {

    /**
     * List of the ids that corresponds to the data that have been provided to the sender but haven't been sent to the
     * device yet.
     */
    private final ConcurrentLinkedQueue<Long> mSendingList = new ConcurrentLinkedQueue<>();

    /**
     * List of the data in their FIFO order that needs to be sent once the owner of the pending data is ready.
     */
    private final ConcurrentLinkedQueue<HoldData> mHoldQueue = new ConcurrentLinkedQueue<>();

    public List<Long> getPendingIds() {
        return new ArrayList<>(mSendingList);
    }

    public void clear() {
        mSendingList.clear();
        mHoldQueue.clear();
    }

    public boolean hasHoldData() {
        return !mHoldQueue.isEmpty();
    }

    public HoldData pollHoldData() {
        return mHoldQueue.poll();
    }

    public void addSendingId(long id) {
        mSendingList.offer(id);
    }

    public void removeSendingId(long id) {
        mSendingList.remove(id);

    }

    public void holdData(HoldData data) {
        mHoldQueue.offer(data);
    }
}
