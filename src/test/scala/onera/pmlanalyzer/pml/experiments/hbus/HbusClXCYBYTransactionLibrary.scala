/** *****************************************************************************
 * Copyright (c) 2023. ONERA This file is part of PML Analyzer
 *
 * PML Analyzer is free software ; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation ; either version 2 of the License, or (at your
 * option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program ; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait HbusClXCYBYTransactionLibrary extends TransactionLibrary {
  self: HbusClXCYBYPlatform with HbusClXCYBYSoftware =>

  private def coreTransactions(clusterId: Int, coreId: Int): Seq[Transaction] = {
    val readL1 = Transaction(
      s"t_Cl${clusterId}_C${coreId}_L1_ld",
      clusterApplications(clusterId)(coreId) read cg0.clusters(clusterId).coresL1(coreId)
    )

    val storeL1 = Transaction(
      s"t_Cl${clusterId}_C${coreId}_L1_st",
      clusterApplications(clusterId)(coreId) write cg0.clusters(clusterId).coresL1(coreId)
    )

    val readL2 = Transaction(
      s"t_Cl${clusterId}_C${coreId}_L2_ld",
      clusterApplications(clusterId)(coreId) read cg0.clusters(clusterId).L2
    )

    val storeL2 = Transaction(
      s"t_Cl${clusterId}_C${coreId}_L2_st",
      clusterApplications(clusterId)(coreId) write cg0.clusters(clusterId).L2
    )

    val readBank = Transaction(
      s"t_Cl${clusterId}_C${coreId}_BK${clusterId}_ld",
      clusterApplications(clusterId)(coreId) read ddr.banks(clusterId)
    )

    val storeBank = Transaction(
      s"t_Cl${clusterId}_C${coreId}_BK${clusterId}_st",
      clusterApplications(clusterId)(coreId) write ddr.banks(clusterId)
    )

    Seq(
      readL1,
      storeL1,
      readL2,
      storeL2,
      readBank,
      storeBank
    )
  }

  val basicCoreTransactions: Seq[Seq[Seq[Transaction]]] =
    for {i <- cg0.clusters.indices} yield
      for {j <- cg0.clusters(i).cores.indices} yield
        coreTransactions(i, j)

  for {
    cl <- basicCoreTransactions
    c <- cl
    tr <- c
  }
    tr used

  val tC0_DMAReg_ld: Transaction = Transaction(clusterApplications(0)(0) read cfg_bus.dma_reg)
  tC0_DMAReg_ld.used

  val tC0_DMAReg_st: Transaction = Transaction(clusterApplications(0)(0) write cfg_bus.dma_reg)
  tC0_DMAReg_st.used

  val tC0ReadBankTransactions: Seq[Transaction] =
    for {
      i <- ddr.banks.indices by 2
    } yield {
      Transaction(
        s"t_Cl0_C0_BK${i}_ld",
        clusterApplications(0)(0) read ddr.banks(i))
    }

  for {tr <- tC0ReadBankTransactions}
    tr used

  val tC0StoreBankTransactions: Seq[Transaction] =
    for {
      i <- ddr.banks.indices by 2
    } yield {
      Transaction(
        s"t_Cl0_C0_BK${i}_st",
        clusterApplications(0)(0) write ddr.banks(i))
    }

  for {tr <- tC0StoreBankTransactions}
    tr used

  val dmaTransactions: Seq[Scenario] =
    for {
      i <- ddr.banks.indices by 2
    } yield {
      if (i == 2)
        Scenario(
          s"t_DMA_BK${i}_ETH",
          app_dma read ddr.banks(i), app_dma write eth)
      else
        Scenario(
          s"t_DMA_BK${i}_ETH",
          app_dma write ddr.banks(i), app_dma read eth)
    }

  for {tr <- dmaTransactions}
    tr used
}
