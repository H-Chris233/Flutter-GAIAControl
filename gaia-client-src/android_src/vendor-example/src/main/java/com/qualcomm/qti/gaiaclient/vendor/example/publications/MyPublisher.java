/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.publications;

import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;

/*
 * Example code to create a Publisher.
 */
public class MyPublisher extends Publisher<MySubscriber> {

    @Override
    public Subscription getSubscription() {
        /*
         * Provide the ID that Subscribers will use to get the publications from this publisher.
         * The ID must be unique to this mapping.
         */
        return MySubscriptions.MY_SUBSCRIPTION_1;
    }

    /*
     * we recommend implementing wrapper methods that calls the forEachSubscriber method to
     * notify each subscriber and provide the action that must be taken for this subscriber.
     * This example shows:
     *   - #publishSomething matches MySubscriber#somethingHappened.
     *   - #publishSomethingElse matches MySubscriber#somethingElseHappened.
     */

    public void publishSomething() {
        forEachSubscriber(MySubscriber::somethingHappened);
    }

    public void publishSomethingElse() {
        forEachSubscriber(MySubscriber::somethingElseHappened);
    }
}
