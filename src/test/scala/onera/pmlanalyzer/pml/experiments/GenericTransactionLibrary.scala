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

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.{Target, Initiator}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait GenericTransactionLibrary extends TransactionLibrary {
  self: GenericPlatform with GenericSoftware =>

  def partition_resources[A, B](
                                 asking: Seq[A],
                                 resources: Seq[B]
                               ): Map[A, Seq[B]] = {
    if (asking.length <= resources.length) {
      asking.zip(resources.grouped(resources.length / asking.length)).toMap
    } else {
      resources
        .zip(asking.grouped(asking.length / resources.length))
        .flatMap((r, a) => a.map((_, Seq(r))))
        .toMap
    }
  }

  val groupToDdr: Map[GroupCore, Seq[ddr]] =
    partition_resources(groupCore, ddrs)

  val clusterToBanks: Map[ClusterCore, Seq[Target]] = groupToDdr.flatMap(
    (g, d) => partition_resources(g.clusters.flatten, d.flatMap(_.banks))
  )

  val coreToBanks: Map[Initiator, Seq[Target]] =
    clusterToBanks.flatMap((c, d) => partition_resources(c.cores, d))

  val basicCoreTransactions: Seq[Seq[Seq[Seq[Seq[Transaction]]]]] =
    for {gId <- groupCore.indices} yield for {
      clIdI <- groupCore(gId).clusters.indices
    } yield for {clIdJ <- groupCore(gId).clusters(clIdI).indices} yield for {
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

      val readL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_ld",
        application read cluster.L2
      )

      val storeL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_st",
        application write cluster.L2
      )

      // FIXME Name should use the DDR Id and Bank Id s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR${0}_BK${clIdI}_ld",
      val readBanks: Seq[Transaction] = for ((bank, bId) <- coreToBanks(core).zipWithIndex) yield {
        Transaction(
          s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR_BK${bId}_ld",
          application read bank,
        )
      }

      // FIXME Name should use the DDR Id and Bank Id s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR${0}_BK${clIdI}_st",
      val writeBanks: Seq[Transaction] = for ((bank, bId) <- coreToBanks(core).zipWithIndex) yield {
        Transaction(
          s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR_BK${bId}_ld",
          application write bank,
        )
      }

      //      readL1 used
      //      storeL1 used

      readL2 used

      storeL2 used

      for (t <- readBanks ++ writeBanks)
        t used

      Seq(
        readL2,
        storeL2,
      ) ++ readBanks ++ writeBanks
    }

  val basicDspTransactions: Seq[Seq[Seq[Seq[Seq[Transaction]]]]] =
    for {gId <- groupDSP.indices} yield for {
      clIdI <- groupDSP(gId).clusters.indices
    } yield for {clIdJ <- groupDSP(gId).clusters(clIdI).indices} yield for {
      cId <- groupDSP(gId).clusters(clIdI)(clIdJ).cores.indices
    } yield {
      val application = dspApplications(gId)(clIdI)(clIdJ)(cId)
      val cluster = groupDSP(gId).clusters(clIdI)(clIdJ)
      val dsp = cluster.cores(cId)
      val sram = cluster.SRAM(cId)

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
        writeSram,
      )


    }
}
