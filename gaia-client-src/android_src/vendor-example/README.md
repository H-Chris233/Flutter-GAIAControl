# Specific vendor ID example
This `vendor-example` module shows how to implement a GAIA vendor by using the `app-core` library
within this application.

This module exposes how to implement a vendor and how to interact with the `app-core` module.

:warning: This module is only an example code and is not part of the application. See
[Adding the `vendor-example` module as a dependency](#adding-the-vendor-example-module-as-a-dependency) to add it to the application.

## `app-core` APIs overview

The `app-core` module provides the following components to implement:
* [Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/Vendor.java) to
  receive and send data to a device.
* [Publisher](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Publisher.java)
  to publish an event that other elements can listen to.
* [Subscriber](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Subscriber.java)
  to receive published events.
* [Request](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/requests/core/Request.java)
  to encapsulate a process or an action - for instance getting some information from a connected
  device.

The `app-core` module provides the following managers to register and subscribes those components:
* [GaiaManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/GaiaManager.java) to register
  a specific [Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/Vendor.java).
* [PublicationManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/PublicationManager.java)
  to register
  [Publisher](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Publisher.java)
  and [Subscriber](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Subscriber.java).
* [RequestManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/requests/RequestManager.java)
  to execute [Request](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/requests/core/Request.java).
* [TaskManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/tasks/TaskManager.java)
  to allow other components to run tasks in background to avoid overloading the UI thread.  
  *Note: All requests are run in background. Publications are run in background if the
  [Subscriber](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Subscriber.java)
  specified the corresponding execution type.*

Those managers can be accessed from the static methods in
[GaiaClientService](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/GaiaClientService.java).


## 1. Implementing a GAIA Vendor
The `app-core` module allows a
[Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/Vendor.java) to be
registered to send and receive packets for a specific vendor ID. The `app-core` module provides
tools to implement a v3 vendor  that u

### 1.1. A GAIA v3 vendor with features *\*\* recommended \*\**
To implement a GAIA vendor that uses GAIA v3, the `app-core` module provides the following classes
to extend:
* [V3Plugin](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/v3/V3Plugin.java):
  manages GAIA v3 packets for a specific feature ID. This is where packets can be analysed.
* [V3Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/v3/V3Vendor.java):
  manages a set of V3 plugins.

Look at
[MyV3Vendor](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/MyV3Vendor.java)
and
[MyV3Plugin](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/MyV3Plugin.java)
for implementation examples.


### 1.2. Other GAIA vendors
Other types of vendors can be implemented:
* [V1V2Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/v1v2/V1V2Vendor.java)
  to use the GAIA v1/v2 formatting that contains a command ID and acknowledgement masks. As vendors
  are meant to manage a set of plugins/features, the V1V2Vendor can use a
  [V1V2Plugin](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/v1v2/V1V2Plugin.java)
  to manage v1/v2 packets.
* [Vendor](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/core/Vendor.java) to
  implement a different formatting.

Look at
[MyV1V2Vendor](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/alternatives/MyV1V2Vendor.java),
[MyV1V2Plugin](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/alternatives/MyV1V2Plugin.java)
and
[MyRawVendor](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/alternatives/MyRawVendor.java)
for implementation examples.


## 2. Interacting with the `app-core` module and the application
This section shows how the vendor can interact with the rest of the application using the managers
provided by the `app-core` module.

Look at [UseExample](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/UseExample.java) for an
example of use of the managers. That class only shows the code to write in order to use the
managers. It does not link ths module to the application. See
[Adding the `vendor-example` module as a dependency](#adding-the-vendor-example-module-as-a-dependency)
to link it to the application.

### 2.1. Registering the vendor
For the vendor to receive and send packets, it must be registered with the
[GaiaManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/gaia/GaiaManager.java) as
follows:
```java
    private MyV3Vendor myVendor;
    
    void initMyV3Vendor() {
        // getting the manager
        GaiaManager gaiaManager = GaiaClientService.getGaiaManager();
        
        // the example V3 vendor requires the publication manager
        PublicationManager publicationManager = GaiaClientService.getPublisherManager();

        // creating the vendor
        myVendor = new MyV3Vendor(gaiaManager.getSender(), publicationManager);
        
        // registering the vendor
        gaiaManager.registerVendor(myVendor);
    }
```

To ensure all resources are released when the application is terminated, make sure to release the
vendor and plugin(s):
```java
    void releaseVendor() {
        myVendor.stopAll();
        myVendor.removeAll();
    }
```

*If implemented in this application, the registration and the release could be done in the
`GaiaClientApplication` class of the `app` module.*


### 2.2. Publishing
The `app-core` library provides a publication system in order for components (publishers) to
dispatch events to other components (subscribers) that are interested by them.

1. Define a
   [Subscription](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Subscription.java).  
   Subscriptions must be unique in the whole application. To ease the development they can be
   defined within a enumeration:
   ```java
   public enum MySubscriptions implements Subscription {
        MY_SUBSCRIPTION_1
    }
   ```
   *This is implemented in
   [MySubscriptions](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/publications/MySubscriptions.java).*

2. Define a
   [Subscriber](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Subscriber.java).  
   Extends the Subscriber interface:
   ```java
   public interface MyVendorSubscriber extends Subscriber {
        @NonNull
        @Override
        default Subscription getSubscription() {
            return MySubscriptions.MY_SUBSCRIPTION_1;
        }
    }
   ```
   Add the methods to call when an event occurs:
   ```java
        // implement methods that can be called by the publisher
        void somethingHappened();
        
        void somethingElseHappened();
   ```

   *This is implemented in
   [MyVendorSubscriber](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/publications/MyVendorSubscriber.java).*

3. Define a
   [Publisher](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/publications/core/Publisher.java).  
   Extends the Publisher interface and provides the type of subscribers it will use:
   ```java
   public class MyVendorPublisher extends Publisher<MyVendorSubscriber> {

        @Override
        public Subscription getSubscription() {  
            return MySubscriptions.MY_SUBSCRIPTION_1;
        }
    }
   ```
   Add publication methods to dispatch events to the subscribers:
   ```java
        public void publishSomething() {
            // dispatch the event to the subscribers     
            forEachSubscriber(MyVendorSubscriber::somethingHappened);
        }
        
        public void publishSomethingElse() {
            // dispatch the event to the subscribers     
            forEachSubscriber(MyVendorSubscriber::somethingElseHappened);
        }
   ```

   *This is implemented in
   [MyVendorPublisher](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/publications/MyVendorPublisher.java).*


4. Use a publisher to publish an event:
   1. Register the publisher:
      ```java
      // create a publisher
      MyVendorPublisher publisher = new MyVendorPublisher();
      
      // register the publisher
      PublicationManager publicationManager = GaiaClientService.getPublisherManager();
      publicationManager.register(publisher);
      ```

    2. Publish the event:
       ```java
       // call the corresponding method from the publisher implementation
       publisher.publishSomething();
       ```

   *This is used in
   [MyV3Plugin](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/vendors/MyV3Plugin.java).*

5. Implements a subscriber to get an event:
   ```java
   // initialise a subscriber and implement the methods
   Subscriber subscriber = new MyVendorSubscriber() {
            @Override
            public void somethingHappened() {
                // event is received
                Log.i("MY_TAG", "something happened!");
            }
            
            @Override
            public void somethingElseHappened() {
                // event is received
                Log.i("MY_TAG", "something else happened!");
            }
        };
		
    // sign up the subscriber
      PublicationManager publicationManager = GaiaClientService.getPublisherManager();
      publicationManager.subscribe(subscriber);
   ```
   *This is shown in
    [UseExample](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/UseExample.java).*

The order of creation for the subscriber and the publisher does not matter. The PublicationManager
adds previous subscribers to ne publishers if they have the same Subscription.
Publishers and Subscribers should be released once they are not of use anymore. However they don\'t
need to be released when the application terminates: the PublicationManager will release them.


### 2.3. Requests
The `app-core` module uses a system of requests for other modules to ask for actions to happen. 
We recommend to use this system for consistency and to avoid blocking the Main/UI thread while 
executing operations.

1. Create a request by extending the 
   [Request](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/requests/core/Request.java) 
   class.
   ```java
   public class MyVendorRequest extends Request<Void, Void, Void> {
   
       private final MyV3Vendor vendor;
   
       public MyVendorRequest(MyV3Vendor vendor,
                                   RequestListener<Void, Void, Void> listener) {
           super(listener);
           this.vendor = vendor;
       }
   
       @Override
       public void run(@NonNull Context context) {
           // run the operation
           vendor.doSomething();

           // provide feedback for long execution by calling onProgress(..)
   
           // Once the request has completed it must call onComplete(Result result).
           onComplete(null);

           // if the request fails, call onError(...) instead
       }
   
       @Override
       protected void onEnd() {
           // called when the request has ended - after onError or onComplete
           // use this to release resources
       }
   }
   ```
   *This is implemented in
   [MyVendorRequest](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/requests/MyRequest.java).*
   
2. Use the request
   ```java
       // create the request
       MyVendorRequest request = new MyVendorRequest(myVendor, null);

       // execute the request
        RequestManager requestManager = GaiaClientService.getRequestManager();
        requestManager.execute(context, request);
   ```
   *This is shown in
    [UseExample](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/UseExample.java).*
    

### 2.4. Foreground and background tasks
The `app-core` module uses a
[TaskManager](../app-core/src/main/java/com/qualcomm/qti/gaiaclient/core/tasks/TaskManager.java)
to run tasks either in the UI/Main thread or in a background thread. We recommend to use it to 
avoid concurrency issues and/or blocking the UI/Main thread. The task manager allows:

1. To run a task in the main thread:
   ```java
   TaskManager taskManager = GaiaClientService.getTaskManager();
   taskManager.runOnMain(() -> { /* do something */});
   ```
   
2. To run a task in a background thread:
   ```java
   TaskManager taskManager = GaiaClientService.getTaskManager();
    taskManager.runInBackground(() -> { /* do something */});
   ```
   
3. To schedule a non-UI task:
   ```java
   TaskManager taskManager = GaiaClientService.getTaskManager();
   int delay = 1000; // 1s
   Runnable myDelayedTask = () -> { /* do something */};
   taskManager.schedule(myDelayedTask, delay);
   ```
   It is also possible to cancel a scheduled task:
   ```java
   taskManager.cancelScheduled(myDelayedTask);
   ```
   *Note: to schedule a UI task, use Handler#postDelayed() as usual. Make sure the Handler is
   created on the UI thread.*

*This is shown in
 [UseExample](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/UseExample.java).*


### 2.5. Connecting to a device with a RFCOMM channel
To initiate a RFCOMM connection use the `TransportManager`:
```java
TransportManager transportManager = getTransportManager();
BluetoothStatus status = transportManager.connect(context, bluetoothAddress, transport);
```
The result will be reported asynchronously through a `ConnectionSubscriber` to register with the `PublicationManager`:
```java
// initialise a subscriber and implement the methods
Subscriber subscriber = new ConnectionSubscriber() {
        @Override
        public void onConnectionStateChanged(Link link, ConnectionState state) {
            // check the connection state to determine if the device was connected
        }

        @Override
        public void onConnectionError(Link link, BluetoothStatus reason) {
            // check the reason to understand why a connection has failed
        }
    };

// sign up the subscriber
  PublicationManager publicationManager = GaiaClientService.getPublisherManager();
  publicationManager.subscribe(subscriber);
// Note: do not forget to unregister the subscriber if it is not needed anymore.
```

*This is shown in [MySimpleConnectionRequest](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/requests/MySimpleConnectionRequest.java).<br/>
To use `MySimpleConnectionRequest` refer to the method `executeMySimpleConnectionRequest` in [UseExample](src/main/java/com/qualcomm/qti/gaiaclient/vendor/example/UseExample.java).*


## Adding the `vendor-example` module as a dependency
To add this module as a dependency modify the `build.gradle` file of the module that is going to use
it:
   ```renderscript
   dependencies {
        [...]
        // modules
        implementation project(':vendor-example')
        [...]
    }
   ```

*This could be done for instance in the `build.gradle` file of the `app` module.*
