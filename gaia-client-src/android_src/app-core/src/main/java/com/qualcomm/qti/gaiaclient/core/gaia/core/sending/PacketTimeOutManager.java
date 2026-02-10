/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending;

import android.util.Log;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.utils.SynchronizedListMap;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

public class PacketTimeOutManager {

    /**
     * A tag to log information from this class.
     */
    private static final String TAG = "TImeOutPacketManager";
    /**
     * A map of runnable that are running to represent the time outs for acknowledgements.
     */
    private final SynchronizedListMap<Integer, TimeOutRunnable> mTimeOutRunnableMap =
            new SynchronizedListMap<>();

    private final TimeOutListener mListener;

    public PacketTimeOutManager(TimeOutListener listener) {
        this.mListener = listener;
    }

    /**
     * Builds a {@link TimeOutRunnable} for the corresponding packet and starts it.
     *
     * @param timeout
     *         the time out in milliseconds.
     */
    public void startTimeOutRunnable(@NonNull GaiaPacket packet, long timeout) {
        TimeOutRunnable runnable = new TimeOutRunnable(packet, timeout);
        mTimeOutRunnableMap.put(packet.getKey(), runnable);
        long id = getTaskManager().schedule(runnable, timeout);
        runnable.setId(id);
    }

    /**
     * To cancel a {@link TimeOutRunnable} that is running for a packet that matches the given
     * packet. This is expected to be called when the plugin receives a packet to identify it as
     * an acknowledgement ang get the packet it acknowledges.
     *
     * @return The packet it acknowledges if any match was found, returns null otherwise.
     */
    public GaiaPacket cancelTimeOutRunnable(int key) {
        TimeOutRunnable runnable = mTimeOutRunnableMap.poll(key);
        if (runnable != null) {
            getTaskManager().cancelScheduled(runnable.id);
        }
        return runnable != null ? runnable.packet : null;
    }

    /**
     * To cancel all {@link TimeOutRunnable}.
     */
    public void reset() {
        mTimeOutRunnableMap.clear((runnable) -> {
            if (runnable != null) {
                getTaskManager().cancelScheduled(runnable.id);
            }
        });
    }

    /**
     * A Runnable to time out packets that have not been acknowledged.
     */
    private class TimeOutRunnable implements Runnable {

        @NonNull
        private final GaiaPacket packet;

        private final long timeout;

        private long id = IdCreator.NULL_ID;

        TimeOutRunnable(@NonNull GaiaPacket packet, long timeout) {
            this.packet = packet;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            Log.w(TAG, "[TimeOutRunnable->run] Unacknowledged within " + timeout + " seconds, " +
                    "packet=" + packet);

            mTimeOutRunnableMap.remove(packet.getKey(), this);
            mListener.onTimeOut(packet);
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    public interface TimeOutListener {
        void onTimeOut(GaiaPacket packet);
    }
}
