/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.StreamReader;

import java.io.IOException;
import java.util.Collection;

public class Communicator {

    private static final String TAG = "Communicator";

    private ReceivingThread receivingThread;

    private SendingThread sendingThread;

    private BluetoothSocket socket;

    private final ReceivingListener listener;

    public Communicator(@NonNull BluetoothSocket socket, @NonNull ReceivingListener listener,
                        @NonNull StreamReader reader) {
        this.socket = socket;
        this.listener = listener;

        ReceivingThread receivingThread = null;
        SendingThread sendingThread = null;

        try {
            receivingThread = new ReceivingThread(listener, this.socket.getInputStream(), reader);
            sendingThread = new SendingThread(this.socket.getOutputStream());
        }
        catch (Exception e) {
            Log.e(TAG, "[Communicator] Error during initialisation: ", e);
            listener.onCommunicationFailed(CommunicationError.INITIALISATION_FAILED);
            // closing it all
            release();
        }

        this.receivingThread = receivingThread;
        this.sendingThread = sendingThread;
    }

    public void start() {
        if (!socket.isConnected()) {
            Log.w(TAG, "[start] BluetoothSocket is not connected.");
            listener.onCommunicationFailed(CommunicationError.INITIALISATION_FAILED);
            return;
        }

        if (receivingThread != null) {
            receivingThread.start();
        }

        if (sendingThread != null) {
            sendingThread.start();
        }
    }

    public void cancel() {
        if (receivingThread != null) {
            receivingThread.cancel();
            receivingThread = null;
        }
        if (sendingThread != null) {
            sendingThread.cancel();
            sendingThread = null;
        }

        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {
                Log.w(TAG,
                      "[cancel] Closing BluetoothSocket failed: " + e.getMessage());
            }
            socket = null;
        }
    }

    public long sendData(@NonNull byte[] bytes, boolean isFlushed, SendListener listener) {
        // Create temporary object
        SendingThread thread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (sendingThread == null) {
                // unexpected:
                Log.w(TAG, "[sendData] No sending thread running");
                return IdCreator.NULL_ID;
            }

            thread = sendingThread;
        }

        // Perform a non synchronised write
        return thread.sendData(bytes, isFlushed, listener);
    }

    public void holdData(Collection<Long> ids) {
        if (sendingThread != null) {
            sendingThread.holdData(ids);
        }
    }

    public void resumeData(Collection<Long> ids) {
        if (sendingThread != null) {
            sendingThread.resumeData(ids);
        }
    }

    public void cancelData(Collection<Long> ids) {
        if (sendingThread != null) {
            sendingThread.cancelData(ids);
        }
    }

    public void setLogBytes(boolean log) {
        if (sendingThread != null) {
            sendingThread.setLogBytes(log);
        }
        if (receivingThread != null) {
            receivingThread.setLogBytes(log);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Communicator{" +
                "listeningThread=" + receivingThread +
                ", sendingThread=" + sendingThread +
                ", socket=" + socket +
                '}';
    }

    private void release() {
        cancel();
    }
}
