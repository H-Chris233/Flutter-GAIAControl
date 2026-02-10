/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;

import java.util.Collection;

/**
 * <p>Provides an interface to send  GAIA PDUs to a device. The sender takes care of wrapping the
 * PDU into the format that corresponds to the transport.</p>
 * <p>For instance in the case of RFCOMM transport, the sender adds the extra header to the
 * data.</p>
 */
public interface GaiaSender {

    /**
     * <p>To send some bytes to a device.</p>
     *
     * @param bytes
     *         The data to send to a device. As per convention, this must contain a 4 bytes header
     *         followed by an optional payload.
     *
     * @return the id of the data. The id can be used to cancel or hold the sending of the data if it hasn't been sent
     *         yet. This returns {@link IdCreator#NULL_ID} if
     *         the data could not be sent.
     */
    default long sendData(@NonNull byte[] bytes) {
        return sendData(bytes, false, false, null);
    }

    /**
     * <p>To send some bytes to a device.</p>
     *
     * @param bytes
     *         The data to send to a device. As per convention, this must contain a 4 bytes header
     *         followed by an optional payload.
     * @param isFlushed
     *         True to force the system to flush the data immediately to the device. This is a
     *         blocking operation for the sending of data that can lead to a slower throughput of
     *         data. If set to false, the system flushes the packets itself at the optimum time -
     *         this can lead packets to be sent over a same stream. When using GAIA v1/v2, it is
     *         recommended to flush the packets due to some delay on the device when packets are
     *         on a same stream.
     * @param listener
     *         A listener to be notified when the device has been sent by the application.
     *
     * @return A unique ID that identifies the data. The ID can be used to hold or cancel the data if
     *         it hasn't been sent yet.
     */
    long sendData(@NonNull byte[] bytes, boolean isFlushed, boolean useRwcp, SendListener listener);

    /**
     * To know if the sender has a current connection to a device.
     *
     * @return True if it is connected to a device, false otherwise.
     */
    boolean isConnected();

    /**
     * <p> To determine if the GaiaSender implementation supports holding data / pausing. </p>
     * <p> RFCOMM supports pausing, LE GATT does not support pausing. </p>
     * @return True if it can pause (RFCOMM), false otherwise (LE GATT).
     */
    boolean canHoldData();
    /**
     * <p>To hold all the data that corresponds to the given ids if it hasn't been sent yet.</p>
     * <p>The data will be hold until either it is released with {@link #resumeData(Collection)},
     * {@link #cancelData(Collection)}, or that the sender has been disconnected and its data cleared.</p>
     *
     * @param ids
     *         The ids that corresponds to each bunch of the data that should be hold.
     */
    void holdData(Collection<Long> ids);

    /**
     * <p>To release the data that corresponds to the given ids if it was on hold.</p>
     *
     * @param ids
     *         The ids that corresponds to each bunch of the data that should be sent.
     */
    void resumeData(Collection<Long> ids);

    /**
     * <p>To cancel the sending of the data that corresponds to the given ids.</p>
     * <p>The data will be cancelled whether it is on hold or waiting to be sent.</p>
     *
     * @param ids
     *         The ids that corresponds to the bunch of data that should not be sent anymore.
     */
    void cancelData(Collection<Long> ids);

    /**
     * <p>To get the payload size a connected device can handle for receiving and sending GAIA messages.</p>
     * <p>We recommend using:
     * <ul>
     *     <li>{@link SizeInfo#OPTIMUM_RX_PAYLOAD}: to know the optimum size of a payload the connected device can receive.</li>
     *     <li>{@link SizeInfo#OPTIMUM_TX_PAYLOAD}: to know the optimum size of a payload the connected device would send.</li>
     * </ul></p>
     *
     * @param info
     *         The type of size to get the value of.
     *
     * @return The corresponding size.
     */
    int getMaxPayloadSize(SizeInfo info);

    int getPayloadSize(long transportSize);
}
