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

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait GeneratedTransactionLibrary extends TransactionLibrary {
  self: GeneratedPlatform with GeneratedSoftwareAllocation =>

  val tCl0C0_L1_ld: Transaction = Transaction(app_Cl0C0 read Cl0.C0_L1)
  tCl0C0_L1_ld.used

  val tCl0C0_L1_st: Transaction = Transaction(app_Cl0C0 write Cl0.C0_L1)
  tCl0C0_L1_st.used

  val tCl0C0_L2_ld: Transaction = Transaction(app_Cl0C0 read Cl0.L2)
  tCl0C0_L2_ld.used

  val tCl0C0_L2_st: Transaction = Transaction(app_Cl0C0 write Cl0.L2)
  tCl0C0_L2_st.used

  val tCl0C0_BK0_ld: Transaction = Transaction(app_Cl0C0 read BK0)
  tCl0C0_BK0_ld.used

  val tCl0C0_BK0_st: Transaction = Transaction(app_Cl0C0 write BK0)
  tCl0C0_BK0_st.used

  val tCl0C0_BK1_ld: Transaction = Transaction(app_Cl0C0 read BK1)
  tCl0C0_BK1_ld.used

  val tCl0C0_BK1_st: Transaction = Transaction(app_Cl0C0 write BK1)
  tCl0C0_BK1_st.used

  val tCl0C1_L1_ld: Transaction = Transaction(app_Cl0C1 read Cl0.C1_L1)
  tCl0C1_L1_ld.used

  val tCl0C1_L1_st: Transaction = Transaction(app_Cl0C1 write Cl0.C1_L1)
  tCl0C1_L1_st.used

  val tCl0C1_L2_ld: Transaction = Transaction(app_Cl0C1 read Cl0.L2)
  tCl0C1_L2_ld.used

  val tCl0C1_L2_st: Transaction = Transaction(app_Cl0C1 write Cl0.L2)
  tCl0C1_L2_st.used

  val tCl0C1_BK2_ld: Transaction = Transaction(app_Cl0C1 read BK2)
  tCl0C1_BK2_ld.used

  val tCl0C1_BK2_st: Transaction = Transaction(app_Cl0C1 write BK2)
  tCl0C1_BK2_st.used

  val tCl0C1_BK3_ld: Transaction = Transaction(app_Cl0C1 read BK3)
  tCl0C1_BK3_ld.used

  val tCl0C1_BK3_st: Transaction = Transaction(app_Cl0C1 write BK3)
  tCl0C1_BK3_st.used

  val tCl1C0_L1_ld: Transaction = Transaction(app_Cl1C0 read Cl1.C0_L1)
  tCl1C0_L1_ld.used

  val tCl1C0_L1_st: Transaction = Transaction(app_Cl1C0 write Cl1.C0_L1)
  tCl1C0_L1_st.used

  val tCl1C0_L2_ld: Transaction = Transaction(app_Cl1C0 read Cl1.L2)
  tCl1C0_L2_ld.used

  val tCl1C0_L2_st: Transaction = Transaction(app_Cl1C0 write Cl1.L2)
  tCl1C0_L2_st.used

  val tCl1C0_BK4_ld: Transaction = Transaction(app_Cl1C0 read BK4)
  tCl1C0_BK4_ld.used

  val tCl1C0_BK4_st: Transaction = Transaction(app_Cl1C0 write BK4)
  tCl1C0_BK4_st.used

  val tCl1C0_BK5_ld: Transaction = Transaction(app_Cl1C0 read BK5)
  tCl1C0_BK5_ld.used

  val tCl1C0_BK5_st: Transaction = Transaction(app_Cl1C0 write BK5)
  tCl1C0_BK5_st.used

  val tCl1C1_L1_ld: Transaction = Transaction(app_Cl1C1 read Cl1.C1_L1)
  tCl1C1_L1_ld.used

  val tCl1C1_L1_st: Transaction = Transaction(app_Cl1C1 write Cl1.C1_L1)
  tCl1C1_L1_st.used

  val tCl1C1_L2_ld: Transaction = Transaction(app_Cl1C1 read Cl1.L2)
  tCl1C1_L2_ld.used

  val tCl1C1_L2_st: Transaction = Transaction(app_Cl1C1 write Cl1.L2)
  tCl1C1_L2_st.used

  val tCl1C1_BK6_ld: Transaction = Transaction(app_Cl1C1 read BK6)
  tCl1C1_BK6_ld.used

  val tCl1C1_BK6_st: Transaction = Transaction(app_Cl1C1 write BK6)
  tCl1C1_BK6_st.used

  val tCl1C1_BK7_ld: Transaction = Transaction(app_Cl1C1 read BK7)
  tCl1C1_BK7_ld.used

  val tCl1C1_BK7_st: Transaction = Transaction(app_Cl1C1 write BK7)
  tCl1C1_BK7_st.used

  val tC0_DMAReg_ld: Transaction = Transaction(app_Cl0C0 read DMAReg)
  tC0_DMAReg_ld.used

  val tC0_DMAReg_st: Transaction = Transaction(app_Cl0C0 write DMAReg)
  tC0_DMAReg_st.used

  val tC0_BK2_ld: Transaction = Transaction(app_Cl0C0 read BK2)
  tC0_BK2_ld.used

  val tC0_BK2_st: Transaction = Transaction(app_Cl0C0 write BK2)
  tC0_BK2_st.used

  val tC0_BK4_ld: Transaction = Transaction(app_Cl0C0 read BK4)
  tC0_BK4_ld.used

  val tC0_BK4_st: Transaction = Transaction(app_Cl0C0 write BK4)
  tC0_BK4_st.used

  val tC0_BK6_ld: Transaction = Transaction(app_Cl0C0 read BK6)
  tC0_BK6_ld.used

  val tC0_BK6_st: Transaction = Transaction(app_Cl0C0 write BK6)
  tC0_BK6_st.used

  val app_dma_r_bk2: Transaction = Transaction(app_dma read BK2)
  app_dma_r_bk2.used

  val tDMA_BK2_ETH: Scenario = Scenario(app_dma read BK2, app_dma write ETH)
  tDMA_BK2_ETH.used

  val tDMA_BK4_ETH: Scenario = Scenario(app_dma write BK4, app_dma read ETH)
  tDMA_BK4_ETH.used

  val tDMA_BK6_ETH: Scenario = Scenario(app_dma write BK6, app_dma read ETH)
  tDMA_BK6_ETH.used

}
