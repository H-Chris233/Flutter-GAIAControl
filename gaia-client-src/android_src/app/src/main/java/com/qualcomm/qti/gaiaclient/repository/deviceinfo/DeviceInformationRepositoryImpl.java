/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.deviceinfo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ChargerStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.CrossUpdateVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures.UserFeatures;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DeviceInformationSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ProtocolSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetDeviceInformationRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetEarbudInformationRequest;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DeviceInformationRepositoryImpl extends DeviceInformationRepositoryData {

    private final DeviceInformationSubscriber mDeviceInformationSubscriber =
            new DeviceInformationSubscriber() {
                @Override
                public void onInfo(DeviceInfo info, Object update) {
                    switch (info) {
                        case GAIA_VERSION:
                            int gaiaVersion = (int) update;
                            updateGaiaVersion(gaiaVersion);
                            break;

                        case APPLICATION_VERSION:
                            String applicationVersion = (String) update;
                            updateApplicationVersion(applicationVersion);
                            break;

                        case VARIANT_NAME:
                            String variantName = (String) update;
                            updateVariantName(variantName);
                            break;

                        case SERIAL_NUMBER:
                            String serialNumber = (String) update;
                            updateSerialNumber(serialNumber);
                            break;

                        case USER_FEATURES:
                            UserFeatures features = (UserFeatures) update;
                            updateUserFeatures(features);
                            break;

                        case CHARGER_STATUS:
                            ChargerStatus status = (ChargerStatus) update;
                            updateChargerStatus(status);
                            break;

                        case SYSTEM_INFORMATION:
                            List<SystemInformation> list = (List<SystemInformation>) update;
                            updateSystemInformation(list);
                            break;

                        case CROSS_UPDATE_REQUIRED:
                            CrossUpdateVersion version = (CrossUpdateVersion) update;
                            updateCrossUpdateRequired(version);
                            break;
                    }
                }

                @Override
                public void onError(DeviceInfo info, Reason reason) {
                }
            };

    private final ProtocolSubscriber mProtocolSubscriber = new ProtocolSubscriber() {
        @Override
        public void onProtocolVersion(long version) {
            updateProtocolVersion(version);
        }

        @Override
        public void onError(Object info, Reason reason) {
        }
    };

    private final EarbudSubscriber mEarbudSubscriber = new EarbudSubscriber() {
        @Override
        public void onInfo(EarbudInfo info, Object update, boolean mustRefreshData) {
            switch (info) {
                case EARBUD_POSITION:
                    EarbudPosition position = (EarbudPosition) update;
                    updateEarbudPosition(position);
                    if (mustRefreshData) {
                        fetchDeviceInfo(null, DeviceInfo.SERIAL_NUMBER);
                        fetchEarbudInfo(null, EarbudInfo.EARBUD_POSITION);
                        fetchEarbudInfo(null, EarbudInfo.SECONDARY_SERIAL_NUMBER);
                    }
                    break;

                case SECONDARY_SERIAL_NUMBER:
                    String serialNumber = (String) update;
                    updateSecondarySerialNumber(serialNumber);
                    break;
            }
        }

        @Override
        public void onError(EarbudInfo info, Reason reason) {
        }
    };


    @Inject
    public DeviceInformationRepositoryImpl() {
    }

    @Override
    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mDeviceInformationSubscriber);
        publicationManager.subscribe(mProtocolSubscriber);
        publicationManager.subscribe(mEarbudSubscriber);
    }

    @Override
    public void fetchDeviceInfo(Context context, @NonNull DeviceInfo info) {
        GetDeviceInformationRequest request = new GetDeviceInformationRequest(info);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    @Override
    public void fetchEarbudInfo(Context context, @NonNull EarbudInfo info) {
        GetEarbudInformationRequest request = new GetEarbudInformationRequest(info);
        GaiaClientService.getRequestManager().execute(context, request);
    }
}
