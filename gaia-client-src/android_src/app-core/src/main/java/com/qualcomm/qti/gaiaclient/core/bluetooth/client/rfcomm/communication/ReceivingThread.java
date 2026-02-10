/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.StreamReader;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Thread to use in order to listen for incoming message and send messages from/to a connected
 * BluetoothDevice.</p>
 * <p>To get messages from a remote device connected using a BluetoothSocket, an application has
 * to constantly read bytes over the InputStream of the BluetoothSocket. In order to avoid to block
 * the calling thread, it is recommended to do it in a dedicated thread.</p>
 */
public class ReceivingThread extends Thread {

    /**
     * <p>The tag to display for logs.</p>
     */
    private final String TAG = "ReceivingThread";
    /**
     * The receiver for incoming bytes.
     */
    final private WeakReference<StreamReader> mAnalyser;
    /**
     * The InputStream object to read bytes from in order to get messages from a connected
     * remote device.
     */
    private final InputStream mInputStream;
    /**
     * To constantly read messages coming from the remote device.
     */
    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
    /**
     * The listener is kept as a reference.
     */
    private final WeakReference<ReceivingListener> mListenerReference;
    /**
     * Boolean to log the bytes that are received in the application logs.
     */
    private boolean mLogBytes = DEBUG.Bluetooth.DATA;
    /**
     * Static debugging boolean to log all the methods calls.
     */
    private static final boolean LOG_METHODS = DEBUG.Bluetooth.Rfcomm.COMMUNICATION_THREAD;

    /**
     * <p>To create a new instance of this class.</p>
     *
     * @param listener
     *         The listener to receive messages from this thread.
     */
    ReceivingThread(@NonNull ReceivingListener listener,
                    @NonNull InputStream inputStream, @NonNull StreamReader analyser) {
        setName(TAG + getId());
        mListenerReference = new WeakReference<>(listener);
        mAnalyser = new WeakReference<>(analyser);
        mInputStream = inputStream;
    }

    /**
     * To cancel this thread if it was running.
     */
    void cancel() {
        Logger.log(LOG_METHODS, TAG, "cancel", new Pair<>("isRunning", mIsRunning));

        if (mIsRunning.get()) {
            endConnection();
        }
    }

    void setLogBytes(boolean log) {
        mLogBytes = log;
    }

    @Override // Thread
    public void run() {
        if (mInputStream == null) {
            Log.w(TAG, "Run thread failed: InputStream is null.");
            notifyFailed(CommunicationError.INITIALISATION_FAILED);
            return;
        }

        // all check passed successfully, the listening can start
        listenStream();

        // stream has stopped either due to an exception (link loss?) or a disconnection
        endConnection();
    }

    /**
     * <p>This method runs the constant read of the InputStream in order to get messages from
     * the connected remote device.</p>
     */
    private void listenStream() {
        Logger.log(LOG_METHODS, TAG, "listenStream", "starts");

        final int MAX_BUFFER = 1024;
        byte[] buffer = new byte[MAX_BUFFER];

        mIsRunning.set(true);
        // to inform subclass it is possible to communicate with the device
        notifyReady();

        while (mIsRunning.get()) {
            int length;
            try {
                length = mInputStream.read(buffer);
            }
            catch (IOException e) {
                Log.e(TAG, "Reception of data failed: exception occurred while " +
                        "reading: " + e.toString());
                if (mIsRunning.get()) {
                    notifyFailed(CommunicationError.CONNECTION_LOST);
                }
                break;
            }

            // if buffer contains some bytes, they are sent to the listener
            if (length > 0) {
                byte[] data = new byte[length];
                System.arraycopy(buffer, 0, data, 0, length);

                Logger.log(mLogBytes, TAG, "listenStream", new Pair<>("data", data));

                processData(mAnalyser, data);
            }
        }

        Logger.log(LOG_METHODS, TAG, "listenStream", "ends");
    }

    private void endConnection() {
        mIsRunning.set(false);
        notifyEnded();
    }

    private void processData(@NonNull WeakReference<StreamReader> analyserReference,
                             byte[] data) {
        if (data == null) {
            Log.w(TAG, "[processData] data is null");
            return;
        }

        StreamReader analyser = analyserReference.get();

        if (analyser == null) {
            Log.w(TAG, "[processData] analyser is null");
            return;
        }

        // runs the analysis on this thread to keep the streams synchronous
        analyser.read(getTaskManager(), data);
    }

    private void notifyReady() {
        getTaskManager().runInBackground(() -> {
            ReceivingListener listener = mListenerReference.get();
            if (listener != null) {
                listener.onCommunicationReady();
            }
        });
    }

    private void notifyEnded() {
        getTaskManager().runInBackground(() -> {
            ReceivingListener listener = mListenerReference.get();
            if (listener != null) {
                listener.onCommunicationEnded();
            }
        });

    }

    private void notifyFailed(CommunicationError reason) {
        getTaskManager().runInBackground(() -> {
            ReceivingListener listener = mListenerReference.get();
            if (listener != null) {
                listener.onCommunicationFailed(reason);
            }
        });
    }

}
