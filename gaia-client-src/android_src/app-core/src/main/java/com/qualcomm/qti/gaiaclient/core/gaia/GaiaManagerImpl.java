/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTransportManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSenderWrapper;
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.GaiaReader;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.V2ApiVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.V2ApiVersionFetcher;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.V2ApiVersionFetcherListener;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DeviceInformationPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ProtocolPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandoverSubscriber;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

final class GaiaManagerImpl {

    private static final String TAG = "GaiaManagerImpl";

    private static final boolean LOG_METHODS = DEBUG.Gaia.GAIA_PROTOCOL_CLIENT;

    private int mGaiaVersion = GaiaVersion.NOT_FETCHED;

    private final VendorHandler mVendorHandler = new VendorHandler();

    /**
     * A wrapper for the DataSender as it can change.
     */
    private final GaiaSenderWrapper mGaiaSender;

    private final V2ApiVersionFetcher mVersionFetcher;

    private final DeviceInformationPublisher mDeviceInformationPublisher =
            new DeviceInformationPublisher();

    private final ProtocolPublisher mProtocolPublisher = new ProtocolPublisher();

    @SuppressWarnings("FieldCanBeLocal")
    private final ConnectionSubscriber mConnectionSubscriber = new ConnectionSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onConnectionStateChanged(Device device, ConnectionState state) {
            Logger.log(LOG_METHODS, TAG, "Connection->StateChanged", new Pair<>("device", device));

            switch (state) {
                case CONNECTED:
                    start();
                    break;

                case DISCONNECTING:
                case DISCONNECTED:
                    stop();
                    break;

                case CONNECTING:
                    // nothing to do
                    break;
            }
        }

        @Override
        public void onConnectionError(Device device, BluetoothStatus reason) {
            Logger.log(LOG_METHODS, TAG, "Connection->Error",
                       new Pair<>("device", device), new Pair<>("reason", reason));
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final V2ApiVersionFetcherListener mVersionListener = new V2ApiVersionFetcherListener() {
        @Override
        public void onVersion(V2ApiVersion version) {
            GaiaManagerImpl.this.onVersion(version);
        }

        @Override
        public void onError(Reason reason) {
            GaiaManagerImpl.this.onError(reason);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final HandoverSubscriber mHandoverSubscriber = new HandoverSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onHandoverStart(HandoverInformation info) {
            Logger.log(LOG_METHODS, TAG, "Handover->onHandoverStart", new Pair<>("info", info));

            // during handover no messages can be sent or received
            if (info.getType() == HandoverType.STATIC) {
                // must stop to send data
                stop(); // stop each plugin that clear their data on the sender

                // static handover triggers a disconnection => reconnection delegate responsibility
            }

            // dynamic handover is ignored as states are kept on the device
            // specific behaviour is managed by at the plugin/feature level
        }
    };

    GaiaManagerImpl(PublicationManager publicationManager) {
        publicationManager.register(mDeviceInformationPublisher);
        publicationManager.register(mProtocolPublisher);
        publicationManager.subscribe(mConnectionSubscriber);
        publicationManager.subscribe(mHandoverSubscriber);
        mGaiaSender = new GaiaSenderWrapper(publicationManager);
        mVersionFetcher = new V2ApiVersionFetcher(mVersionListener, mGaiaSender);
    }

    void release() {
        Logger.log(LOG_METHODS, TAG, "release");
        mGaiaVersion = GaiaVersion.NOT_FETCHED;
        mGaiaSender.setTransport(null);
        mVendorHandler.release();
    }

    GaiaSender getGaiaSender() {
        return mGaiaSender;
    }

    VendorHandler getVendorHandler() {
        return mVendorHandler;
    }

    GaiaReader.Listener getReaderListener() {
        return this::onDataFound;
    }

    private void start() {
        mGaiaSender.setTransport(getTransportManager().getGaiaTransport());
        fetchVersion();
    }

    private void stop() {
        mGaiaVersion = GaiaVersion.NOT_FETCHED;

        // setting sender to null to stop any message to be sent
        mGaiaSender.setTransport(null);

        // stopping the vendors and their plugins
        mVendorHandler.stop();
    }

    private void onDataFound(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onDataFound", new Pair<>("gaiaVersion", mGaiaVersion),
                   new Pair<>("data", data));
        if (mGaiaVersion == GaiaVersion.NOT_FETCHED) {
            mVersionFetcher.onIncomingData(data);
        }
        else {
            mVendorHandler.handleData(data);
        }
    }

    private void fetchVersion() {
        Logger.log(LOG_METHODS, TAG, "fetchVersion");
        // re initialising the version
        mGaiaVersion = GaiaVersion.NOT_FETCHED;
        // all devices must support the GAIA V1 GET_API_VERSION command
        mVersionFetcher.start();
    }

    private void onVersion(V2ApiVersion version) {
        mVersionFetcher.stop();
        this.mGaiaVersion = version.getGaiaVersion();
        mDeviceInformationPublisher.publishInfo(DeviceInfo.GAIA_VERSION, version.getGaiaVersion());
        mProtocolPublisher.publishProtocolVersion(version.getProtocolVersion());
        publishDefaultProtocolValues();
        mVendorHandler.start(version.getGaiaVersion());
    }

    private void publishDefaultProtocolValues() {
        int defaultSize = mGaiaSender.getMaxPayloadSize();
        for (SizeInfo info : SizeInfo.VALUES) {
            mProtocolPublisher.publishSizeInfo(info, defaultSize);
        }
    }

    private void onError(Reason reason) {
        Log.w(TAG, "[onError] Not possible to discover API version as fetching the version " +
                "resulted in error=" + reason);
        mVersionFetcher.stop();
        mDeviceInformationPublisher.publishError(DeviceInfo.GAIA_VERSION, reason);
        mProtocolPublisher.publishError(ProtocolInfo.PROTOCOL_VERSION, reason);
    }

}
