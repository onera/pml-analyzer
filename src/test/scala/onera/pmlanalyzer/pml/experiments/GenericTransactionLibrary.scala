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
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait GenericTransactionLibrary extends TransactionLibrary {
  self: GenericPlatform with GenericSoftware =>

  val basicCoreTransactions: Seq[Seq[Seq[Seq[Seq[Transaction]]]]] =
    for { gId <- groupCore.indices } yield for {
      clIdI <- groupCore(gId).clusters.indices
    } yield for { clIdJ <- groupCore(gId).clusters(clIdI).indices } yield for {
      cId <- groupCore(gId).clusters(clIdI)(clIdJ).cores.indices
    } yield {
      val application = coreApplications(gId)(clIdI)(clIdJ)(cId)
      val cluster = groupCore(gId).clusters(clIdI)(clIdJ)
      val readL1 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L1_ld",
        application read cluster.coresL1(cId)
      )

      val storeL1 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L1_st",
        application write cluster.coresL1(cId)
      )

      val readL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_ld",
        application read cluster.L2
      )

      val storeL2 = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_L2_st",
        application write cluster.L2
      )

      // FIXME How to know the DDR used by groups?

      val readBank = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR${0}_BK${clIdI}_ld",
        application read ddrs(0).banks(clIdI)
      )

      val storeBank = Transaction(
        s"t_G${gId}_Cl${clIdI}_${clIdJ}_C${cId}_DDR${0}_BK${clIdI}_st",
        application write ddrs(0).banks(clIdI)
      )

      readL1 used

      storeL1 used

      readL2 used

      storeL2 used

      readBank used

      storeBank used

      Seq(
        readL1,
        storeL1,
        readL2,
        storeL2,
        readBank,
        storeBank
      )
    }
}
