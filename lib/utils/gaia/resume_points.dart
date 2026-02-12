class ResumePoints {
  /// This is the resume point "0", that means the upgrade will start from the beginning, the upgradeStartDataReq
  /// request.
  static const int dataTransfer = 0x00;

  /// This is the 1st resume point, that means the upgrade should resume from the UPGRADE_IS_CSR_VALID_DONE_REQ
  /// request.
  static const int validation = 0x01;

  /// This is the 2nd resume point, that means the upgrade should resume from the upgradeTransferCompleteRes request.
  static const int transferComplete = 0x02;

  /// This is the 3rd resume point, that means the upgrade should resume from the upgradeInProgressRes request.
  static const int inProgress = 0x03;

  /// This is the 4th resume point, that means the upgrade should resume from the upgradeCommitCfm confirmation request.
  static const int commit = 0x04;
}
