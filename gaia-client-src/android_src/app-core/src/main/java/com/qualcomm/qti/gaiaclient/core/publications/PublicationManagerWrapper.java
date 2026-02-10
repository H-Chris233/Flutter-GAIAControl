/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications;

import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;

public final class PublicationManagerWrapper implements PublicationManager {

    private final PublicationManagerImpl mManager = new PublicationManagerImpl();

    public PublicationManagerWrapper() {
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        mManager.subscribe(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber subscriber) {
        mManager.unsubscribe(subscriber);
    }

    @Override
    public void register(@SuppressWarnings("rawtypes") Publisher publisher) {
        mManager.register(publisher);
    }

    @Override
    public void unregister(@SuppressWarnings("rawtypes") Publisher publisher) {
        mManager.unregister(publisher);
    }

    public void release() {
        mManager.release();
    }
}
