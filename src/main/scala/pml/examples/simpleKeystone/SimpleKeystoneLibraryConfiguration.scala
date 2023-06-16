package pml.examples.simpleKeystone

/**
  * Transaction that are always used.
  * A user transaction is considered during the analyses if identified as so.
  * For instance to indicate that the t11 transaction defined in [[SimpleKeystoneTransactionLibrary]] is used
  * {{{t11_app1_rd_interrupt1.used}}}
  * @see [[pml.operators.Use.Ops]] for operator definition
  */
trait SimpleKeystoneLibraryConfiguration extends SimpleKeystoneTransactionLibrary with SimpleSoftwareAllocation {
  self: SimpleKeystonePlatform =>

  t11_app1_rd_interrupt1.used
  t12_app1_rd_d1.used
  t13_app1_wr_d2.used

}
