/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3;

import android.util.Log;

import com.qualcomm.qti.gaiaclient.core.gaia.GaiaManager;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketFactory;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getHexadecimalStringFromInt;

/**
 * Class to override in order to register a Vendor for GAIA v3 with the {@link GaiaManager}.
 * This class handles V3 packets received by the application and that corresponds to the given
 * vendor ID.
 */
public abstract class V3Vendor extends Vendor {

    /**
     * A tag to display logs within this class.
     */
    private static final String TAG = "V3Vendor";
    /**
     * Static debugging boolean to log all the methods calls.
     */
    private static final boolean LOG_METHODS = DEBUG.Gaia.V3_VENDOR;
    /**
     * Contains the list of plugins that have been registered for this vendor.
     */
    private final ConcurrentHashMap<Integer, V3Plugin> mPlugins = new ConcurrentHashMap<>();

    /**
     * @param id
     *         the ID is used to identify the vendor.
     * @param sender
     *         the sender this vendor should use to send packets.
     */
    public V3Vendor(int id, @NonNull GaiaSender sender) {
        super(id, sender);
    }

    /**
     * <p>To register a plugin for this vendor.</p>
     * <p>The Plugin is used when this vendor receives some data from a connected device.
     * The vendor checks the feature of the received data and calls the corresponding
     * registered plugin.</p>
     * <p>This method only registers the plugin and does not start it.</p>
     * <p>If a plugin was already registered for the corresponding feature this method replaced
     * it with the new plugin.</p>
     *
     * @param plugin
     *         A plugin to handle V3 data for a feature of this vendor.
     */
    protected void addPlugin(@NonNull V3Plugin plugin) {
        Logger.log(LOG_METHODS, TAG, "addPlugin", new Pair<>("feature", plugin.getFeature()));

        if (getVendorId() != plugin.getVendor()) {
            Log.w(TAG, String.format("[addPlugin] plugin vendor=%1$s does not match this " +
                                             "vendor=%2$s", plugin.getVendor(), getVendorId()));
        }

        int feature = plugin.getFeature();
        if (mPlugins.containsKey(feature)) {
            Log.w(TAG, "[addPlugin] Replacing plugin for feature=" + feature);
        }

        mPlugins.put(feature, plugin);
    }

    /**
     * <p>To unregister a plugin from this vendor. This could be of used if a feature of this
     * vendor was not supported.</p>
     *
     * @param feature
     *         The feature to remove from this vendor.
     *
     * @return the plugin that has been removed from this vendor. This method return null if no
     * plugin was registered for the given feature.
     */
    @Nullable
    protected V3Plugin removePlugin(int feature) {
        Logger.log(LOG_METHODS, TAG, "removePlugin", new Pair<>("feature", feature));
        return mPlugins.remove(feature);
    }

    /**
     * This method checks if the provided version is v3.
     * If it is v3, this method calls {@link #onStarted}.
     * If it is not v3, this {@link V3Vendor} is not supported, and the method calls
     * {@link #onNotSupported()} and {@link #stop()} in order to stop the vendor.
     * The method is made final for v3 unpacking.
     */
    @Override // Vendor
    protected final void onStarted(int gaiaVersion) {
        Logger.log(LOG_METHODS, TAG, "onStarted", new Pair<>("version", gaiaVersion));
        if (gaiaVersion != GaiaVersion.V3) {
            Log.i(TAG, "[onStarted] stopping V3Vendor, gaiaVersion=" + gaiaVersion);
            onNotSupported();
            stop();
            return;
        }

        onStarted();
    }

    /**
     * This method builds a {@link V3Packet} from the data and finds the plugin that corresponds
     * to the packet feature as registered with {@link #addPlugin(V3Plugin)}.
     * If no plugin were found, this method calls {@link #onUnhandledPacket(V3Packet)};
     * The method is made final for v3 unpacking.
     */
    @Override // Vendor
    public final void handleData(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "handleData");
        V3Packet packet = V3PacketFactory.buildPacket(data);
        int feature = packet.getFeature();

        Plugin plugin = mPlugins.get(feature);
        if (plugin != null) {
            plugin.onReceiveGaiaPacket(packet);
        }
        else {
            onUnhandledPacket(packet);
        }
    }

    /**
     * To get the plugin that corresponds to the given feature.
     *
     * @param feature
     *         the feature to get the plugin for.
     *
     * @return The v3 plugin that corresponds to the given feature or null if no plugin could be
     * found.
     */
    public V3Plugin getPlugin(int feature) {
        Logger.log(LOG_METHODS, TAG, "getPlugin", new Pair<>("feature", feature));
        return mPlugins.get(feature);
    }

    /**
     * This method calls {@link Plugin#start()} for all its registered plugins.
     */
    public void startAll() {
        for (Plugin plugin : mPlugins.values()) {
            plugin.start();
        }
    }

    /**
     * This method calls {@link Plugin#stop()} for all its registered plugins.
     */
    public void stopAll() {
        Logger.log(LOG_METHODS, TAG, "stopAll", new Pair<>("count=", mPlugins.size()));
        for (Plugin plugin : mPlugins.values()) {
            plugin.stop();
        }
    }

    /**
     * This method clears all its registered plugins.
     */
    public void removeAll() {
        Logger.log(LOG_METHODS, TAG, "unregisterAll", new Pair<>("count=", mPlugins.size()));
        mPlugins.clear();
    }

    /**
     * This is called when the application is ready for V3 data to be sent and receives by this
     * vendor.
     */
    protected abstract void onStarted();

    /**
     * This is called when the application is ready to send and receive some data but V3 is not
     * supported.
     */
    protected abstract void onNotSupported();

    /**
     * <p>Packets handled by this vendor that do not have a corresponding plugin are sent to
     * this method.</p>
     * <p>Children classes can extend this method.</p>
     *
     * @param packet
     *         The V3 packet with no corresponding v3 plugin.
     */
    protected void onUnhandledPacket(V3Packet packet) {
        Log.w(TAG,
              String.format("[handleData] Vendor %1$s: no plugin implemented for feature=%2$s",
                            getHexadecimalStringFromInt(getVendorId()),
                            getHexadecimalStringFromInt(packet.getFeature())));
    }

}
