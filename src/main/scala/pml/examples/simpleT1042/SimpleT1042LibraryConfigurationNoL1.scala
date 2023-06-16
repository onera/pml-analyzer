package pml.examples.simpleT1042

trait SimpleT1042LibraryConfigurationNoL1 extends SimpleT1042LibraryConfiguration {
  self: SimpleT1042Platform =>

  app4_wr_input_d.used
  app21_rd_input_d.used
  app21_wr_d1.used
  app22_rd_d2.used
  app22_wr_output_d.used
  app22_st_dma_reg.used
  app3_transfer.used
}
