/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;

/**
 * <p>This class manages two queues of {@link SendingData} elements. One queue represents the elements that have been
 * put on hold, the other queue represents the elements that are ready to be sent.</p>
 * <p>Having two queues avoid looking for the element that is ready in the sending queue. It allows to take full
 * advantage of {@link PriorityBlockingQueue#take()} by blocking until an element is available.</p>
 * <p>This class keeps the order of the elements as FIFO:
 * <ul>
 *     <li>offer(#1), offer(#2), offer(#3), take()->#1, take()->#2, take()->#3.</li>
 *     <li>offer(#1), offer(#2), offer(#3), take()->#1, hold(#2), take()->#3.</li>
 *     <li>offer(#1), offer(#2), offer(#3), holdAll(#2), take() is blocked until more data is offered or unhold.</li>
 * </ul></p>
 */
// TODO idea: generic between Gatt and rfcomm?
class SendingQueue {

    /**
     * The tag to use to identify this class when logging.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "SendingQueue";
    /**
     * Contains elements that are ready to be sent.
     */
    private final PriorityBlockingQueue<SendingData> mReadyQueue = new PriorityBlockingQueue<>();
    /**
     * Contains elements that have been put on hold and should not be sent.
     */
    private final PriorityBlockingQueue<SendingData> mHoldQueue = new PriorityBlockingQueue<>();
    /**
     * Create the IDs that are used to cancel or put a packet on hold.
     */
    private final IdCreator idCreator = new IdCreator();

    /**
     * <p>Insert an element to the list as a ready element.</p>
     *
     * @param bytes
     *         The bytes to send.
     * @param isFlushed
     *         True if the data should be flushed.
     * @param listener
     *         The listener attached to the data to send to be notified when the data is about to be sent and has been
     *         sent.
     *
     * @return The id to use if the element should be cancelled or put on hold at some point.
     */
    long offer(@NonNull byte[] bytes, boolean isFlushed, SendListener listener) {
        long id = idCreator.nextId();
        SendingData data = new SendingData(id, bytes, isFlushed, listener);
        mReadyQueue.offer(data);
        return id;
    }

    /**
     * <p>This method looks for the next ready element to send.</p>
     * <p>If not element is ready or available, this method is blocked until an element is available or an exception
     * occurs.</p>
     *
     * @return An element that can be sent, null if an exception has occurred.
     */
    @Nullable
    SendingData take() {
        // blocking not an issue as data queue must have elements for paused to get elements
        try {
            return mReadyQueue.take(); // blocking operation
        }
        catch (Exception e) {
            Log.w(TAG, "[run] exception with take: " + e.getMessage());
            return null;
        }
    }

    /**
     * <p>To cancel all the data that are queued and have the corresponding IDs.</p>
     *
     * @param ids
     *         The ids that corresponds to the data to be cancelled.
     */
    void cancelData(Collection<Long> ids) {
        Predicate<SendingData> predicate = sendingData -> ids.contains(sendingData.getId());
        mHoldQueue.removeIf(predicate);
        mReadyQueue.removeIf(predicate);
    }

    /**
     * <p>To put the data that corresponds to the given IDs on hold.</p>
     *
     * @param ids
     *         The data Ids to put on hold.
     */
    void holdData(Collection<Long> ids) {
        mReadyQueue.removeIf((data) -> {
            if (ids.contains(data.getId())) {
                mHoldQueue.offer(data);
                return true;
            }
            return false;
        });
    }

    /**
     * <p>To resume the sending of the data that was on hold.</p>
     *
     * @param ids
     *         The IDs for which the data should be resumed.
     */
    void resumeData(Collection<Long> ids) {
        mHoldQueue.removeIf((data) -> {
            if (ids.contains(data.getId())) {
                mReadyQueue.offer(data);
                return true;
            }
            return false;
        });
    }

    void clearQueue() {
        mHoldQueue.clear();
        mReadyQueue.clear();
    }
}
