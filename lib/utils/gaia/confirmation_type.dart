class ConfirmationType {
  /// <p>When the manager receives the
  /// `OpCodes.Enum#upgradeTransferCompleteInd
  /// upgradeTransferCompleteInd` message, the board is asking for a confirmation to
  /// `OpCodes.UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE`
  /// or `OpCodes.UpgradeTransferCompleteRES.Action#ABORT ABORT`  the
  /// process.</p>
  static const int transferComplete = 1;

  /// <p>When the manager receives the
  /// `OpCodes.Enum#upgradeCommitReq upgradeCommitReq` message, the
  /// board is asking for a confirmation to
  /// `OpCodes.UpgradeCommitCFM.Action#CONTINUE CONTINUE`
  /// or `OpCodes.UpgradeCommitCFM.Action#ABORT ABORT`  the process.</p>
  static const int commit = 2;

  /// <p>When the resume point
  /// `ResumePoints.Enum#inProgress inProgress` is reached, the board
  /// is expecting to receive a confirmation to
  /// `OpCodes.UpgradeInProgressRES.Action#CONTINUE CONTINUE`
  /// or `OpCodes.UpgradeInProgressRES.Action#ABORT ABORT` the process.</p>
  static const int inProgress = 3;

  /// <p>When the Host receives
  /// `com.qualcomm.qti.libraries.vmupgrade.codes.ReturnCodes.Enum#WARN_SYNC_ID_IS_DIFFERENT WARN_SYNC_ID_IS_DIFFERENT`,
  /// the listener has to ask if the upgrade should continue or not.</p>
  static const int warningFileIsDifferent = 4;

  /// <p>>When the Host receives
  /// `com.qualcomm.qti.libraries.vmupgrade.codes.ReturnCodes.Enum#ERROR_BATTERY_LOW ERROR_BATTERY_LOW`,the
  /// listener has to ask if the upgrade should continue or not.</p>
  static const int batteryLowOnDevice = 5;
}
