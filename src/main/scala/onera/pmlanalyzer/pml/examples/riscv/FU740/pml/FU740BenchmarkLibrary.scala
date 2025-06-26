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

  val Hart_0_Load_L1D$ : Scenario = Scenario(t0_0.asInstanceOf[ScenarioLike])
  val Hart_0_Store_L1D$ : Scenario = Scenario(t0_1)
  val Hart_0_Load_L2$ : Scenario = Scenario(t0_2)
  val Hart_0_Store_L2$ : Scenario = Scenario(t0_3)
  val Hart_0_Load_DDR_b0: Scenario = Scenario(t0_4)
  val Hart_0_Store_DDR_b0: Scenario = Scenario(t0_5)

  val Hart_1_Load_L1D$ : Scenario = Scenario(t1_0)
  val Hart_1_Store_L1D$ : Scenario = Scenario(t1_1)
  val Hart_1_Load_LIM: Scenario = Scenario(t1_4)
  val Hart_1_Store_LIM: Scenario = Scenario(t1_5)
  val Hart_1_Load_L2$ : Scenario = Scenario(t1_2)
  val Hart_1_Store_L2$ : Scenario = Scenario(t1_3)
  val Hart_1_Load_DDR_b0: Scenario = Scenario(t1_6)
  val Hart_1_Store_DDR_b0: Scenario = Scenario(t1_7)

  val Hart_2_Load_L1D$ : Scenario = Scenario(t2_0)
  val Hart_2_Store_L1D$ : Scenario = Scenario(t2_1)
  val Hart_2_Load_L2$ : Scenario = Scenario(t2_2)
  val Hart_2_Store_L2$ : Scenario = Scenario(t2_3)
  val Hart_2_Load_DDR_b0: Scenario = Scenario(t2_4)
  val Hart_2_Store_DDR_b0: Scenario = Scenario(t2_5)

  val Hart_3_Load_L1D$ : Scenario = Scenario(t3_0)
  val Hart_3_Store_L1D$ : Scenario = Scenario(t3_1)
  val Hart_3_Load_L2$ : Scenario = Scenario(t3_2)
  val Hart_3_Store_L2$ : Scenario = Scenario(t3_3)
  val Hart_3_Load_DDR_b0: Scenario = Scenario(t3_4)
  val Hart_3_Store_DDR_b0: Scenario = Scenario(t3_5)

  val Hart_4_Load_L1D$ : Scenario = Scenario(t4_0)
  val Hart_4_Store_L1D$ : Scenario = Scenario(t4_1)
  val Hart_4_Load_L2$ : Scenario = Scenario(t4_2)
  val Hart_4_Store_L2$ : Scenario = Scenario(t4_3)
  val Hart_4_Load_DDR_b0: Scenario = Scenario(t4_4)
  val Hart_4_Store_DDR_b0: Scenario = Scenario(t4_5)

  val benchTransactions: Seq[Scenario] = Seq(
    Hart_0_Load_L1D$,
    Hart_0_Store_L1D$,
    Hart_0_Load_L2$,
    Hart_0_Store_L2$,
    Hart_0_Load_DDR_b0,
    Hart_0_Store_DDR_b0,
    Hart_1_Load_L1D$,
    Hart_1_Store_L1D$,
    Hart_1_Load_LIM,
    Hart_1_Store_LIM,
    Hart_1_Load_L2$,
    Hart_1_Store_L2$,
    Hart_1_Store_DDR_b0,
    Hart_1_Load_DDR_b0,
    Hart_2_Load_L1D$,
    Hart_2_Store_L1D$,
    Hart_2_Load_L2$,
    Hart_2_Load_DDR_b0,
    Hart_2_Store_L2$,
    Hart_3_Load_L1D$,
    Hart_3_Store_L1D$,
    Hart_3_Load_L2$,
    Hart_3_Store_L2$,
    Hart_2_Store_DDR_b0,
    Hart_3_Load_DDR_b0,
    Hart_3_Store_DDR_b0,
    Hart_4_Load_L1D$,
    Hart_4_Store_L1D$,
    Hart_4_Load_L2$,
    Hart_4_Store_L2$,
    Hart_4_Load_DDR_b0,
    Hart_4_Store_DDR_b0
  )

}
