/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.features;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.QTILFeaturesSubscriber;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FeaturesRepositoryImpl extends FeaturesRepositoryData {

    private final QTILFeaturesSubscriber mFeatureSubscriber = new QTILFeaturesSubscriber() {
        @Override
        public void onFeatureSupported(QTILFeature feature, int version) {
            addFeature(feature, version);
        }

        @Override
        public void onFeatureNotSupported(QTILFeature feature, Reason reason) {
            removeFeature(feature);
        }

        @Override
        public void onError(Reason reason) {
            FeaturesRepositoryImpl.this.onError(matchFeatureReason(reason));
        }
    };

    @Inject
    public FeaturesRepositoryImpl() {
    }

    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mFeatureSubscriber);
    }

    private FeatureReason matchFeatureReason(Reason reason) {
        switch (reason) {
            case NOT_AVAILABLE:
            case NOT_SUPPORTED:
            case AUTHENTICATION:
                return FeatureReason.NOT_AVAILABLE;

            case NO_RESPONSE:
            case MALFORMED_REQUEST:
            case SENDING_FAILED:
                return FeatureReason.FAILED;

            case NOTIFICATION_NOT_SUPPORTED:
                return FeatureReason.NOTIFICATION_NOT_SUPPORTED;

            case FILE_WRITING_FAILED: // unexpected
            case LOGS_NO_DATA: // unexpected
            case LOGS_NO_DEBUG_PARTITION: // unexpected
            case DISCONNECTED: // unexpected
            case FILE_CREATION_FAILED: // unexpected
            case UNKNOWN:
            default:
                return FeatureReason.UNKNOWN;
        }
    }

}
