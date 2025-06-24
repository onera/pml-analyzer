/*******************************************************************************
 * Copyright (c)  2025. ONERA
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

package onera.pmlanalyzer.pml.examples.riscv.FU740.pml

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.{
  UserScenarioId,
  UserTransactionId
}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait FU740BenchmarkLibrary extends FU740TransactionLibrary {
  self: FU740Platform with FU740SoftwareAllocation =>

  /* Expected from trace:
   * Hart_0_Load_L1D$
   * Hart_0_Store_L1D$
   * Hart_0_Load_L2$
   * Hart_0_Store_L2$
   * Hart_0_Load_DDR_b0
   * Hart_0_Store_DDR_b0
   * Hart_1_Load_L1D$
   * Hart_1_Store_L1D$
   * Hart_1_Load_LIM
   * Hart_1_Store_LIM
   * Hart_1_Load_L2$
   * Hart_1_Store_L2$
   * Hart_1_Store_DDR_b0
   * Hart_1_Load_DDR_b0
   * Hart_2_Load_L1D$
   * Hart_2_Store_L1D$
   * Hart_2_Load_L2$
   * Hart_2_Load_DDR_b0
   * Hart_2_Store_L2$
   * Hart_3_Load_L1D$
   * Hart_3_Store_L1D$
   * Hart_3_Load_L2$
   * Hart_3_Store_L2$
   * Hart_2_Store_DDR_b0
   * Hart_3_Load_DDR_b0
   * Hart_3_Store_DDR_b0
   * Hart_4_Load_L1D$
   * Hart_4_Store_L1D$
   * Hart_4_Load_L2$
   * Hart_4_Store_L2$
   * Hart_4_Load_DDR_b0
   * Hart_4_Store_DDR_b0
   */

  // TODO Alfonso, Check if this is what L1D$ means for C0
  val c0_ld_l1: Transaction = Issue47( "Hart_0_Load_L1D$", t0_0)
  val c0_wr_l1: Transaction = Issue47( "Hart_0_Store_L1D$", t0_1)
//  val c0_ld_l1: Scenario = Issue47( "Hart_0_Load_L1D$", t0_0)
//  val c0_wr_l1: Scenario = Issue47( "Hart_0_Store_L1D$", t0_1)
//  val c0_ld_l2: Scenario = Issue47("Hart_0_Load_L2$", t0_2)
//  val c0_wr_l2: Scenario = Issue47("Hart_0_Store_L2$", t0_3)
//  val c0_ld_m: Scenario = Issue47("Hart_0_Load_DDR_b0", t0_4)
//  val c0_wr_m: Scenario = Issue47("Hart_0_Store_DDR_b0", t0_5)

  val c1_ld_l1: Transaction = Issue47("Hart_1_Load_L1D$", t1_0)
  val c1_wr_l1: Transaction = Issue47("Hart_1_Store_L1D$", t1_1)
//  val c1_ld_l1: Scenario = Issue47("Hart_1_Load_L1D$", t1_0)
//  val c1_wr_l1: Scenario = Issue47("Hart_1_Store_L1D$", t1_1)
//  val c1_ld_lm: Scenario = Issue47("Hart_1_Load_LIM", t1_4)
//  val c1_wr_lm: Scenario = Issue47("Hart_1_Store_LIM", t1_5)
//  val c1_ld_l2: Scenario = Issue47("Hart_1_Load_L2$", t1_2)
//  val c1_wr_l2: Scenario = Issue47("Hart_1_Store_L2$", t1_3)
//  val c1_ld_m: Scenario = Issue47("Hart_1_Load_DDR_b0", t1_6)
//  val c1_wr_m: Scenario = Issue47("Hart_1_Store_DDR_b0", t1_7)

  val c2_ld_l1: Transaction = Issue47("Hart_2_Load_L1D$", t2_0)
  val c2_wr_l1: Transaction = Issue47("Hart_2_Store_L1D$", t2_1)
//  val c2_ld_l1: Scenario = Issue47("Hart_2_Load_L1D$", t2_0)
//  val c2_wr_l1: Scenario = Issue47("Hart_2_Store_L1D$", t2_1)
//  val c2_ld_l2: Scenario = Issue47("Hart_2_Load_L2$", t2_2)
//  val c2_wr_l2: Scenario = Issue47("Hart_2_Store_L2$", t2_3)
//  val c2_ld_m: Scenario = Issue47("Hart_2_Load_DDR_b0", t2_4)
//  val c2_wr_m: Scenario = Issue47("Hart_2_Store_DDR_b0", t2_5)

  val c3_ld_l1: Transaction = Issue47("Hart_3_Load_L1D$", t3_0)
  val c3_wr_l1: Transaction = Issue47("Hart_3_Store_L1D$", t3_1)
//  val c3_ld_l1: Scenario = Issue47("Hart_3_Load_L1D$", t3_0)
//  val c3_wr_l1: Scenario = Issue47("Hart_3_Store_L1D$", t3_1)
//  val c3_ld_l2: Scenario = Issue47("Hart_3_Load_L2$", t3_2)
//  val c3_wr_l2: Scenario = Issue47("Hart_3_Store_L2$", t3_3)
//  val c3_ld_m: Scenario = Issue47("Hart_3_Load_DDR_b0", t3_4)
//  val c3_wr_m: Scenario = Issue47("Hart_3_Store_DDR_b0", t3_5)

  val c4_ld_l1: Transaction = Issue47("Hart_4_Load_L1D$", t4_0)
  val c4_wr_l1: Transaction = Issue47("Hart_4_Store_L1D$", t4_1)
//  val c4_ld_l1: Scenario = Issue47("Hart_4_Load_L1D$", t4_0)
//  val c4_wr_l1: Scenario = Issue47("Hart_4_Store_L1D$", t4_1)
//  val c4_ld_l2: Scenario = Issue47("Hart_4_Load_L2$", t4_2)
//  val c4_wr_l2: Scenario = Issue47("Hart_4_Store_L2$", t4_3)
//  val c4_ld_m: Scenario = Issue47("Hart_4_Load_DDR_b0", t4_4)
//  val c4_wr_m: Scenario = Issue47("Hart_4_Store_DDR_b0", t4_5)

  val benchTransactions: Seq[Transaction] = Seq(
//    c0_ld_l1,
    c0_wr_l1,
//    c0_ld_l2,
//    c0_wr_l2,
//    c0_ld_m,
//    c0_wr_m,
//    c1_ld_l1,
    c1_wr_l1,
//    c1_ld_lm,
//    c1_wr_lm,
//    c1_ld_l2,
//    c1_wr_l2,
//    c1_ld_m,
//    c1_wr_m,
//    c2_ld_l1,
    c2_wr_l1,
//    c2_ld_l2,
//    c2_wr_l2,
//    c2_ld_m,
//    c2_wr_m,
//    c3_ld_l1,
    c3_wr_l1,
//    c3_ld_l2,
//    c3_wr_l2,
//    c3_ld_m,
//    c3_wr_m,
//    c4_ld_l1,
    c4_wr_l1,
//    c4_ld_l2,
//    c4_wr_l2,
//    c4_ld_m,
//    c4_wr_m,
  )

}
