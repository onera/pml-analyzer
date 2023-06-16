package pml.examples.simpleT1042

trait SimpleT1042LibraryConfiguration extends SimpleT1042TransactionLibrary with SimpleSoftwareAllocation {
  self: SimpleT1042Platform =>

  app1_rd_interrupt1.used
  app1_rd_d1.used
  app1_wr_d2.used

}
