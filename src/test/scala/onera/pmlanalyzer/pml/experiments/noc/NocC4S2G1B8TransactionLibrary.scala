package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait NocC4S2G1B8TransactionLibrary extends TransactionLibrary {
  self: NocC4S2G1B8Platform with NocC4S2G1B8Software =>

  val tr_rosace_cg0_cl0_C0_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cg0.cl0.C0_L1
  )
  tr_rosace_cg0_cl0_C0_l1_ld.used

  val tr_rosace_cg0_cl0_C0_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cg0.cl0.C0_L1
  )
  tr_rosace_cg0_cl0_C0_l1_st.used

  val tr_rosace_cg0_cl0_C0_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C0_l2_ld.used

  val tr_rosace_cg0_cl0_C0_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cg0.cl0.l2
  )
  tr_rosace_cg0_cl0_C0_l2_st.used

  val tr_rosace_cg0_cl0_C0_BK0_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK0
  )
  tr_rosace_cg0_cl0_C0_BK0_ld.used

  val tr_rosace_cg0_cl0_C0_BK0_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK0
  )
  tr_rosace_cg0_cl0_C0_BK0_st.used

  val tr_rosace_cg0_cl0_C0_BK1_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK1
  )
  tr_rosace_cg0_cl0_C0_BK1_ld.used

  val tr_rosace_cg0_cl0_C0_BK1_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK1
  )
  tr_rosace_cg0_cl0_C0_BK1_st.used

  val tr_rosace_cg0_cl1_C0_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 read rosace.cg0.cl1.C0_L1
  )
  tr_rosace_cg0_cl1_C0_l1_ld.used

  val tr_rosace_cg0_cl1_C0_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 write rosace.cg0.cl1.C0_L1
  )
  tr_rosace_cg0_cl1_C0_l1_st.used

  val tr_rosace_cg0_cl1_C0_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 read rosace.cg0.cl1.l2
  )
  tr_rosace_cg0_cl1_C0_l2_ld.used

  val tr_rosace_cg0_cl1_C0_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 write rosace.cg0.cl1.l2
  )
  tr_rosace_cg0_cl1_C0_l2_st.used

  val tr_rosace_cg0_cl1_C0_BK2_ld: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 read rosace.ddr.BK2
  )
  tr_rosace_cg0_cl1_C0_BK2_ld.used

  val tr_rosace_cg0_cl1_C0_BK2_st: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 write rosace.ddr.BK2
  )
  tr_rosace_cg0_cl1_C0_BK2_st.used

  val tr_rosace_cg0_cl1_C0_BK3_ld: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 read rosace.ddr.BK3
  )
  tr_rosace_cg0_cl1_C0_BK3_ld.used

  val tr_rosace_cg0_cl1_C0_BK3_st: Transaction = Transaction(
    app_rosace_cg0_cl1_C0 write rosace.ddr.BK3
  )
  tr_rosace_cg0_cl1_C0_BK3_st.used

  val tr_rosace_cg0_cl2_C0_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 read rosace.cg0.cl2.C0_L1
  )
  tr_rosace_cg0_cl2_C0_l1_ld.used

  val tr_rosace_cg0_cl2_C0_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 write rosace.cg0.cl2.C0_L1
  )
  tr_rosace_cg0_cl2_C0_l1_st.used

  val tr_rosace_cg0_cl2_C0_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 read rosace.cg0.cl2.l2
  )
  tr_rosace_cg0_cl2_C0_l2_ld.used

  val tr_rosace_cg0_cl2_C0_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 write rosace.cg0.cl2.l2
  )
  tr_rosace_cg0_cl2_C0_l2_st.used

  val tr_rosace_cg0_cl2_C0_BK4_ld: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 read rosace.ddr.BK4
  )
  tr_rosace_cg0_cl2_C0_BK4_ld.used

  val tr_rosace_cg0_cl2_C0_BK4_st: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 write rosace.ddr.BK4
  )
  tr_rosace_cg0_cl2_C0_BK4_st.used

  val tr_rosace_cg0_cl2_C0_BK5_ld: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 read rosace.ddr.BK5
  )
  tr_rosace_cg0_cl2_C0_BK5_ld.used

  val tr_rosace_cg0_cl2_C0_BK5_st: Transaction = Transaction(
    app_rosace_cg0_cl2_C0 write rosace.ddr.BK5
  )
  tr_rosace_cg0_cl2_C0_BK5_st.used

  val tr_rosace_cg0_cl3_C0_l1_ld: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 read rosace.cg0.cl3.C0_L1
  )
  tr_rosace_cg0_cl3_C0_l1_ld.used

  val tr_rosace_cg0_cl3_C0_l1_st: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 write rosace.cg0.cl3.C0_L1
  )
  tr_rosace_cg0_cl3_C0_l1_st.used

  val tr_rosace_cg0_cl3_C0_l2_ld: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 read rosace.cg0.cl3.l2
  )
  tr_rosace_cg0_cl3_C0_l2_ld.used

  val tr_rosace_cg0_cl3_C0_l2_st: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 write rosace.cg0.cl3.l2
  )
  tr_rosace_cg0_cl3_C0_l2_st.used

  val tr_rosace_cg0_cl3_C0_BK6_ld: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 read rosace.ddr.BK6
  )
  tr_rosace_cg0_cl3_C0_BK6_ld.used

  val tr_rosace_cg0_cl3_C0_BK6_st: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 write rosace.ddr.BK6
  )
  tr_rosace_cg0_cl3_C0_BK6_st.used

  val tr_rosace_cg0_cl3_C0_BK7_ld: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 read rosace.ddr.BK7
  )
  tr_rosace_cg0_cl3_C0_BK7_ld.used

  val tr_rosace_cg0_cl3_C0_BK7_st: Transaction = Transaction(
    app_rosace_cg0_cl3_C0 write rosace.ddr.BK7
  )
  tr_rosace_cg0_cl3_C0_BK7_st.used

  val tr_CIO_dma_reg_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_ld.used

  val tr_CIO_dma_reg_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_st.used

  val tr_CIO_BK0_ld: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 read rosace.ddr.BK0
  )
  tr_CIO_BK0_ld.used

  val tr_CIO_BK0_st: Transaction = Transaction(
    app_rosace_cg0_cl0_C0 write rosace.ddr.BK0
  )
  tr_CIO_BK0_st.used

  val tr_dma_BK0_eth: Scenario =
    Scenario(app_dma read rosace.ddr.BK0, app_dma write rosace.eth)
  tr_dma_BK0_eth.used

  val tr_dma_eth_BK2: Scenario =
    Scenario(app_dma write rosace.ddr.BK2, app_dma read rosace.eth)
  tr_dma_eth_BK2.used

  val tr_dma_eth_BK4: Scenario =
    Scenario(app_dma write rosace.ddr.BK4, app_dma read rosace.eth)
  tr_dma_eth_BK4.used

  val tr_dma_eth_BK6: Scenario =
    Scenario(app_dma write rosace.ddr.BK6, app_dma read rosace.eth)
  tr_dma_eth_BK6.used

}
