/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2;

import android.util.Log;

import com.qualcomm.qti.gaiaclient.core.gaia.GaiaManager;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2PacketFactory;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

/**
 * Class to override in order to register a Vendor with the {@link GaiaManager} for GAIA V1/V2.
 * This class handles V1 and V2 packets received by the application and that corresponds to the
 * vendor it was registered with.
 */
public abstract class V1V2Vendor extends Vendor {

    /**
     * A tag to display logs within this class.
     */
    private static final String TAG = "V1V2Vendor";
    /**
     * Static debugging boolean to log all the methods calls.
     */
    private static final boolean LOG_METHODS = DEBUG.Gaia.V1V2_VENDOR;

    /**
     * @param id
     *         the ID is used to identify the vendor.
     * @param sender
     *         the sender this vendor should use to send packets.
     */
    public V1V2Vendor(int id, @NonNull GaiaSender sender) {
        super(id, sender);
    }

    /**
     * This method checks if the provided version is v1 or v2.
     * If it is v1 or v2, this method calls {@link #onStarted}.
     * If it is not, this {@link V1V2Vendor} is not supported, and the method calls
     * {@link #onNotSupported()} and {@link #stop()} in order to stop the vendor.
     * The method is made final for v1 and v2 unpacking.
     */
    @Override // Vendor
    protected final void onStarted(int gaiaVersion) {
        Logger.log(LOG_METHODS, TAG, "onStarted", new Pair<>("version", gaiaVersion));

        if (gaiaVersion != GaiaVersion.V1 && gaiaVersion != GaiaVersion.V2) {
            Log.i(TAG, "[onStarted] stopping V1V2Vendor, gaiaVersion=" + gaiaVersion);
            onNotSupported();
            stop();
            return;
        }

        onStarted();
    }

    /**
     * This method builds a {@link V1V2Packet} from the data and calls
     * {@link #handlePacket(V1V2Packet)}.
     * The method is made final for v1 and v2 unpacking.
     */
    @Override // Vendor
    public final void handleData(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "handleData");
        V1V2Packet packet = V1V2PacketFactory.buildPacket(data);
        handlePacket(packet);
    }

    /**
     * <p>Called when this vendor handles a v1v2Packet.</p>
     */
    protected abstract void handlePacket(@NonNull V1V2Packet packet);

    /**
     * This is called when the application is ready for V1/V2 data to be sent and receives by this
     * vendor.
     */
    protected abstract void onStarted();

    /**
     * This is called when the application is ready to send and receive some data but this vendor
     * is not supported.
     */
    protected abstract void onNotSupported();

}
