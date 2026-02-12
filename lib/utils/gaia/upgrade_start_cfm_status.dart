class UpgradeStartCFMStatus {
  /// Value for an `Enum#upgradeStartCfm upgradeStartCfm` message when the device is ready to start
  /// the upgrade process.
  static const int success = 0x00;

  /// Value for an `Enum#upgradeStartCfm upgradeStartCfm` message when the device is not ready to
  /// start the upgrade process.
  static const int errorAppNotReady = 0x09;
}
