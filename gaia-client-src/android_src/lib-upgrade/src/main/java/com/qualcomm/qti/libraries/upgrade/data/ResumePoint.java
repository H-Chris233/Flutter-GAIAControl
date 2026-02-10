/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

/**
 * <p>This class contains the resume points as defined in the Upgrade protocol to resume to a
 * certain point in the process.</p>
 * <p>A resume point represents the step where the upgrade restarts from once the INIT PHASE of
 * the process is done.</p>
 */
public enum ResumePoint {
    /**
     * This is the resume point "0", that means the upgrade will start from the beginning, the
     * UPGRADE_START_DATA_REQ request.
     * <p><i>Note: this was called "DATA_TRANSFER" before.</i></p>
     */
    START((byte) 0x00),
    /**
     * This is the 1st resume point, that means the upgrade should resume from the
     * UPGRADE_IS_IMAGE_VALID_DONE_REQ request.
     * <p><i>Note: this was called "VALIDATION" before.</i></p>
     */
    PRE_VALIDATE((byte) 0x01),
    /**
     * This is the 2nd resume point, that means the upgrade should resume from the
     * UPGRADE_TRANSFER_COMPLETE_RES request.
     * <p><i>Note: this was called "TRANSFER_COMPLETE" before.</i></p>
     */
    PRE_REBOOT((byte) 0x02),
    /**
     * This is the 3rd resume point, that means the upgrade should resume from the
     * UPGRADE_PROCEED_TO_COMMIT request.
     * <p><i>Note: this was called "IN_PROGRESS" before.</i></p>
     */
    POST_REBOOT((byte) 0x03),
    /**
     * This is the 4th resume point, that means the upgrade should resume from the
     * UPGRADE_COMMIT_CFM confirmation request.
     */
    COMMIT((byte) 0x04),
    /**
     * This is when the new image has been committed but the device is yet to send the
     * UPGRADE_COMPLETE_IND message.
     */
    POST_COMMIT((byte) 0x05);

    /**
     * The value that represents the resume point.
     */
    private final byte value;

    /**
     * To keep a static array of all the values instead of calling {@link #values()} when needing
     * a list of the values.
     */
    private static final ResumePoint[] VALUES = values();

    /**
     * Constructor of the enumeration.
     *
     * @param value
     *         the value that represents the point.
     */
    ResumePoint(byte value) {
        this.value = value;
    }

    /**
     * To get the ResumePoint corresponding to the given value.
     *
     * @param value
     *         The value to get the corresponding Resume Point from.
     *
     * @return The corresponding ResumePoint, if no corresponding point is found, this method
     * returns {@link ResumePoint#START} as the default value.
     */
    public static ResumePoint valueOf(byte value) {
        for (ResumePoint point : VALUES) {
            if (point.value == value) {
                return point;
            }
        }

        return START;
    }

}
