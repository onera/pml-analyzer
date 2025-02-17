package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait DbusCXDYBXTransactionLibrary extends TransactionLibrary {
  self: DbusCXDYBXPlatform with DbusCXDYBXSoftware =>

  val coreLoadL1Transactions: Seq[Transaction] =
    for {
      (app, l1) <- standardCoreApplications.zip(rosace.cg0.cl0.L1Caches)
    } yield Transaction(s"tr_${l1}_ld", app read l1)

  val coreStoreL1Transactions: Seq[Transaction] =
    for {
      (app, l1) <- standardCoreApplications.zip(rosace.cg0.cl0.L1Caches)
    } yield Transaction(s"tr_${l1}_st", app write l1)

  val coreLoadL2Transactions: Seq[Transaction] =
    for { app <- standardCoreApplications } yield Transaction(
      s"tr_${app}_${rosace.cg0.cl0.l2}_ld",
      app read rosace.cg0.cl0.l2
    )

  val coreStoreL2Transactions: Seq[Transaction] =
    for { app <- standardCoreApplications } yield Transaction(
      s"tr_${app}_${rosace.cg0.cl0.l2}_st",
      app write rosace.cg0.cl0.l2
    )

  val coreLoadBankTransactions: Seq[Transaction] =
    for {
      (app, bank) <- standardCoreApplications.zip(rosace.ddr.banks)
    } yield Transaction(s"tr_${app}_${bank}_ld", app read bank)

  val coreStoreBankTransactions: Seq[Transaction] =
    for {
      (app, bank) <- standardCoreApplications.zip(rosace.ddr.banks)
    } yield Transaction(s"tr_${app}_${bank}_ld", app write bank)

  for {
    tr <- coreLoadL1Transactions
      ++ coreStoreL1Transactions
      ++ coreLoadL2Transactions
      ++ coreStoreL2Transactions
      ++ coreLoadBankTransactions
      ++ coreStoreBankTransactions
  }
    tr used

  val dspLoadSRAMTransactions: Seq[Transaction] =
    for {
      (app, sram) <- ioCoreApplications.zip(rosace.dg0.cl0.SRAM)
    } yield Transaction(s"tr_${app}_${sram}_ld", app read sram)

  val dspStoreSRAMTransactions: Seq[Transaction] =
    for {
      (app, sram) <- ioCoreApplications.zip(rosace.dg0.cl0.SRAM)
    } yield Transaction(s"tr_${app}_${sram}_st", app write sram)

  for { tr <- dspLoadSRAMTransactions ++ dspStoreSRAMTransactions }
    tr used

  val tr_CIO_dma_reg_ld: Transaction = Transaction(
    standardCoreApplications.head read rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_ld used

  val tr_CIO_dma_reg_st: Transaction = Transaction(
    standardCoreApplications.head write rosace.cfg_bus.dma_reg
  )
  tr_CIO_dma_reg_st used

  // FIXME These transactions are the same as coreLoadBankTransactions(0) and coreStoreBankTransactions(0)
//  val tr_CIO_BK0_ld: Transaction = Transaction(
//    standardCoreApplications.head read rosace.ddr.banks.head
//  )
//  tr_CIO_BK0_ld.used
//
//  val tr_CIO_BK0_st: Transaction = Transaction(
//    standardCoreApplications.head write rosace.ddr.banks.head
//  )
//  tr_CIO_BK0_st.used

  val tr_dma_BK0_eth: Scenario =
    Scenario(app_dma read rosace.ddr.banks.head, app_dma write rosace.eth)
  tr_dma_BK0_eth.used

  val dmaBKEthTransactions: Seq[Scenario] =
    for {
      i <- rosace.ddr.banks.indices by 2
    } yield {
      Scenario(
        s"tr_dma_BK${i}_eth",
        app_dma write rosace.ddr.banks(i),
        app_dma read rosace.eth
      )
    }

  for { tr <- dmaBKEthTransactions }
    tr used

  val tr_dma_eth_rosace_dg0_cl0_C2_SRAM: Option[Scenario] =
    if (rosace.dg0.cl0.cores.size >= 3)
      Some(
        Scenario(app_dma write rosace.dg0.cl0.SRAM(2), app_dma read rosace.eth)
      )
    else
      None

  for { tr <- tr_dma_eth_rosace_dg0_cl0_C2_SRAM } yield tr used
}
