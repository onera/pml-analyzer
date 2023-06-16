package pml.examples.simpleT1042

trait SimpleT1042LibraryConfigurationPlanApp22 extends SimpleT1042LibraryConfiguration {
  self: SimpleT1042Platform =>

  app1_rd_wr_L1.used
  app22_rd_d2.used
  app22_wr_output_d.used
  app22_st_dma_reg.used
  app3_transfer.used

}
