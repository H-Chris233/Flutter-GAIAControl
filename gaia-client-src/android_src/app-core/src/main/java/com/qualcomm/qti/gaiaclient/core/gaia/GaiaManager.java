/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.GaiaReader;

/**
 * <p>Represents a managing class for GAIA. This allows to register a GAIA {@link Vendor} with the
 * core library.</p>
 * <p>The {@link GaiaManager} can be retrieved using {@link GaiaClientService#getGaiaManager()}.</p>
 */
public interface GaiaManager {

    /**
     * <p>This method allows vendors to register in order to get the packets that corresponds to
     * their vendor ID.</p>
     */
    void registerVendor(Vendor vendor);

    /**
     * <p>To get the sender GAIA {@link Vendor} and
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin} need to send packets to a
     * connected device.</p>
     */
    GaiaSender getSender();

    /**
     * To get the GAIA stream analyser for RFCOMM connections. This is used by the transport
     * layer when initialising a connection.
     */
    GaiaReader.Listener getReaderListener();

}
