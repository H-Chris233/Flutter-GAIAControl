/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class SendingThread extends Thread {

    private final String TAG = "SendingThread";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.Rfcomm.COMMUNICATION_THREAD;

    private boolean mLogBytes = DEBUG.Bluetooth.DATA;

    private final OutputStream mOutputStream;

    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);

    private final SendingQueue mDataQueue = new SendingQueue();

    SendingThread(@NonNull OutputStream outputStream) {
        setName(TAG + getId());
        mOutputStream = outputStream;
    }

    long sendData(@NonNull byte[] bytes, boolean isFlushed, SendListener listener) {
        long id = mDataQueue.offer(bytes, isFlushed, listener);
        Logger.log(LOG_METHODS, TAG, "sendData", new Pair<>("isFlushed", isFlushed),
                   new Pair<>("id", id));
        return id;
    }

    void holdData(Collection<Long> ids) {
        Logger.log(LOG_METHODS, TAG, "holdData", new Pair<>("isRunning", mIsRunning), new Pair<>("ids", ids));
        mDataQueue.holdData(ids);
    }

    void resumeData(Collection<Long> ids) {
        Logger.log(LOG_METHODS, TAG, "resumeData", new Pair<>("isRunning", mIsRunning), new Pair<>("ids", ids));
        mDataQueue.resumeData(ids);
    }

    void cancelData(Collection<Long> ids) {
        Logger.log(LOG_METHODS, TAG, "cancelData", new Pair<>("isRunning", mIsRunning), new Pair<>("ids", ids));
        mDataQueue.cancelData(ids);
    }

    void cancel() {
        Logger.log(LOG_METHODS, TAG, "cancel", new Pair<>("isRunning", mIsRunning));
        mIsRunning.set(false);
        mDataQueue.clearQueue();
    }

    void setLogBytes(boolean log) {
        Logger.log(LOG_METHODS, TAG, "setLogBytes", new Pair<>("logged", log));
        mLogBytes = log;
    }

    @Override // Thread
    public void run() {
        if (mOutputStream == null) {
            Log.w(TAG, "Run failed: OutputStream is null.");
            return;
        }

        mIsRunning.set(true);

        while (mIsRunning.get()) {
            // get the data to be sent, keeping the FIFO order
            SendingData data = mDataQueue.take(); // operation is blocked until some data is available

            // re-checking the state after a possible blocking operation
            if (data != null && mIsRunning.get()) {
                notifySending(data);
                writeData(data); // can block thread
            }
        }
    }

    /**
     * <p>To write some data on the OutputStream in order to send it to a connected remote
     * device.</p>
     *
     * @param data
     *         the data to send.
     */
    private void writeData(@NonNull SendingData data) {
        Logger.log(LOG_METHODS, TAG, "writeData", new Pair<>("id", data.getId()));

        if (mOutputStream == null) {
            Log.w(TAG, "Sending of data failed: OutputStream is null.");
            return;
        }

        byte[] bytes = data.getData();

        if (bytes == null || bytes.length == 0) {
            Log.w(TAG, "Sending of data failed: data is null or empty.");
            return;
        }

        try {
            Logger.log(mLogBytes, TAG, "writeData", new Pair<>("data", bytes));
            mOutputStream.write(bytes);
            if (data.isFlushed()) {
                // flush the data to make sure the packet is sent immediately.
                // blocks the thread => less efficient for the system and the application
                mOutputStream.flush(); // blocking operation
            }
            notifySent(data);
        }
        catch (IOException e) {
            Log.w(TAG, "Sending of data failed: Exception occurred while writing data:" +
                    " " + e.toString());
            notifyFail(data);
        }
    }

    /**
     * To notify any data listener that the data has been sent.
     */
    private void notifySending(SendingData data) {
        getTaskManager().runInBackground(() -> {
            if (data != null) {
                data.onSending();
            }
        });
    }

    /**
     * To notify any data listener that the data has been sent.
     */
    private void notifySent(SendingData data) {
        getTaskManager().runInBackground(() -> {
            if (data != null) {
                data.onSent();
            }
        });
    }

    /**
     * To notify any data listener that the data could not be sent.
     */
    private void notifyFail(SendingData data) {
        getTaskManager().runInBackground(() -> {
            if (data != null) {
                data.onFailed();
            }
        });
    }
}
