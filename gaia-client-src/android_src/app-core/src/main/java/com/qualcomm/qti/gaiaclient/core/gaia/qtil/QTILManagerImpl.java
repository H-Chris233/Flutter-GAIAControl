/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.GaiaManager;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.vendors.QTILV1V2Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.vendors.QTILV3Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.vendors.QTILVendor;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DeviceInformationSubscriber;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelperWrapper;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.concurrent.ConcurrentHashMap;

final class QTILManagerImpl {

    private static final String TAG = "QTILManagerImpl";

    private static final boolean LOG_METHODS = DEBUG.QTIL.QTIL_VENDOR;

    private int mGaiaVersion = GaiaVersion.NOT_FETCHED;

    private final ConcurrentHashMap<Integer, QTILVendor> mQtilVendors =
            new ConcurrentHashMap<>();

    private final UpgradeHelper mUpgradeHelper;

    @SuppressWarnings("FieldCanBeLocal")
    private final DeviceInformationSubscriber mVersionsSubscriber = new DeviceInformationSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onInfo(DeviceInfo info, Object update) {
            Logger.log(LOG_METHODS, TAG, "DeviceInfo->onInfo", new Pair<>("info", info));
            if (info == DeviceInfo.GAIA_VERSION) {
                mGaiaVersion = (int) update;
            }
        }

        @Override
        public void onError(DeviceInfo info, Reason reason) {
            Logger.log(LOG_METHODS, TAG, "DeviceInfo->onError", new Pair<>("info", info),
                       new Pair<>("reason", reason));
            if (info == DeviceInfo.GAIA_VERSION) {
                Log.w(TAG, "[DeviceInformationSubscriber->onError] Not " +
                        "possible to discover API version as fetching " + info
                        + " resulted in error=" + reason);
                mGaiaVersion = GaiaVersion.NOT_FETCHED;
            }
        }
    };

    /**
     * Listen for connection states in order to update the state when reconnecting.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final ConnectionSubscriber mConnectionSubscriber = new ConnectionSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onConnectionStateChanged(Device device, ConnectionState state) {
            Logger.log(LOG_METHODS, TAG, "Subscriber->onConnectionStateChanged", new Pair<>(
                    "state", state));
            if (state != ConnectionState.CONNECTED) {
                mGaiaVersion = GaiaVersion.NOT_FETCHED;
            }
        }

        @Override
        public void onConnectionError(Device device, BluetoothStatus reason) {
            mGaiaVersion = GaiaVersion.NOT_FETCHED;
        }
    };

    QTILManagerImpl(@NonNull GaiaManager gaiaManager,
                    @NonNull PublicationManager publicationManager) {
        mUpgradeHelper = new UpgradeHelperWrapper(publicationManager);
        addV1V2Vendor(gaiaManager, mUpgradeHelper);
        addV3Vendor(publicationManager, gaiaManager, mUpgradeHelper);
        publicationManager.subscribe(mVersionsSubscriber);
        publicationManager.subscribe(mConnectionSubscriber);
    }

    private void addV1V2Vendor(@NonNull GaiaManager manager, @NonNull UpgradeHelper helper) {
        QTILV1V2Vendor vendor = new QTILV1V2Vendor(manager.getSender(), helper);
        mQtilVendors.put(GaiaVersion.V1, vendor);
        mQtilVendors.put(GaiaVersion.V2, vendor);
        manager.registerVendor(vendor);
    }


    private void addV3Vendor(@NonNull PublicationManager publicationManager,
                             @NonNull GaiaManager gaiaManager, @NonNull UpgradeHelper helper) {
        QTILV3Vendor vendor = new QTILV3Vendor(publicationManager, gaiaManager.getSender(), helper);
        mQtilVendors.put(GaiaVersion.V3, vendor);
        gaiaManager.registerVendor(vendor);
    }

    public void release() {
        Logger.log(LOG_METHODS, TAG, "release");
        mUpgradeHelper.release();
        for (QTILVendor vendor : mQtilVendors.values()) {
            vendor.release();
        }
        mQtilVendors.clear();
    }

    Plugin getPlugin(@NonNull QTILFeature feature) {
        Logger.log(LOG_METHODS, TAG, "getPlugin", new Pair<>("feature", feature));
        QTILVendor vendor = mQtilVendors.get(mGaiaVersion);
        return vendor != null ? vendor.getPlugin(feature) : null;
    }

    @NonNull
    UpgradeHelper getUpgradeHelper() {
        return mUpgradeHelper;
    }
}
