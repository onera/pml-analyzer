package pml.examples.simpleKeystone

/**
  * Transaction used when app4, app1 and app21 are scheduled
  */
trait SimpleKeystoneLibraryConfigurationPlanApp21 extends SimpleKeystoneLibraryConfiguration {
  self: SimpleKeystonePlatform =>

  t41_app4_wr_input_d.used
  t211_app21_rd_input_d.used
  t212_app21_wr_d1.used
  t14_app1_rd_wr_L1.used
}
