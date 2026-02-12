class OpCodes {
  /// <p>To request an upgrade procedure to start.</p> <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous
  /// message</b></dt><dd>`OpCodes.Enum#upgradeSyncCfm upgradeSyncCfm` from device.</dd> <dt><b>Next
  /// message</b></dt><dd>`OpCodes.Enum#upgradeStartCfm upgradeStartCfm` from the device.</dd> </dl>
  static const upgradeStartReq = 0x01;

  /// <p>To confirm the start of the upgrade procedure.</p> <dl> <dt><b>Content</b></dt><dd>Contains a value to
  /// indicate if the Device is ready for the upgrade, see `UpgradeStartCFM`.</dd> <dt><b>Previous
  /// message</b></dt><dd>`OpCodes.Enum#upgradeStartReq upgradeStartReq`</dd> <dt><b>Next
  /// message</b></dt><dd>depends on the `ResumePostatic consts` value received by the Host in the `/// OpCodes.Enum#upgradeSyncCfm upgradeSyncCfm` message: <table> <tr> <td>`/// ResumePostatic consts.Enum#dataTransfer dataTransfer`</td> <td> &#8658; `OpCodes.Enum#upgradeStartReq` from
  /// application.</td> </tr> <tr> <td>`ResumePostatic consts.Enum#validation validation`</td> <td> &#8658; `/// OpCodes.Enum#upgradeIsValidationDoneReq` from application.</td> </tr> <tr> <td>`/// ResumePostatic consts.Enum#transferComplete transferComplete`</td> <td> &#8658; `/// OpCodes.Enum#upgradeTransferCompleteRes` from application.</td> </tr> <tr> <td>`/// ResumePostatic consts.Enum#inProgress inProgress`</td> <td> &#8658; `OpCodes.Enum#upgradeInProgressRes`
  /// from application.</td> </tr> <tr> <td>`ResumePostatic consts.Enum#commit commit`</td> <td> &#8658; `/// OpCodes.Enum#upgradeCommitCfm` from application.</td> </tr> </table> </dd> </dl>
  static const upgradeStartCfm = 0x02;

  /// <p>To request the section of the upgrade image file bytes array expected by the board.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>The length and the offset of the required section from the upgrade image
  /// file.</dd> <dt><b>Previous message</b></dt><dd> <ul style="list-style-type:none"> <li>`/// OpCodes.Enum#upgradeData upgradeData` from application</li> <li>`OpCodes.Enum#upgradeStartDataReq
  /// upgradeStartDataReq` from application</li> </ul> </dd> <dt><b>Next message</b></dt><dd>`/// OpCodes.Enum#upgradeData upgradeData` from the application.</dd> </dl>
  static const upgradeDataBytesReq = 0x03;

  /// <p>To transfer sections of the upgrade image file to the board.</p> <dl> <dt><b>Content</b></dt><dd>The
  /// section from the upgrade file which has been requested by the Device.</dd> <dt><b>Previous
  /// message</b></dt><dd>`OpCodes.Enum#upgradeDataBytesReq upgradeDataBytesReq` from device.</dd>
  /// <dt><b>Next message</b></dt><dd> <ul style="list-style-type:none"> <li>`/// OpCodes.Enum#upgradeIsValidationDoneReq upgradeIsValidationDoneReq` from application.</li> <li>`/// OpCodes.Enum#upgradeDataBytesReq upgradeDataBytesReq` from device.</li> </ul> </dd> </dl>
  static const upgradeData = 0x04;

  /// @deprecated <p>Was sent by the device.</p> <p>The device may send this message to suspend transmission of
  /// `Enum#upgradeData upgradeData` messages from the Host. This is used as flow control when the device
  /// is busy and cannot accept more data.</p>
  static const upgradeSuspendInd = 0x05;

  /// @deprecated <p>Was sent by device.</p> <p>If the device has sent an `Enum#upgradeSuspendInd
  /// upgradeSuspendInd` message to the Host it will resume transmission of `Enum#upgradeData
  /// upgradeData` messages by sending this message</p>
  static const upgradeResumeInd = 0x06;

  /// <p>To abort the upgrade procedure.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>any message or none.</dd>
  /// <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeAbortCfm upgradeAbortCfm` from device.</dd>
  /// </dl>
  static const upgradeAbortReq = 0x07;

  /// <p>To confirm the abortion of the upgrade</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeAbortReq upgradeAbortReq` from application.</dd> <dt><b>Next
  /// message</b></dt><dd>None: disconnection of the upgrade?</dd> </dl>
  static const upgradeAbortCfm = 0x08;

  /// @deprecated <p>Was sent by Host.</p> <p>The host can use this message to request an update on the
  /// progress of the upgrade image download. The device will respond with an `Enum#upgradeProgressCfm
  /// upgradeProgressCfm` message</p>
  static const upgradeProgressReq = 0x09;

  /// @deprecated <p>Was sent by Device.</p> <p>The device uses this message to respond to an `/// Enum#upgradeProgressReq upgradeProgressReq` message from the host. It indicates the current percentage of
  /// completion of the upgrade image file download from the host.</p>
  static const upgradeProgressCfm = 0x0A;

  /// <p>To indicate the upgrade image file has successfully been received and validated.</p>
  /// <p/>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeIsValidationDoneReq upgradeIsValidationDoneReq` from application.</dd>
  /// <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeTransferCompleteRes
  /// upgradeTransferCompleteRes` from application.</dd> </dl>
  static const upgradeTransferCompleteInd = 0x0B;

  /// <p>To respond to the `OpCodes.Enum#upgradeTransferCompleteInd upgradeTransferCompleteInd` message
  /// .</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>Contains `UpgradeTransferCompleteRES.Action#ABORT ABORT` or `/// UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE` information.</dd> <dt><b>Previous
  /// message</b></dt><dd>`OpCodes.Enum#upgradeTransferCompleteInd upgradeTransferCompleteInd` from
  /// device.</dd> <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeSyncReq upgradeSyncReq` from
  /// application after the reboot of the device.</dd> </dl>
  static const upgradeTransferCompleteRes = 0x0C;

  /// @deprecated <p>Was sent by Device.</p> <p>Following reboot of the device to perform the upgrade, the device
  /// will reconnect to the host.</p>
  static const upgradeInProgressInd = 0x0D;

  /// <p>To inform the Device that the Host would like to continue the upgrade process.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>Contains `UpgradeInProgressRES.Action#CONTINUE CONTINUE`
  /// information.</dd> <dt><b>Previous message</b></dt><dd>`OpCodes.Enum#upgradeStartCfm
  /// upgradeStartCfm` which should contain the Resume postatic const 3: `ResumePostatic consts.Enum#inProgress
  /// inProgress`.</dd> <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeCommitReq upgradeCommitReq`
  /// from the device.</dd> </dl>
  static const upgradeInProgressRes = 0x0E;

  /// <p>Used by the board to indicate it is ready for permission to commit the upgrade.</p> <dl>
  /// <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeInProgressRes upgradeInProgressRes` from the Host.</dd> <dt><b>Next
  /// message</b></dt><dd>`OpCodes.Enum#upgradeCommitCfm upgradeCommitCfm` from the Host.</dd> </dl>
  static const upgradeCommitReq = 0x0F;

  /// <p>To respond to the `OpCodes.Enum#upgradeCommitReq upgradeCommitReq` message from the board.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>0x00 to indicate to continue the upgrade, 0x01 to abort. See `/// UpgradeCommitCFM UpgradeCommitCFM`.</dd> <dt><b>Previous message</b></dt><dd>Two possibilities:<ul><li>`/// OpCodes.Enum#upgradeStartCfm upgradeStartCfm` from the Device which should contain the Resume postatic const 4:
  /// `ResumePostatic consts.Enum#commit commit`.</li> <li>`OpCodes.Enum#upgradeCommitReq upgradeCommitReq`
  /// from the Device.</li></ul></dd> <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeTransferCompleteInd
  /// upgradeTransferCompleteInd` from Device.</dd> </dl>
  static const upgradeCommitCfm = 0x10;

  /// <p>Used by the Device to inform the application about errors or warnings. Errors are considered as fatal.
  /// Warnings are considered as informational.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>Contains a `ReturnCodes ReturnCodes`.</dd> <dt><b>Previous
  /// message</b></dt><dd>none</dd> <dt><b>Next message</b></dt><dd>depends on the Return Code and any user
  /// action.</dd> </dl>
  static const upgradeErrorWarnInd = 0x11;

  /// <p>Used by the board to indicate the upgrade has been completed.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeCommitCfm upgradeCommitCfm` from Host.</dd> <dt><b>Next message</b></dt><dd>None,
  /// that one is the last one of a successful upgrade.</dd> </dl>
  static const upgradeCompleteInd = 0x12;

  /// <p>Used by the application to synchronize with the board before any other protocol message.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>ID of the upgrade which corresponds to the MD5 check sum of the upgrade
  /// file.</dd> <dt><b>Previous message</b></dt><dd>None, that one is the initiator of the process.</dd>
  /// <dt><b>Next message</b></dt><dd>`OpCodes.Enum#upgradeSyncCfm upgradeSyncCfm` from Device.</dd>
  /// </dl>
  static const upgradeSyncReq = 0x13;

  /// <p>Used by the board to respond to the `OpCodes.Enum#upgradeSyncReq upgradeSyncReq` message.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>A `ResumePostatic consts` value.</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeSyncReq upgradeStartReq` from Device.</dd> <dt><b>Next message</b></dt><dd>`/// OpCodes.Enum#upgradeStartReq upgradeStartReq` from Device.</dd> </dl>
  static const upgradeSyncCfm = 0x14;

  /// <p>Used by the Host to start a data transfer.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeStartCfm upgradeStartCfm` from Device.</dd> <dt><b>Next message</b></dt><dd>`/// OpCodes.Enum#UPGRADE_DATA_static constS_REQ upgradeDataBytesReq` from Device.</dd> </dl>
  static const upgradeStartDataReq = 0x15;

  /// <p>Used by the Host to request for executable partition validation status.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>Three possibilities from
  /// Device: <ul><li>`OpCodes.Enum#upgradeIsValidationDoneCfm upgradeIsValidationDoneCfm`</li>
  /// <li>`OpCodes.Enum#upgradeData upgradeData`</li> <li>`OpCodes.Enum#upgradeStartCfm
  /// upgradeStartCfm`</li> </ul></dd> <dt><b>Next message</b></dt><dd>Two possibilities from Device:
  /// <ul><li>`OpCodes.Enum#upgradeIsValidationDoneCfm upgradeIsValidationDoneCfm`</li> <li>`/// OpCodes.Enum#upgradeTransferCompleteInd upgradeTransferCompleteInd`</li> </ul></dd> </dl>
  static const upgradeIsValidationDoneReq = 0x16;

  /// <p>Used by the Device to respond to the `OpCodes.Enum#upgradeIsValidationDoneReq
  /// upgradeIsValidationDoneReq` message.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>none</dd> <dt><b>Previous message</b></dt><dd>`/// OpCodes.Enum#upgradeIsValidationDoneReq upgradeIsValidationDoneReq` from Device.</dd> <dt><b>Next
  /// message</b></dt><dd>`OpCodes.Enum#upgradeIsValidationDoneReq upgradeIsValidationDoneReq` from
  /// Device.</dd> </dl>
  static const upgradeIsValidationDoneCfm = 0x17;

  /// @deprecated <p>Was sent by Host.</p> <p>The Host must send this message reboot for commit.</p>
  static const upgradeSyncAfterRebootReq = 0x18;

  /// <i>no documentation</i>
  static const upgradeVersionReq = 0x19;

  /// <i>no documentation</i>
  static const upgradeVersionCfm = 0x1A;

  /// <i>no documentation</i>
  static const upgradeVariantReq = 0x1B;

  /// <i>no documentation</i>
  static const upgradeVariantCfm = 0x1C;

  /// @deprecated <p>Was sent by Device.</p> <p>The device may send this message instead of `/// Enum#upgradeCommitReq upgradeCommitReq` (it depends on file content).</p>
  static const upgradeEraseSqifReq = 0x1D;

  /// @deprecated <p>Was sent by Host.</p> <p>The host must respond to the `Enum#upgradeEraseSqifReq
  /// upgradeEraseSqifReq` message from the device with this message.</p>
  static const upgradeEraseSqifCfm = 0x1E;

  /// <p>Used by the Host to confirm it received an error or a warning message from the board.</p>
  /// <p/>
  /// <dl> <dt><b>Content</b></dt><dd>The `ReturnCodes ReturnCodes` received.</dd> <dt><b>Previous
  /// message</b></dt><dd>`OpCodes.Enum#upgradeErrorWarnInd upgradeErrorWarnInd` from Device.</dd>
  /// <dt><b>Next message</b></dt><dd>Depends on the received `ReturnCodes ReturnCodes` value.</dd> </dl>
  static const upgradeErrorWarnRes = 0x1F;

  /// <p>The number of bytes which contains the number of bytes of the uploading file to send.</p>
  static const nbBytesLength = 4;

  /// <p>The offset in the `Enum#upgradeDataBytesReq upgradeDataBytesReq` bytes data where the "number
  /// of bytes to send" information starts.</p>
  static const nbBytesOffset = 0;

  /// <p>The number of bytes which contains the byte offset within the upgrade file from which the host should
  /// start transferring data to the device.</p>
  static const fileOffsetLength = 4;

  /// <p>The offset in the `Enum#upgradeDataBytesReq upgradeDataBytesReq` bytes data where the file
  /// offset information starts. .</p>
  static const fileOffsetOffset = nbBytesOffset + nbBytesLength;

  /// The length for the data of the `Enum#upgradeDataBytesReq upgradeDataBytesReq` message.
  static const dataLength = fileOffsetLength + nbBytesLength;
}
