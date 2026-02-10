/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.version;

import com.qualcomm.qti.gaiaclient.core.data.Reason;

public interface V2ApiVersionFetcherListener {

    void onVersion(V2ApiVersion version);

    void onError(Reason reason);

}
