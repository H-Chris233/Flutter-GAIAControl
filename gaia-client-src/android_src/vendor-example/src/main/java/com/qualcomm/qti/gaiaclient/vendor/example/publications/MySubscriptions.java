/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.publications;

import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;

/*
 * An example implementation of Subscription. Those must be unique through the whole application.
 * Also, subscriptions must be mapped with a Publisher and a Subscriber.
 */
public enum MySubscriptions implements Subscription {

    /**
     * This subscription is mapped with:
     * <ul>
     * <li>Publisher: {@link MyPublisher}</li>
     * <li>Subscriber: {@link MySubscriber}</li>
     * </ul>
     */
    MY_SUBSCRIPTION_1

}
