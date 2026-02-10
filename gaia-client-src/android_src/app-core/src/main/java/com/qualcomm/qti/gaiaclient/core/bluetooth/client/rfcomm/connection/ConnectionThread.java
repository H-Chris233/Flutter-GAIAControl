/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.connection;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * <p>Thread to use in order to connect to a Bluetooth device using a BluetoothSocket.</p>
 * <p>The call to {@link BluetoothSocket#connect()} is synchronous but can take time. In order to
 * avoid to block the calling thread, it is recommended to do it in a dedicated thread.</p>
 */
public class ConnectionThread extends Thread {

    /**
     * <p>The tag to display for logs of this Thread.</p>
     */
    private final String TAG = "ConnectionThread";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.Rfcomm.CONNECTION_THREAD;

    /**
     * A weak reference to the listener attached to this thread.
     */
    private final WeakReference<ConnectionListener> mListenerReference;
    /**
     * The Bluetooth device to connect to.
     */
    private final BluetoothDevice mDevice;
    /**
     * The UUID to connect with when using a RFCOMM channel.
     */
    private final UUID mUuid;

    public ConnectionThread(@NonNull ConnectionListener listener, @NonNull BluetoothDevice device, @NonNull UUID uuid) {
        setName(TAG + getId());
        mListenerReference = new WeakReference<>(listener);
        mDevice = device;
        mUuid = uuid;
    }

    @Override // Thread
    public void run() {
        Logger.log(LOG_METHODS, TAG, "run", new Pair<>("device", mDevice.getAddress()), new Pair<>("uuid", mUuid));

        // create the Bluetooth socket
        BluetoothSocket socket = createSocket(mDevice, mUuid);

        // check socket has been created
        if (socket == null) {
            Log.w(TAG, "[run] Connection failed: creation of a Bluetooth socket failed.");
            notifyFail();
            return;
        }

        // the socket has been created, the connection can start.
        Logger.log(LOG_METHODS, TAG, "run", "Socket connection starts",
                   new Pair<>("device", socket.getRemoteDevice().getAddress()));

        try {
            // Connect to the remote device through the socket.
            // This call blocks until it succeeds or throws an exception.
            socket.connect();
        }
        catch (Exception connectException) {
            // Unable to connect; close the socket and return.
            Log.w(TAG, "Exception while connecting: " + connectException.toString());
            try {
                socket.close();
            }
            catch (Exception closeException) {
                Log.w(TAG, "Could not close the client socket", closeException);
            }
            notifyFail();
            return;
        }

        // connection is successful
        notifySuccess(socket);
    }

    /**
     * To cancel this thread if it was running.
     */
    public void cancel() {
        // stop the thread if still running
        // This is not recommended, however because the BluetoothSocket.connect() method is
        // synchronous it is the only to stop this Thread.
        interrupt();
    }

    /**
     * Creates the RFCOMM bluetooth socket.
     *
     * @return BluetoothSocket object.
     */
    @SuppressLint("MissingPermission")
    private BluetoothSocket createSocket(BluetoothDevice device, UUID uuid) {
        Logger.log(LOG_METHODS, TAG, "createSocket", new Pair<>("device", device.getAddress()),
                   new Pair<>("UUID", uuid));

        try {
            return device.createInsecureRfcommSocketToServiceRecord(uuid);
        }
        catch (IOException e) {
            Log.w(TAG, "Exception occurs while creating Bluetooth socket: " + e.toString());
            Log.i(TAG, "Attempting to invoke method to create Bluetooth Socket.");

            try {
                // This is a workaround that reportedly helps on some older devices like HTC
                // Desire, where using the standard createRfcommSocketToServiceRecord() method
                // always causes connect() to fail.

                //noinspection RedundantArrayCreation,JavaReflectionMemberAccess
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                // noinspection UnnecessaryBoxing
                return (BluetoothSocket) method.invoke(device, Integer.valueOf(1));
            }
            catch (Exception e1) {
                // NoSuchMethodException from method getMethod: impossible to retrieve the method.
                // IllegalArgumentException from method invoke: problem with arguments which
                // don't match with expectations.
                // IllegalAccessException from method invoke: if invoked object is not accessible.
                // InvocationTargetException from method invoke: Exception thrown by the invoked
                // method.
                Log.w(TAG, "Exception occurs while creating Bluetooth socket by invoking method: "
                        + e.toString());
            }
        }

        return null;
    }

    private void notifySuccess(final BluetoothSocket socket) {
        getTaskManager().runInBackground(() -> {
            ConnectionListener listener = mListenerReference.get();
            if (listener != null) {
                listener.onConnectionSuccess(socket);
            }
        });
    }

    private void notifyFail() {
        getTaskManager().runInBackground(() -> {
            ConnectionListener listener = mListenerReference.get();
            if (listener != null) {
                listener.onConnectionFailed();
            }
        });
    }
}
