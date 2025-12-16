/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.hardware.{Initiator, Target}
import onera.pmlanalyzer.*

import scala.language.postfixOps

trait GenericTransactionLibrary(withDMA: Boolean = true)
    extends TransactionLibrary {
  self: GenericPlatform with GenericSoftware =>

  def partition_resources[A, B](
      asking: Seq[A],
      resources: Seq[B]
  ): Map[A, Seq[B]] = {
    if (asking.isEmpty || resources.isEmpty) {
      asking.map((_, Seq.empty)).toMap
    } else if (asking.length <= resources.length) {
      asking.zip(resources.grouped(resources.length / asking.length)).toMap
    } else {
      partition_resources(resources, asking).toSeq
        .flatMap((k, v) => v.map(a => a -> Seq(k)))
        .groupMapReduce(_._1)(_._2)(_ ++ _)
    }
  }

  val coreToBanks: Map[Initiator, Seq[Target]] = {
    val cores = for {
      gC <- groupCore
      cl <- gC.clusters
      cl1 <- cl
      c <- cl1.cores
    } yield c
    partition_resources(cores, ddrs.flatMap(_.banks))
  }

  /**
   * General-purpose Cores transactions
   */
  val basicCoreTransactions: Seq[Seq[Seq[Seq[Seq[Transaction]]]]] =
    for { gId <- groupCore.indices } yield for {
      clIdI <- groupCore(gId).clusters.indices
    } yield for { clIdJ <- groupCore(gId).clusters(clIdI).indices } yield for {
      cId <- groupCore(gId).clusters(clIdI)(clIdJ).cores.indices
    } yield {
      val application = coreApplications(gId)(clIdI)(clIdJ)(cId)
      val cluster = groupCore(gId).clusters(clIdI)(clIdJ)
      val core = cluster.cores(cId)

      // Let's ignore trivially-free transactions for the moment
      //      val readL1 = Transaction(
      //        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L1_ld",
      //        application read cluster.coresL1(cId)
      //      )
      //
      //      val storeL1 = Transaction(
      //        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L1_st",
      //        application write cluster.coresL1(cId)
      //      )

      /**
       * Cores read from and write to their cluster L2 cache.
       */
      val readL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_ld",
        application read cluster.L2
      )

      val storeL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_st",
        application write cluster.L2
      )

      /**
       * Cores read from and write to their allocated memory banks.
       */
      val readBanks: Seq[Transaction] =
        for (bank <- coreToBanks(core)) yield {
          val bankName = bank.name.name.replace(s"${fullName}_", "").toUpperCase
          Transaction(
            s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_${bankName}_ld",
            application read bank
          )
        }

      val writeBanks: Seq[Transaction] =
        for (bank <- coreToBanks(core)) yield {
          val bankName = bank.name.name.replace(s"${fullName}_", "").toUpperCase
          Transaction(
            s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_${bankName}_st",
            application write bank
          )
        }

      /**
       * Tag all core transactions as used
       */

      //      readL1 used
      //      storeL1 used

      readL2 used

      storeL2 used

      for (t <- readBanks ++ writeBanks)
        t used

      Seq(
        readL2,
        storeL2
      ) ++ readBanks ++ writeBanks
    }

  /**
   * DSP Cores transactions
   */
  val basicDspTransactions: Seq[Seq[Seq[Seq[Seq[Transaction]]]]] =
    for { gId <- groupDSP.indices } yield for {
      clIdI <- groupDSP(gId).clusters.indices
    } yield for { clIdJ <- groupDSP(gId).clusters(clIdI).indices } yield for {
      cId <- groupDSP(gId).clusters(clIdI)(clIdJ).cores.indices
    } yield {
      val application = dspApplications(gId)(clIdI)(clIdJ)(cId)
      val cluster = groupDSP(gId).clusters(clIdI)(clIdJ)
      val dsp = cluster.cores(cId)
      val sram = cluster.SRAM(cId)

      /**
       * DSP Cores read from and write to their local SRAM.
       */
      val readSram = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_SRAM_ld",
        application read sram
      )

      val writeSram = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_SRAM_st",
        application write sram
      )

      readSram used

      writeSram used

      Seq(
        readSram,
        writeSram
      )

    }

  /**
   * DMA Transactions
   */
  private val dma_rd_eth = Transaction(app_dma read eth)
  val basicDmaTransactions: Seq[Transaction] = {
    val ddrCopies: Seq[Transaction] = for {
      ddr <- ddrs
      (bank, j) <- ddr.banks.zipWithIndex
    } yield {
      val dma_wr_bank = Transaction(s"dma_wr_bank${ddr.id}_BK$j",app_dma write bank)
      Transaction(
        s"t_dma_st_DDR${ddr.id}_BK${j}",
        dma_rd_eth,
        dma_wr_bank
      )
    }

    val sramCopies: Seq[Transaction] = for {
      group <- groupDSP
      cluster <- group.clusters.flatten
      sram <- cluster.SRAM
    } yield {
      val dma_wr_sram = Transaction(s"dma_wr_sram${sram.name}", app_dma write sram)
      Transaction(
        s"t_dma_rd_G${group.id}_Cl${cluster.id}_SRAM${sram.name}",
        dma_rd_eth,
        dma_wr_sram
      )
    }

    val dma_rd_reg = Transaction(app_dma read cfg_bus.dma_reg)
    val dma_wr_reg = Transaction(app_dma write cfg_bus.dma_reg)

    val dma_rd_config = Transaction(
      "t_dma_rd_dma_reg",
      dma_rd_reg,
      dma_wr_reg
    )

    Seq(dma_rd_config) ++ ddrCopies ++ sramCopies
  }

  if (withDMA) {
    for (s <- basicDmaTransactions)
      s used
  }
}
