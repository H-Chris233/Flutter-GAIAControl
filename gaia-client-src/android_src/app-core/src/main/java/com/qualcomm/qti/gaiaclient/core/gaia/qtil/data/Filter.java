/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum Filter {

    /**
     * "Bypass" band filter
     */
    BYPASS(0),
    /**
     * "First Order Low Pass" band filter
     */
    LOW_PASS_1(1),
    /**
     * "First Order High Pass" band filter
     */
    HIGH_PASS_1(2),
    /**
     * "First Order All Pass" band filter
     */
    ALL_PASS_1(3),
    /**
     * "First Order Low Shelf" band filter
     */
    LOW_SHELF_1(4),
    /**
     * "First Order High Shelf" band filter
     */
    HIGH_SHELF_1(5),
    /**
     * "First Order Tilt" band filter
     */
    TILT_1(6),
    /**
     * "Second Order Low Pass" band filter
     */
    LOW_PASS_2(7),
    /**
     * "Second Order High Pass" band filter
     */
    HIGH_PASS_2(8),
    /**
     * "Second Order All Pass" band filter
     */
    ALL_PASS_2(9),
    /**
     * "Second Order Low Shelf" band filter
     */
    LOW_SHELF_2(10),
    /**
     * "Second Order High Shelf" band filter
     */
    HIGH_SHELF_2(11),
    /**
     * "Second Order Tilt" band filter
     */
    TILT_2(12),
    /**
     * "Parametric Equalizer" band filter
     */
    PARAMETRIC_EQUALIZER(13);

    private static final Filter[] VALUES = values();

    private final int value;

    Filter(int value) {
        this.value = value;
    }

    public static Filter valueOf(int value) {
        for (Filter filter : VALUES) {
            if (filter.value == value) {
                return filter;
            }
        }

        return null;
    }
}
