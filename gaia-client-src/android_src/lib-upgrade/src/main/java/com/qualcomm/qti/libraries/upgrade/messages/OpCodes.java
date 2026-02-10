/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.messages;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;

import com.qualcomm.qti.libraries.upgrade.data.ResumePoint;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class contains the different operation codes used in the {@link UpgradeMessage
 * UpgradeMessage} exchanged between the Host and the Device during the upgrade process.</p>
 * <p>It also contains complementary information about values or messages structure for certain
 * operation codes as defined in the protocol.</p>
 */
@SuppressWarnings("unused")
public final class OpCodes {

    /* --------------------------------------- OPCODES --------------------------------------- */

    /**
     * <p>The enumeration of all different operation codes.</p> <p/> <p>Each Operation code is
     * defined by the following:</p> <dl> <dt><b>Content</b></dt><dd>The information which should be
     * in the "content" part of the message.</dd> <dt><b>Previous and next messages</b></dt><pp>The
     * operation code of the messages which should happen before and after this one.</pp> </dl>
     */
    @IntDef(flag = true, value = {Enum.UPGRADE_START_REQ, Enum.UPGRADE_START_CFM,
                                  Enum.UPGRADE_DATA_BYTES_REQ, Enum.UPGRADE_DATA, Enum.UPGRADE_ABORT_REQ,
                                  Enum.UPGRADE_ABORT_CFM, Enum.UPGRADE_TRANSFER_COMPLETE_IND,
                                  Enum.UPGRADE_TRANSFER_COMPLETE_RES, Enum.UPGRADE_PROCEED_TO_COMMIT,
                                  Enum.UPGRADE_COMMIT_REQ, Enum.UPGRADE_COMMIT_CFM, Enum.UPGRADE_ERROR_IND,
                                  Enum.UPGRADE_COMPLETE_IND, Enum.UPGRADE_SYNC_REQ, Enum.UPGRADE_START_DATA_REQ,
                                  Enum.UPGRADE_IS_VALIDATION_DONE_REQ, Enum.UPGRADE_IS_VALIDATION_DONE_CFM,
                                  Enum.UPGRADE_ERROR_RES, Enum.UPGRADE_VERSION_REQ, Enum.UPGRADE_SYNC_CFM,
                                  Enum.UPGRADE_VARIANT_CFM, Enum.UPGRADE_VERSION_CFM, Enum.UPGRADE_VARIANT_REQ,
                                  Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_CFM, Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_REQ,
                                  Enum.UPGRADE_SILENT_COMMIT_CFM, Enum.UPGRADE_PUT_EARBUDS_IN_CASE_REQ,
                                  Enum.UPGRADE_EARBUDS_IN_CASE_CFM, Enum.UPGRADE_COMPLETE_IND_WITH_STATUS})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Enum {

        /**
         * <p>To request an upgrade procedure to start.</p> <dl> <dt><b>Content</b></dt>
         * <dd>none</dd> <dt><b>Previous message</b></dt> <dd>{@link OpCodes.Enum#UPGRADE_SYNC_CFM
         * UPGRADE_SYNC_CFM} from device.</dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from the device.</dd> </dl>
         */
        byte UPGRADE_START_REQ = 0x01;

        /**
         * <p>To confirm the start of the upgrade procedure.</p> <dl>
         * <dt><b>Content</b></dt><dd>Contains
         * a value to indicate if the Device is ready for the upgrade, see {@link
         * UpgradeStartCFM}.</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_REQ UPGRADE_START_REQ}</dd> <dt><b>Next
         * message</b></dt><dd>depends on the {@link ResumePoint} value received by the Host in
         * the
         * {@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} message: <table> <tr> <td>{@link
         * ResumePoint#START START}</td> <td> &#8658; {@link OpCodes.Enum#UPGRADE_START_REQ}
         * from application.</td> </tr> <tr> <td>{@link ResumePoint#PRE_VALIDATE
         * PRE_VALIDATE}</td> <td> &#8658; {@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ} from
         * application.</td> </tr> <tr> <td>{@link ResumePoint#PRE_REBOOT PRE_REBOOT}</td>
         * <td> &#8658; {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES} from application.</td>
         * </tr> <tr> <td>{@link ResumePoint#POST_REBOOT POST_REBOOT}</td> <td> &#8658;
         * {@link
         * OpCodes.Enum#UPGRADE_PROCEED_TO_COMMIT} from application.</td> </tr> <tr> <td>{@link
         * ResumePoint#COMMIT COMMIT}</td> <td> &#8658;
         * {@link OpCodes.Enum#UPGRADE_COMMIT_CFM}
         * from application.</td> </tr> <tr> <td>{@link ResumePoint#POST_COMMIT
         * POST_COMMIT}</td> <td> &#8658; {@link OpCodes.Enum#UPGRADE_COMPLETE_IND} from the
         * device.</td> </tr> </table> </dd> </dl>
         */
        byte UPGRADE_START_CFM = 0x02;

        /**
         * <p>To request the section of the data expected.</p> <p/> <dl>
         * <dt><b>Content</b></dt><dd>The length and the offset of the required
         * section from the data.</dd> <dt><b>Previous message</b></dt><dd> <ul
         * style="list-style-type:none"> <li>{@link OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA} from
         * application</li> <li>{@link OpCodes.Enum#UPGRADE_START_DATA_REQ UPGRADE_START_DATA_REQ}
         * from application</li> </ul> </dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA} from the application.</dd> </dl>
         */
        byte UPGRADE_DATA_BYTES_REQ = 0x03;

        /**
         * <p>To transfer sections of the data.</p> <dl> <dt><b>Content</b></dt><dd>The section
         * from the data which has been requested by the Device.</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_DATA_BYTES_REQ
         * UPGRADE_DATA_BYTES_REQ} from device.</dd>
         * <dt><b>Next message</b></dt><dd> <ul style="list-style-type:none"> <li>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from
         * application.</li> <li>{@link
         * OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} from device.</li> </ul>
         * </dd>
         * </dl>
         */
        byte UPGRADE_DATA = 0x04;

        /**
         * <p>To abort the upgrade procedure.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>any message
         * or none.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM}
         * from device.</dd>
         * </dl>
         */
        byte UPGRADE_ABORT_REQ = 0x07;

        /**
         * <p>To confirm the abortion of the upgrade</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ} from application.</dd> <dt><b>Next
         * message</b></dt><dd>None: disconnection of the upgrade?</dd> </dl>
         */
        byte UPGRADE_ABORT_CFM = 0x08;

        /**
         * <p>To indicate that the data has successfully been received and validated.</p>
         * <p/>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from
         * application.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES
         * UPGRADE_TRANSFER_COMPLETE_RES} from application.</dd> </dl>
         */
        byte UPGRADE_TRANSFER_COMPLETE_IND = 0x0B;

        /**
         * <p>To respond to the {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND} message
         * .</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains {@link UpgradeTransferCompleteRES.Action#ABORT
         * ABORT} or {@link
         * UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE} information.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND} from
         * device.</dd> <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SYNC_REQ
         * UPGRADE_SYNC_REQ} from
         * application after the reboot of the device.</dd> </dl>
         */
        byte UPGRADE_TRANSFER_COMPLETE_RES = 0x0C;

        /**
         * <p>To inform the Device that the Host would like to continue the upgrade process.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains {@link UpgradeProceedToCommitRES.Action#CONTINUE
         * CONTINUE}
         * information.</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_CFM
         * UPGRADE_START_CFM} which should contain the Resume point 3: {@link
         * ResumePoint#POST_REBOOT
         * POST_REBOOT}.</dd> <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_COMMIT_REQ
         * UPGRADE_COMMIT_REQ}
         * from the device.</dd> </dl>
         * <p><i>Note: this was called "UPGRADE_IN_PROGRESS_RES" before.</i></p>
         */
        byte UPGRADE_PROCEED_TO_COMMIT = 0x0E;

        /**
         * <p>Used by a device to indicate it is ready for permission to commit the upgrade.</p>
         * <dl>
         * <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_PROCEED_TO_COMMIT UPGRADE_PROCEED_TO_COMMIT} from the Host.</dd>
         * <dt><b>Next
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_COMMIT_CFM UPGRADE_COMMIT_CFM} from the
         * Host.</dd> </dl>
         */
        byte UPGRADE_COMMIT_REQ = 0x0F;

        /**
         * <p>The respond to the {@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ}
         * message.</p>
         * <dl> <dt><b>Content</b></dt><dd>0x00 to indicate to continue the upgrade, 0x01 to abort.
         * See {@link
         * UpgradeCommitCFM UpgradeCommitCFM}.</dd> <dt><b>Previous message</b></dt><dd>Two
         * possibilities:<ul><li>{@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from the Device which should contain
         * the Resume point 4:
         * {@link ResumePoint#COMMIT COMMIT}.</li> <li>{@link OpCodes.Enum#UPGRADE_COMMIT_REQ
         * UPGRADE_COMMIT_REQ}
         * from the Device.</li></ul></dd> <dt><b>Next message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND} from Device.</dd> </dl>
         */
        byte UPGRADE_COMMIT_CFM = 0x10;

        /**
         * <p>Used by the Device to inform the application about errors or warnings. Errors are
         * considered as fatal.
         * Warnings are considered as informational.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>Contains a {@link ErrorCodes ErrorCodes}.</dd>
         * <dt><b>Previous
         * message</b></dt><dd>none</dd> <dt><b>Next message</b></dt><dd>depends on the Return Code
         * and any user
         * action.</dd> </dl>
         * <p><i>Note: this was called "UPGRADE_ERROR_WARN_IND" before.</i></p>
         */
        byte UPGRADE_ERROR_IND = 0x11;

        /**
         * <p>Used by the device to indicate the upgrade has been completed.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_COMMIT_CFM UPGRADE_COMMIT_CFM} from Host.</dd> <dd>or {@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from Device if the resume point
         * contained in
         * {@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} was {@link
         * ResumePoint#POST_COMMIT POST_COMMIT}
         * .</dd> <dt><b>Next message</b></dt><dd>None, that one is the last one of a successful
         * upgrade.</dd> </dl>
         */
        byte UPGRADE_COMPLETE_IND = 0x12;

        /**
         * <p>Used by the application to synchronize with the device before any other protocol
         * message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>ID of the upgrade which corresponds to the MD5 check sum
         * of the data.</dd> <dt><b>Previous message</b></dt><dd>None, that one is the initiator of
         * the process.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM}
         * from Device.</dd>
         * </dl>
         */
        byte UPGRADE_SYNC_REQ = 0x13;

        /**
         * <p>Used by the device to respond to the {@link OpCodes.Enum#UPGRADE_SYNC_REQ
         * UPGRADE_SYNC_REQ} message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>A {@link ResumePoint} value.</dd> <dt><b>Previous
         * message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_START_REQ} from Device.</dd> <dt><b>Next
         * message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_REQ UPGRADE_START_REQ} from Device.</dd> </dl>
         */
        byte UPGRADE_SYNC_CFM = 0x14;

        /**
         * <p>Used by the Host to start the data transfer.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from Device.</dd> <dt><b>Next
         * message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} from Device.</dd> </dl>
         */
        byte UPGRADE_START_DATA_REQ = 0x15;

        /**
         * <p>Used by the Host to request for executable partition validation status.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>Three
         * possibilities from
         * Device: <ul><li>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_CFM
         * UPGRADE_IS_VALIDATION_DONE_CFM}</li>
         * <li>{@link OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA}</li> <li>{@link
         * OpCodes.Enum#UPGRADE_START_CFM
         * UPGRADE_START_CFM}</li> </ul></dd> <dt><b>Next message</b></dt><dd>Two possibilities from
         * Device:
         * <ul><li>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_CFM
         * UPGRADE_IS_VALIDATION_DONE_CFM}</li>
         * <li>{@link
         * OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND}</li> </ul></dd>
         * </dl>
         */
        byte UPGRADE_IS_VALIDATION_DONE_REQ = 0x16;

        /**
         * <p>Used by the Device to respond to the
         * {@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ
         * UPGRADE_IS_VALIDATION_DONE_REQ} message.</p>
         * <p/>
         * <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>{@link
         * OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ UPGRADE_IS_VALIDATION_DONE_REQ} from
         * Device.</dd> <dt><b>Next
         * message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_IS_VALIDATION_DONE_REQ
         * UPGRADE_IS_VALIDATION_DONE_REQ} from
         * Device.</dd> </dl>
         */
        byte UPGRADE_IS_VALIDATION_DONE_CFM = 0x17;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VERSION_REQ = 0x19;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VERSION_CFM = 0x1A;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VARIANT_REQ = 0x1B;

        /**
         * <i>no documentation</i>
         */
        byte UPGRADE_VARIANT_CFM = 0x1C;

        /**
         * <p>Used by the Host to confirm it received an error or a warning
         * message from the device.</p>
         * <dl> <dt><b>Content</b></dt><dd>The {@link ErrorCodes ErrorCodes} received.</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_ERROR_IND
         * UPGRADE_ERROR_IND} from Device.</dd>
         * <dt><b>Next message</b></dt><dd>Depends on the received {@link ErrorCodes ErrorCodes}
         * value.</dd> </dl>
         * <p><i>Note: this was called "UPGRADE_ERROR_WARN_RES" before.</i></p>
         */
        byte UPGRADE_ERROR_RES = 0x1F;

        /**
         * <p>Used by the Host to check if silent commit is supported by the device.</p>
         * <dl> <dt><b>Content</b></dt><dd>none</dd>
         * <dt><b>Previous message</b></dt><dd>can be sent anytime after {@link OpCodes.Enum#UPGRADE_SYNC_CFM
         * UPGRADE_SYNC_CFM} and before {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES UPGRADE_TRANSFER_COMPLETE_RES}.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SILENT_COMMIT_SUPPORTED_CFM
         * UPGRADE_SILENT_COMMIT_SUPPORTED_CFM} sent by the Device.</dd> </dl>
         *
         * @since upgrade protocol v4
         */
        byte UPGRADE_SILENT_COMMIT_SUPPORTED_REQ = 0x20;

        /**
         * <p>Used by the Device to inform the Host if silent commit can be used.</p>
         * <dl> <dt><b>Content</b></dt><dd>A value: <code>0x00</code> if not supported, <code>0x01</code> if
         * supported.</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_SILENT_COMMIT_SUPPORTED_REQ
         * UPGRADE_SILENT_COMMIT_SUPPORTED_REQ}.</dd>
         * <dt><b>Next message</b></dt><dd>Any.</dd> </dl>
         *
         * @since upgrade protocol v4
         */
        byte UPGRADE_SILENT_COMMIT_SUPPORTED_CFM = 0x21;

        /**
         * <p>Used by the Device to inform the Host that silent commit is going to happen and that the interaction with
         * the Host is now complete and that the installation will happen at a later time.</p>
         * <dl> <dt><b>Content</b></dt><dd>none</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_RES
         * UPGRADE_TRANSFER_COMPLETE_RES}.</dd>
         * <dt><b>Next message</b></dt><dd>Upgrade disconnection is expected.</dd> </dl>
         *
         * @since upgrade protocol v4
         */
        byte UPGRADE_SILENT_COMMIT_CFM = 0x22;

        /**
         * <p>Used by the Device to inform the Host that the user must put the pair of earbuds in the case and close the
         * lid (if any) to continue the upgrade.</p>
         * <dl> <dt><b>Content</b></dt><dd>none</dd>
         * <dt><b>Previous message</b></dt><dd>most likely to be: {@link OpCodes.Enum#UPGRADE_DATA UPGRADE_DATA}.</dd>
         * <dt><b>Next message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_EARBUDS_IN_CASE_CFM
         * UPGRADE_EARBUDS_IN_CASE_CFM}.</dd> </dl>
         *
         * @since upgrade protocol v5
         */
        byte UPGRADE_PUT_EARBUDS_IN_CASE_REQ = 0x23;

        /**
         * <p>Used by the Device to inform the Host that the user has successfully put the pair of earbuds in their case
         * and close the lid (if any) to continue the upgrade. This announces that the upgrade will continue.</p>
         * <dl> <dt><b>Content</b></dt><dd>none</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_PUT_EARBUDS_IN_CASE_REQ
         * UPGRADE_PUT_EARBUDS_IN_CASE_REQ}.</dd>
         * <dt><b>Next message</b></dt><dd>most likely to be: {@link OpCodes.Enum#UPGRADE_DATA_BYTES_REQ
         * UPGRADE_DATA_BYTES_REQ}.</dd> </dl>
         *
         * @since upgrade protocol v5
         */
        byte UPGRADE_EARBUDS_IN_CASE_CFM = 0x24;

        /**
         * <p>Used by the device to indicate the upgrade has been completed with an informal status.</p>
         * <dl> <dt><b>Content</b></dt><dd>A Status as one of {@link CommitStatus}.</dd>
         * <dt><b>Previous message</b></dt><dd>{@link OpCodes.Enum#UPGRADE_COMMIT_CFM UPGRADE_COMMIT_CFM} from
         * Host.</dd> <dd>or {@link OpCodes.Enum#UPGRADE_START_CFM UPGRADE_START_CFM} from Device if the resume point
         * contained in {@link OpCodes.Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} was
         * {@link ResumePoint#POST_COMMIT POST_COMMIT} .</dd> <dt><b>Next message</b></dt><dd>None, that one is the
         * last one of a successful upgrade.</dd> </dl>
         */
        byte UPGRADE_COMPLETE_IND_WITH_STATUS = 0x25;
    }


    /* ******* STRUCTURE AND CONTENT FOR MESSAGES INFORMATION ******* */

    /**
     * <p>Complementary information for the structure and content of the {@link
     * Enum#UPGRADE_START_CFM
     * UPGRADE_START_REQ} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeStartREQ {

        /**
         * length of the content for the {@link Enum#UPGRADE_START_REQ UPGRADE_START_REQ} message.
         */
        public static final byte CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_START_CFM UPGRADE_START_CFM}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3
     *      +--------+--------+--------+
     *      | STATUS |  BATTERY LEVEL  |
     *      +--------+--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeStartCFM {

        /**
         * <p>All the status which can be returned for the STATUS information.</p>
         */
        public static class Status {

            /**
             * Value for an {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message when the device
             * is ready to start
             * the upgrade process.
             */
            public static final byte SUCCESS = 0x00;
            /**
             * Value for an {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message when the device
             * is not ready to
             * start the upgrade process.
             */
            public static final byte ERROR_APP_NOT_READY = 0x09;
        }

        /**
         * The offset to get the status information.
         */
        public static final int STATUS_OFFSET = 0;
        /**
         * To know how many bytes represent the status information.
         */
        static final int STATUS_LENGTH = 1;
        /**
         * The offset to get the battery level information.
         */
        public static final int BATTERY_LEVEL_OFFSET = STATUS_OFFSET + STATUS_LENGTH;
        /**
         * Nb of bytes which represent the battery level information.
         */
        public static final int BATTERY_LEVEL_LENGTH = 2;
        /**
         * length of the content for the {@link Enum#UPGRADE_START_CFM UPGRADE_START_CFM} message.
         */
        public static final byte CONTENT_LENGTH = STATUS_LENGTH + BATTERY_LEVEL_LENGTH;
    }


    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3        4        5        6        7        8
     *      +--------+--------+--------+--------+--------+--------+--------+--------+
     *      |     NUMBER OF BYTES REQUESTED     |         DATA OFFSET JUMP          |
     *      +--------+--------+--------+--------+--------+--------+--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeDataBytesREQ {

        /**
         * <p>The number of bytes which contains the number of bytes of the data to
         * send.</p>
         */
        public static final int NB_BYTES_LENGTH = 4;
        /**
         * <p>The offset in the content of {@link Enum#UPGRADE_DATA_BYTES_REQ
         * UPGRADE_DATA_BYTES_REQ} where the "number of bytes to send" information starts.</p>
         */
        public static final int NB_BYTES_OFFSET = 0;
        /**
         * <p>The number of bytes which contains the byte offset jump within the data from
         * which the host should jump its offset by.</p>
         */
        public static final int OFFSET_MOVE_LENGTH = 4;
        /**
         * <p>The offset in the {@link Enum#UPGRADE_DATA_BYTES_REQ UPGRADE_DATA_BYTES_REQ} bytes
         * content where the data offset information starts. .</p>
         */
        public static final int OFFSET_MOVE_OFFSET = NB_BYTES_OFFSET + NB_BYTES_LENGTH;
        /**
         * The length for the content of the {@link Enum#UPGRADE_DATA_BYTES_REQ
         * UPGRADE_DATA_BYTES_REQ} message.
         */
        public static final int CONTENT_LENGTH = OFFSET_MOVE_LENGTH + NB_BYTES_LENGTH;

    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_DATA
     * UPGRADE_DATA} message.</p>
     * <blockquote><pre>
     *      0 bytes                1       ...       n
     *      +----------------------+--------+--------+
     *      | IS LAST DATA MESSAGE |    DATA...      |
     *      +----------------------+--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeData {

        /**
         * The offset which contains the last message information in the UPGRADE_DATA content
         * message.
         */
        public static final int IS_LAST_DATA_MESSAGE_OFFSET = 0;

        /**
         * The number of bytes which contains the last data message information.
         */
        static final int IS_LAST_DATA_MESSAGE_LENGTH = 1;

        /**
         * All the values which can be sent to know if the {@link Enum#UPGRADE_DATA UPGRADE_DATA}
         * message is sending the last data message of the upgrade.
         */
        public static class LastDataMessage {

            /**
             * Value for the first byte of the content when sending the last {@link
             * Enum#UPGRADE_DATA UPGRADE_DATA} message.
             */
            public static final byte IS_LAST_DATA_MESSAGE = 0x01;
            /**
             * Value for the first byte of the content when not sending the last {@link
             * Enum#UPGRADE_DATA UPGRADE_DATA} message.
             */
            public static final byte IS_NOT_LAST_DATA_MESSAGE = 0x00;
        }

        /**
         * <p>The offset that contains the data bytes to send.</p>
         */
        public static final int DATA_OFFSET =
                IS_LAST_DATA_MESSAGE_OFFSET + IS_LAST_DATA_MESSAGE_LENGTH;
        /**
         * The minimum length for the content of the {@link Enum#UPGRADE_DATA UPGRADE_DATA} message.
         */
        public static final int DATA_HEADER_LENGTH = IS_LAST_DATA_MESSAGE_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ}
     * message..</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeAbortREQ {

        /**
         * The length for the content of the {@link Enum#UPGRADE_ABORT_REQ UPGRADE_ABORT_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM}
     * message..</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeAbortCFM {

        /**
         * The length for the content of the {@link Enum#UPGRADE_ABORT_CFM UPGRADE_ABORT_CFM}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_TRANSFER_COMPLETE_IND
     * UPGRADE_TRANSFER_COMPLETE_IND} message..</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeTransferCompleteIND {

        /**
         * The length for the content of the {@link Enum#UPGRADE_TRANSFER_COMPLETE_IND
         * UPGRADE_TRANSFER_COMPLETE_IND}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_TRANSFER_COMPLETE_RES
     * UPGRADE_TRANSFER_COMPLETE_RES} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public static class UpgradeTransferCompleteRES {

        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public static class Action {

            /**
             * Used by the application to confirm that the upgrade should continue using an interactive commit.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm that the upgrade should abort.
             */
            public static final byte ABORT = 0x01;
            /**
             * Used by the application to confirm that the upgrade should continue and the commit should be done in
             * silent: no interaction with the host.
             *
             * @since upgrade protocol v4
             */
            public static final byte SILENT_COMMIT = 0x02;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        static final int ACTION_LENGTH = 1;
        /**
         * The length of the content for this message.
         */
        public static final int CONTENT_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_PROCEED_TO_COMMIT
     * UPGRADE_PROCEED_TO_COMMIT} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public static class UpgradeProceedToCommitRES {

        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public static class Action {

            /**
             * Used by the application to indicate to continue that the upgrade progress.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm that the upgrade should abort.
             */
            public static final byte ABORT = 0x01;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        static final int ACTION_LENGTH = 1;
        /**
         * The length of the content for this message.
         */
        public static final int CONTENT_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_COMMIT_REQ
     * UPGRADE_COMMIT_REQ} message..</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeCommitREQ {

        /**
         * The length for the content of the {@link Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_COMMIT_CFM
     * UPGRADE_COMMIT_CFM} message.</p>
     * <blockquote><pre>
     *      0 bytes    1
     *      +----------+
     *      |  ACTION  |
     *      +----------+
     * </pre></blockquote>
     */
    public static class UpgradeCommitCFM {

        /**
         * <p>All the actions which can be returned for the ACTION information.</p>
         */
        public static class Action {

            /**
             * Used by the application to confirm the user wants to commit the
             * upgrade.
             */
            public static final byte CONTINUE = 0x00;
            /**
             * Used by the application to confirm the user doesn't want to commit the
             * upgrade for
             * now.
             */
            public static final byte ABORT = 0x01;
        }

        /**
         * The offset which contains the action information.
         */
        public static final int ACTION_OFFSET = 0;
        /**
         * The number of bytes which contains the action information.
         */
        static final int ACTION_LENGTH = 1;
        /**
         * The length of the content for this message.
         */
        public static final int CONTENT_LENGTH = ACTION_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_ERROR_IND
     * UPGRADE_ERROR_IND} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |   RETURN CODE   |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeErrorWarnIND {

        /**
         * <p>The offset where the return code information starts.</p>
         */
        public static final int RETURN_CODE_OFFSET = 0;
        /**
         * <p>The number of bytes which contains the return code information.</p>
         */
        public static final int RETURN_CODE_LENGTH = 2;
        /**
         * Used by the application to build the confirmation sent with {@link
         * Enum#UPGRADE_ERROR_IND
         * UPGRADE_ERROR_IND} message that it has received an error or warning message: see {@link
         * ErrorCodes}.
         */
        public static final int CONTENT_LENGTH = RETURN_CODE_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_COMPLETE_IND
     * UPGRADE_COMPLETE_IND} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeCompleteIND {

        /**
         * The length for the content of the {@link Enum#UPGRADE_COMPLETE_IND UPGRADE_COMPLETE_IND}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SYNC_REQ
     * UPGRADE_SYNC_REQ}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2        3        4
     *      +--------+--------+--------+--------+
     *      |      IN PROGRESS IDENTIFIER       |
     *      +--------+--------+--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeSyncREQ {

        /**
         * The offset where the identifier information starts.
         */
        public static final int IDENTIFIER_OFFSET = 0;
        /**
         * The number of bytes which contain the identifier information.
         */
        public static final int IDENTIFIER_LENGTH = 4;
        /**
         * The length for the content of the {@link Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} message.
         */
        public static final int CONTENT_LENGTH = IDENTIFIER_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SYNC_CFM
     * UPGRADE_SYNC_CFM}
     * message.</p>
     * <blockquote><pre>
     *      0 bytes            1        2        3        4        5                  6
     *      +------------------+--------+--------+--------+--------+------------------+
     *      |   RESUME POINT   |       IN PROGRESS IDENTIFIER      | PROTOCOL VERSION |
     *      +------------------+--------+--------+--------+--------+------------------+
     * </pre></blockquote>
     */
    public static class UpgradeSyncCFM {

        /**
         * <p>The offset from where the resume point information starts.</p>
         */
        public static final int RESUME_POINT_OFFSET = 0;
        /**
         * <p>The number of bytes which contain the resume point information.</p>
         */
        static final int RESUME_POINT_LENGTH = 1;
        /**
         * <p>The offset from where the identifier information starts.</p>
         */
        public static final int IDENTIFIER_OFFSET = RESUME_POINT_OFFSET + RESUME_POINT_LENGTH;
        /**
         * <p>The number of bytes which contain the identifier information.</p>
         */
        public static final int IDENTIFIER_LENGTH = 4;
        /**
         * <p>The offset from where the protocol version starts.</p>
         */
        public static final int PROTOCOL_VERSION_OFFSET = IDENTIFIER_OFFSET + IDENTIFIER_LENGTH;
        /**
         * <p>The number of bytes which contain the protocol version information.</p>
         */
        static final int PROTOCOL_VERSION_LENGTH = 1;
        /**
         * The length for the content of the {@link Enum#UPGRADE_SYNC_CFM UPGRADE_SYNC_CFM} message.
         */
        public static final int CONTENT_LENGTH =
                RESUME_POINT_LENGTH + IDENTIFIER_LENGTH + PROTOCOL_VERSION_LENGTH;

    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_START_DATA_REQ
     * UPGRADE_START_DATA_REQ} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeStartDataREQ {

        /**
         * The length for the content of the {@link Enum#UPGRADE_START_DATA_REQ
         * UPGRADE_START_DATA_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_IS_VALIDATION_DONE_REQ
     * UPGRADE_IS_VALIDATION_DONE_REQ} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     */
    public static class UpgradeIsValidationDoneREQ {

        /**
         * The length for the content of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_REQ
         * UPGRADE_IS_VALIDATION_DONE_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_IS_VALIDATION_DONE_CFM
     * UPGRADE_IS_VALIDATION_DONE_CFM} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |   WAITING TIME  |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeIsValidationDoneCFM {

        /**
         * The offset from where the waiting time information starts.
         */
        public static final int WAITING_TIME_OFFSET = 0;
        /**
         * The number of bytes which represent the waiting time information.
         */
        public static final int WAITING_TIME_LENGTH = 2;
        /**
         * The length for the content of the {@link Enum#UPGRADE_IS_VALIDATION_DONE_CFM
         * UPGRADE_IS_VALIDATION_DONE_CFM}
         * message.
         */
        public static final int CONTENT_LENGTH = WAITING_TIME_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link
     * Enum#UPGRADE_ERROR_RES
     * UPGRADE_ERROR_RES} message.</p>
     * <blockquote><pre>
     *      0 bytes  1        2
     *      +--------+--------+
     *      |    ERROR CODE   |
     *      +--------+--------+
     * </pre></blockquote>
     */
    public static class UpgradeErrorWarnRES {

        /**
         * The offset from where the error code information as a {@link
         * ErrorCodes.Enum
         * ErrorCodes} starts.
         */
        public static final int ERROR_OFFSET = 0;
        /**
         * The number of bytes which represent the Error information.
         */
        static final int ERROR_LENGTH = 2;
        /**
         * Used by the application to build the confirmation sent with {@link Enum#UPGRADE_ERROR_RES
         * UPGRADE_ERROR_RES} message that it has received an error or warning message.
         */
        public static final int CONTENT_LENGTH = ERROR_LENGTH;
    }


    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SILENT_COMMIT_SUPPORTED_REQ
     * UPGRADE_SILENT_COMMIT_SUPPORTED_REQ} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     *
     * @since upgrade protocol v4
     */
    public static class UpgradeSilentCommitSupportedReq {

        /**
         * The length for the content of the {@link Enum#UPGRADE_SILENT_COMMIT_SUPPORTED_REQ
         * UPGRADE_SILENT_COMMIT_SUPPORTED_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SILENT_COMMIT_SUPPORTED_CFM
     * UPGRADE_SILENT_COMMIT_SUPPORTED_CFM} message.</p>
     * <blockquote><pre>
     *      0 bytes     1
     *      +-----------+
     *      |  SUPPORT  |
     *      +-----------+
     * </pre></blockquote>
     *
     * @since upgrade protocol v4
     */
    public static class UpgradeSilentCommitSupportedCFM {

        /**
         * <p>All value for the support field.</p>
         */
        public static class Support {

            /**
             * Used by the Device to inform that silent commit cannot be used.
             */
            public static final byte NOT_SUPPORTED = 0x00;
            /**
             * Used by the Device to inform that silent commit can be used.
             */
            public static final byte SUPPORTED = 0x01;
        }

        /**
         * The offset that contains the support field.
         */
        public static final int SUPPORT_OFFSET = 0;
        /**
         * The number of bytes that contains the support field.
         */
        public static final int SUPPORT_LENGTH = 1;
        /**
         * The length of the content for this message.
         */
        public static final int CONTENT_LENGTH = SUPPORT_LENGTH;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_SILENT_COMMIT_CFM
     * UPGRADE_SILENT_COMMIT_CFM} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     *
     * @since upgrade protocol v4
     */
    public static class UpgradeSilentCommitCfm {

        /**
         * The length for the content of the {@link Enum#UPGRADE_SILENT_COMMIT_CFM UPGRADE_SILENT_COMMIT_CFM}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_PUT_EARBUDS_IN_CASE_REQ
     * UPGRADE_PUT_EARBUDS_IN_CASE_REQ} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     *
     * @since upgrade protocol v5
     */
    public static class UpgradePutEarbudsInCaseReq {

        /**
         * The length for the content of the {@link Enum#UPGRADE_PUT_EARBUDS_IN_CASE_REQ UPGRADE_PUT_EARBUDS_IN_CASE_REQ}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_EARBUDS_IN_CASE_CFM
     * UPGRADE_EARBUDS_IN_CASE_CFM} message.</p>
     * <blockquote><pre>
     *      no content
     * </pre></blockquote>
     *
     * @since upgrade protocol v5
     */
    public static class UpgradePutEarbudsInCaseCfm {

        /**
         * The length for the content of the {@link Enum#UPGRADE_EARBUDS_IN_CASE_CFM UPGRADE_EARBUDS_IN_CASE_CFM}
         * message.
         */
        public static final int CONTENT_LENGTH = 0;
    }

    /**
     * <p>Complementary information for the structure or content of the {@link Enum#UPGRADE_COMPLETE_IND_WITH_STATUS
     * UPGRADE_COMPLETE_IND_WITH_STATUS} message.</p>
     * <blockquote><pre>
     *      0 bytes     1
     *      +-----------------+
     *      |  COMMIT STATUS  |
     *      +-----------------+
     * </pre></blockquote>
     *
     * @since upgrade protocol v6
     */
    public static class UpgradeCompleteINDWithStatus {
        /**
         * The offset that contains the status field.
         */
        public static final int STATUS_OFFSET = 0;
        /**
         * The number of bytes that contains the status field.
         */
        public static final int STATUS_LENGTH = 1;
        /**
         * The length of the content for this message.
         */
        public static final int CONTENT_LENGTH = STATUS_LENGTH;
    }


    /* ******* METHODS ******* */

    /**
     * <p>To retrieve the OpCode value from its byte value.</p>
     *
     * @param opCode
     *         The byte to obtain the corresponding {@link OpCodes.Enum OpCodes}.
     *
     * @return The corresponding OpCodes or -1 if not found.
     */
    @OpCodes.Enum
    public static int getOpCode(byte opCode) {
        switch (opCode) {
            case Enum.UPGRADE_START_REQ:
                return Enum.UPGRADE_START_REQ;

            case Enum.UPGRADE_START_CFM:
                return Enum.UPGRADE_START_CFM;

            case Enum.UPGRADE_DATA_BYTES_REQ:
                return Enum.UPGRADE_DATA_BYTES_REQ;

            case Enum.UPGRADE_DATA:
                return Enum.UPGRADE_DATA;

            case Enum.UPGRADE_ABORT_REQ:
                return Enum.UPGRADE_ABORT_REQ;

            case Enum.UPGRADE_ABORT_CFM:
                return Enum.UPGRADE_ABORT_CFM;

            case Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                return Enum.UPGRADE_TRANSFER_COMPLETE_IND;

            case Enum.UPGRADE_TRANSFER_COMPLETE_RES:
                return Enum.UPGRADE_TRANSFER_COMPLETE_RES;

            case Enum.UPGRADE_PROCEED_TO_COMMIT:
                return Enum.UPGRADE_PROCEED_TO_COMMIT;

            case Enum.UPGRADE_COMMIT_REQ:
                return Enum.UPGRADE_COMMIT_REQ;

            case Enum.UPGRADE_COMMIT_CFM:
                return Enum.UPGRADE_COMMIT_CFM;

            case Enum.UPGRADE_ERROR_IND:
                return Enum.UPGRADE_ERROR_IND;

            case Enum.UPGRADE_COMPLETE_IND:
                return Enum.UPGRADE_COMPLETE_IND;

            case Enum.UPGRADE_SYNC_REQ:
                return Enum.UPGRADE_SYNC_REQ;

            case Enum.UPGRADE_SYNC_CFM:
                return Enum.UPGRADE_SYNC_CFM;

            case Enum.UPGRADE_START_DATA_REQ:
                return Enum.UPGRADE_START_DATA_REQ;

            case Enum.UPGRADE_IS_VALIDATION_DONE_REQ:
                return Enum.UPGRADE_IS_VALIDATION_DONE_REQ;

            case Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                return Enum.UPGRADE_IS_VALIDATION_DONE_CFM;

            case Enum.UPGRADE_ERROR_RES:
                return Enum.UPGRADE_ERROR_RES;

            case Enum.UPGRADE_VERSION_REQ:
                return Enum.UPGRADE_VERSION_REQ;

            case Enum.UPGRADE_VERSION_CFM:
                return Enum.UPGRADE_VERSION_CFM;

            case Enum.UPGRADE_VARIANT_REQ:
                return Enum.UPGRADE_VARIANT_REQ;

            case Enum.UPGRADE_VARIANT_CFM:
                return Enum.UPGRADE_VARIANT_CFM;

            case Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_CFM:
                return Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_CFM;

            case Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_REQ:
                return Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_REQ;

            case Enum.UPGRADE_SILENT_COMMIT_CFM:
                return Enum.UPGRADE_SILENT_COMMIT_CFM;

            case Enum.UPGRADE_PUT_EARBUDS_IN_CASE_REQ:
                return Enum.UPGRADE_PUT_EARBUDS_IN_CASE_REQ;

            case Enum.UPGRADE_EARBUDS_IN_CASE_CFM:
                return Enum.UPGRADE_EARBUDS_IN_CASE_CFM;

            case Enum.UPGRADE_COMPLETE_IND_WITH_STATUS:
                return Enum.UPGRADE_COMPLETE_IND_WITH_STATUS;

            default:
                return -1;
        }

    }


    /**
     * <p>To get a readable version of the given OpCodes.</p>
     *
     * @param opCode
     *         The OpCodes.
     *
     * @return a human readable information for an OpCodes.
     */
    public static String getString(@OpCodes.Enum int opCode) {
        String message;
        switch (opCode) {
            case Enum.UPGRADE_START_REQ:
                message = "UPGRADE_START_REQ";
                break;

            case Enum.UPGRADE_START_CFM:
                message = "UPGRADE_START_CFM";
                break;

            case Enum.UPGRADE_DATA_BYTES_REQ:
                message = "UPGRADE_DATA_BYTES_REQ";
                break;

            case Enum.UPGRADE_DATA:
                message = "UPGRADE_DATA";
                break;

            case Enum.UPGRADE_ABORT_REQ:
                message = "UPGRADE_ABORT_REQ";
                break;

            case Enum.UPGRADE_ABORT_CFM:
                message = "UPGRADE_ABORT_CFM";
                break;

            case Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                message = "UPGRADE_TRANSFER_COMPLETE_IND";
                break;

            case Enum.UPGRADE_TRANSFER_COMPLETE_RES:
                message = "UPGRADE_TRANSFER_COMPLETE_RES";
                break;

            case Enum.UPGRADE_PROCEED_TO_COMMIT:
                message = "UPGRADE_PROCEED_TO_COMMIT";
                break;

            case Enum.UPGRADE_COMMIT_REQ:
                message = "UPGRADE_COMMIT_REQ";
                break;

            case Enum.UPGRADE_COMMIT_CFM:
                message = "UPGRADE_COMMIT_CFM";
                break;

            case Enum.UPGRADE_ERROR_IND:
                message = "UPGRADE_ERROR_IND";
                break;

            case Enum.UPGRADE_COMPLETE_IND:
                message = "UPGRADE_COMPLETE_IND";
                break;

            case Enum.UPGRADE_SYNC_REQ:
                message = "UPGRADE_SYNC_REQ";
                break;

            case Enum.UPGRADE_SYNC_CFM:
                message = "UPGRADE_SYNC_CFM";
                break;

            case Enum.UPGRADE_START_DATA_REQ:
                message = "UPGRADE_START_DATA_REQ";
                break;

            case Enum.UPGRADE_IS_VALIDATION_DONE_REQ:
                message = "UPGRADE_IS_VALIDATION_DONE_REQ";
                break;

            case Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                message = "UPGRADE_IS_VALIDATION_DONE_CFM";
                break;

            case Enum.UPGRADE_ERROR_RES:
                message = "UPGRADE_ERROR_RES";
                break;

            case Enum.UPGRADE_VERSION_REQ:
                message = "UPGRADE_VERSION_REQ";
                break;

            case Enum.UPGRADE_VERSION_CFM:
                message = "UPGRADE_VERSION_CFM";
                break;

            case Enum.UPGRADE_VARIANT_REQ:
                message = "UPGRADE_VARIANT_REQ";
                break;

            case Enum.UPGRADE_VARIANT_CFM:
                message = "UPGRADE_VARIANT_CFM";
                break;

            case Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_REQ:
                message = "UPGRADE_SILENT_COMMIT_SUPPORTED_REQ";
                break;

            case Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_CFM:
                message = "UPGRADE_SILENT_COMMIT_SUPPORTED_CFM";
                break;

            case Enum.UPGRADE_SILENT_COMMIT_CFM:
                message = "UPGRADE_SILENT_COMMIT_CFM";
                break;

            case Enum.UPGRADE_PUT_EARBUDS_IN_CASE_REQ:
                message = "UPGRADE_PUT_EARBUDS_IN_CASE_REQ";
                break;

            case Enum.UPGRADE_EARBUDS_IN_CASE_CFM:
                message = "UPGRADE_EARBUDS_IN_CASE_CFM";
                break;

            case Enum.UPGRADE_COMPLETE_IND_WITH_STATUS:
                message = "UPGRADE_COMPLETE_IND_WITH_STATUS";
                break;

            default:
                message = "UNDEFINED OPCODE";
        }

        return message + " (" + Utils.getHexadecimalStringTwoBytes(opCode) + ")";

    }

}
