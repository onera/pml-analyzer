package pml.examples.simpleKeystone

/**
  * All transaction are used
  */
trait SimpleKeystoneLibraryConfigurationFull extends SimpleKeystoneLibraryConfiguration {
  self: SimpleKeystonePlatform =>

  t41_app4_wr_input_d.used
  t211_app21_rd_input_d.used
  t14_app1_rd_wr_L1.used
  t212_app21_wr_d1.used
  t221_app22_rd_d2.used
  t222_app22_wr_output_d.used
  t223_app22_st_dma_reg.used
  app3_transfer.used
}
