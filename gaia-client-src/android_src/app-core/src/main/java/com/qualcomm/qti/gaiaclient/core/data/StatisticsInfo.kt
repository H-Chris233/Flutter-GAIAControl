/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data

enum class StatisticsInfo {
    /**
     * <ul>
     *     <li>Fetch: Last Category ID received as 16 bit UInt. 0 on first request.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    CATEGORIES,
    /**
     * <ul>
     *     <li>Fetch:   Category ID as 16 bit UInt.
     *                  StatisticID as 8 bit UInt. 0 on first request.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    ALL_STATISTICS_IN_CATEGORY,
    /**
     * <ul>
     *     <li>Fetch:   List of statistic descriptors consisting of:
     *                      Category ID as 16 bit UInt.
     *                      StatisticID as 8 bit UInt.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    STATISTIC_VALUES,
}
