package pml.examples.simpleKeystone

/**
  * Transaction used when app22, app1 and app3 are scheduled
  */
trait SimpleKeystoneLibraryConfigurationPlanApp22 extends SimpleKeystoneLibraryConfiguration {
  self: SimpleKeystonePlatform =>

  t14_app1_rd_wr_L1.used
  t221_app22_rd_d2.used
  t222_app22_wr_output_d.used
  t223_app22_st_dma_reg.used
  app3_transfer.used

}
