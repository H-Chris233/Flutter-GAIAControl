/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.vendors;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v1v2.V1V2QTILPlugin;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

public final class QTILV1V2Vendor extends V1V2Vendor implements QTILVendor {

    private static final String TAG = "QTILV1V2Vendor";

    private static final boolean LOG_METHODS = DEBUG.QTIL.QTIL_VENDOR;

    @Nullable
    private V1V2QTILPlugin mv1v2Client = null;

    private final UpgradeHelper mUpgradeHelper;

    public QTILV1V2Vendor(@NonNull GaiaSender sender,
                          @NonNull UpgradeHelper upgradeHelper) {
        super(QTILVendorIDs.QTIL_V1V2_VENDOR_ID, sender);
        mUpgradeHelper = upgradeHelper;
    }

    @Override
    protected void onStarted() {
        Logger.log(LOG_METHODS, TAG, "onStarted");
        mv1v2Client = new V1V2QTILPlugin(getSender(), mUpgradeHelper);
        mv1v2Client.start();
    }

    @Override
    protected void onNotSupported() {

    }

    @Override
    protected void onStopped() {
        Logger.log(LOG_METHODS, TAG, "onStopped");
        if (mv1v2Client != null) {
            mv1v2Client.stop();
            mv1v2Client = null;
        }
    }

    @Override
    public void handlePacket(@NonNull V1V2Packet packet) {
        Logger.log(LOG_METHODS, TAG, "handlePacket", new Pair<>("packet", packet));

        if (mv1v2Client != null) {
            mv1v2Client.onReceiveGaiaPacket(packet);
        }
    }

    @Override // QTILVendor
    public void release() {
        Logger.log(LOG_METHODS, TAG, "release");
        if (mv1v2Client != null) {
            mv1v2Client.stop();
            mv1v2Client = null;
        }
    }

    @Override // QTILVendor
    public Plugin getPlugin(@NonNull QTILFeature feature) {
        Logger.log(LOG_METHODS, TAG, "getPlugin", new Pair<>("feature", feature));
        return feature.equals(QTILFeature.UPGRADE) || feature.equals(QTILFeature.BASIC) ?
                mv1v2Client : null;
    }
}
