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

trait HbusCl2C8B8TransactionLibrary extends TransactionLibrary {
  self: HbusCl2C8B8Platform with HbusCl2C8B8Software =>

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

  val tCl0C1_L1_ld: Transaction = Transaction(app_Cl0C1 read Cl0.C1_L1)
  tCl0C1_L1_ld.used

  val tCl0C1_L1_st: Transaction = Transaction(app_Cl0C1 write Cl0.C1_L1)
  tCl0C1_L1_st.used

  val tCl0C1_L2_ld: Transaction = Transaction(app_Cl0C1 read Cl0.L2)
  tCl0C1_L2_ld.used

  val tCl0C1_L2_st: Transaction = Transaction(app_Cl0C1 write Cl0.L2)
  tCl0C1_L2_st.used

  val tCl0C1_BK1_ld: Transaction = Transaction(app_Cl0C1 read BK1)
  tCl0C1_BK1_ld.used

  val tCl0C1_BK1_st: Transaction = Transaction(app_Cl0C1 write BK1)
  tCl0C1_BK1_st.used

  val tCl0C2_L1_ld: Transaction = Transaction(app_Cl0C2 read Cl0.C2_L1)
  tCl0C2_L1_ld.used

  val tCl0C2_L1_st: Transaction = Transaction(app_Cl0C2 write Cl0.C2_L1)
  tCl0C2_L1_st.used

  val tCl0C2_L2_ld: Transaction = Transaction(app_Cl0C2 read Cl0.L2)
  tCl0C2_L2_ld.used

  val tCl0C2_L2_st: Transaction = Transaction(app_Cl0C2 write Cl0.L2)
  tCl0C2_L2_st.used

  val tCl0C2_BK2_ld: Transaction = Transaction(app_Cl0C2 read BK2)
  tCl0C2_BK2_ld.used

  val tCl0C2_BK2_st: Transaction = Transaction(app_Cl0C2 write BK2)
  tCl0C2_BK2_st.used

  val tCl0C3_L1_ld: Transaction = Transaction(app_Cl0C3 read Cl0.C3_L1)
  tCl0C3_L1_ld.used

  val tCl0C3_L1_st: Transaction = Transaction(app_Cl0C3 write Cl0.C3_L1)
  tCl0C3_L1_st.used

  val tCl0C3_L2_ld: Transaction = Transaction(app_Cl0C3 read Cl0.L2)
  tCl0C3_L2_ld.used

  val tCl0C3_L2_st: Transaction = Transaction(app_Cl0C3 write Cl0.L2)
  tCl0C3_L2_st.used

  val tCl0C3_BK3_ld: Transaction = Transaction(app_Cl0C3 read BK3)
  tCl0C3_BK3_ld.used

  val tCl0C3_BK3_st: Transaction = Transaction(app_Cl0C3 write BK3)
  tCl0C3_BK3_st.used

  val tCl0C4_L1_ld: Transaction = Transaction(app_Cl0C4 read Cl0.C4_L1)
  tCl0C4_L1_ld.used

  val tCl0C4_L1_st: Transaction = Transaction(app_Cl0C4 write Cl0.C4_L1)
  tCl0C4_L1_st.used

  val tCl0C4_L2_ld: Transaction = Transaction(app_Cl0C4 read Cl0.L2)
  tCl0C4_L2_ld.used

  val tCl0C4_L2_st: Transaction = Transaction(app_Cl0C4 write Cl0.L2)
  tCl0C4_L2_st.used

  val tCl0C4_BK0_ld: Transaction = Transaction(app_Cl0C4 read BK0)
  tCl0C4_BK0_ld.used

  val tCl0C4_BK0_st: Transaction = Transaction(app_Cl0C4 write BK0)
  tCl0C4_BK0_st.used

  val tCl0C5_L1_ld: Transaction = Transaction(app_Cl0C5 read Cl0.C5_L1)
  tCl0C5_L1_ld.used

  val tCl0C5_L1_st: Transaction = Transaction(app_Cl0C5 write Cl0.C5_L1)
  tCl0C5_L1_st.used

  val tCl0C5_L2_ld: Transaction = Transaction(app_Cl0C5 read Cl0.L2)
  tCl0C5_L2_ld.used

  val tCl0C5_L2_st: Transaction = Transaction(app_Cl0C5 write Cl0.L2)
  tCl0C5_L2_st.used

  val tCl0C5_BK1_ld: Transaction = Transaction(app_Cl0C5 read BK1)
  tCl0C5_BK1_ld.used

  val tCl0C5_BK1_st: Transaction = Transaction(app_Cl0C5 write BK1)
  tCl0C5_BK1_st.used

  val tCl0C6_L1_ld: Transaction = Transaction(app_Cl0C6 read Cl0.C6_L1)
  tCl0C6_L1_ld.used

  val tCl0C6_L1_st: Transaction = Transaction(app_Cl0C6 write Cl0.C6_L1)
  tCl0C6_L1_st.used

  val tCl0C6_L2_ld: Transaction = Transaction(app_Cl0C6 read Cl0.L2)
  tCl0C6_L2_ld.used

  val tCl0C6_L2_st: Transaction = Transaction(app_Cl0C6 write Cl0.L2)
  tCl0C6_L2_st.used

  val tCl0C6_BK2_ld: Transaction = Transaction(app_Cl0C6 read BK2)
  tCl0C6_BK2_ld.used

  val tCl0C6_BK2_st: Transaction = Transaction(app_Cl0C6 write BK2)
  tCl0C6_BK2_st.used

  val tCl0C7_L1_ld: Transaction = Transaction(app_Cl0C7 read Cl0.C7_L1)
  tCl0C7_L1_ld.used

  val tCl0C7_L1_st: Transaction = Transaction(app_Cl0C7 write Cl0.C7_L1)
  tCl0C7_L1_st.used

  val tCl0C7_L2_ld: Transaction = Transaction(app_Cl0C7 read Cl0.L2)
  tCl0C7_L2_ld.used

  val tCl0C7_L2_st: Transaction = Transaction(app_Cl0C7 write Cl0.L2)
  tCl0C7_L2_st.used

  val tCl0C7_BK3_ld: Transaction = Transaction(app_Cl0C7 read BK3)
  tCl0C7_BK3_ld.used

  val tCl0C7_BK3_st: Transaction = Transaction(app_Cl0C7 write BK3)
  tCl0C7_BK3_st.used

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

  val tCl1C1_L1_ld: Transaction = Transaction(app_Cl1C1 read Cl1.C1_L1)
  tCl1C1_L1_ld.used

  val tCl1C1_L1_st: Transaction = Transaction(app_Cl1C1 write Cl1.C1_L1)
  tCl1C1_L1_st.used

  val tCl1C1_L2_ld: Transaction = Transaction(app_Cl1C1 read Cl1.L2)
  tCl1C1_L2_ld.used

  val tCl1C1_L2_st: Transaction = Transaction(app_Cl1C1 write Cl1.L2)
  tCl1C1_L2_st.used

  val tCl1C1_BK5_ld: Transaction = Transaction(app_Cl1C1 read BK5)
  tCl1C1_BK5_ld.used

  val tCl1C1_BK5_st: Transaction = Transaction(app_Cl1C1 write BK5)
  tCl1C1_BK5_st.used

  val tCl1C2_L1_ld: Transaction = Transaction(app_Cl1C2 read Cl1.C2_L1)
  tCl1C2_L1_ld.used

  val tCl1C2_L1_st: Transaction = Transaction(app_Cl1C2 write Cl1.C2_L1)
  tCl1C2_L1_st.used

  val tCl1C2_L2_ld: Transaction = Transaction(app_Cl1C2 read Cl1.L2)
  tCl1C2_L2_ld.used

  val tCl1C2_L2_st: Transaction = Transaction(app_Cl1C2 write Cl1.L2)
  tCl1C2_L2_st.used

  val tCl1C2_BK6_ld: Transaction = Transaction(app_Cl1C2 read BK6)
  tCl1C2_BK6_ld.used

  val tCl1C2_BK6_st: Transaction = Transaction(app_Cl1C2 write BK6)
  tCl1C2_BK6_st.used

  val tCl1C3_L1_ld: Transaction = Transaction(app_Cl1C3 read Cl1.C3_L1)
  tCl1C3_L1_ld.used

  val tCl1C3_L1_st: Transaction = Transaction(app_Cl1C3 write Cl1.C3_L1)
  tCl1C3_L1_st.used

  val tCl1C3_L2_ld: Transaction = Transaction(app_Cl1C3 read Cl1.L2)
  tCl1C3_L2_ld.used

  val tCl1C3_L2_st: Transaction = Transaction(app_Cl1C3 write Cl1.L2)
  tCl1C3_L2_st.used

  val tCl1C3_BK7_ld: Transaction = Transaction(app_Cl1C3 read BK7)
  tCl1C3_BK7_ld.used

  val tCl1C3_BK7_st: Transaction = Transaction(app_Cl1C3 write BK7)
  tCl1C3_BK7_st.used

  val tCl1C4_L1_ld: Transaction = Transaction(app_Cl1C4 read Cl1.C4_L1)
  tCl1C4_L1_ld.used

  val tCl1C4_L1_st: Transaction = Transaction(app_Cl1C4 write Cl1.C4_L1)
  tCl1C4_L1_st.used

  val tCl1C4_L2_ld: Transaction = Transaction(app_Cl1C4 read Cl1.L2)
  tCl1C4_L2_ld.used

  val tCl1C4_L2_st: Transaction = Transaction(app_Cl1C4 write Cl1.L2)
  tCl1C4_L2_st.used

  val tCl1C4_BK4_ld: Transaction = Transaction(app_Cl1C4 read BK4)
  tCl1C4_BK4_ld.used

  val tCl1C4_BK4_st: Transaction = Transaction(app_Cl1C4 write BK4)
  tCl1C4_BK4_st.used

  val tCl1C5_L1_ld: Transaction = Transaction(app_Cl1C5 read Cl1.C5_L1)
  tCl1C5_L1_ld.used

  val tCl1C5_L1_st: Transaction = Transaction(app_Cl1C5 write Cl1.C5_L1)
  tCl1C5_L1_st.used

  val tCl1C5_L2_ld: Transaction = Transaction(app_Cl1C5 read Cl1.L2)
  tCl1C5_L2_ld.used

  val tCl1C5_L2_st: Transaction = Transaction(app_Cl1C5 write Cl1.L2)
  tCl1C5_L2_st.used

  val tCl1C5_BK5_ld: Transaction = Transaction(app_Cl1C5 read BK5)
  tCl1C5_BK5_ld.used

  val tCl1C5_BK5_st: Transaction = Transaction(app_Cl1C5 write BK5)
  tCl1C5_BK5_st.used

  val tCl1C6_L1_ld: Transaction = Transaction(app_Cl1C6 read Cl1.C6_L1)
  tCl1C6_L1_ld.used

  val tCl1C6_L1_st: Transaction = Transaction(app_Cl1C6 write Cl1.C6_L1)
  tCl1C6_L1_st.used

  val tCl1C6_L2_ld: Transaction = Transaction(app_Cl1C6 read Cl1.L2)
  tCl1C6_L2_ld.used

  val tCl1C6_L2_st: Transaction = Transaction(app_Cl1C6 write Cl1.L2)
  tCl1C6_L2_st.used

  val tCl1C6_BK6_ld: Transaction = Transaction(app_Cl1C6 read BK6)
  tCl1C6_BK6_ld.used

  val tCl1C6_BK6_st: Transaction = Transaction(app_Cl1C6 write BK6)
  tCl1C6_BK6_st.used

  val tCl1C7_L1_ld: Transaction = Transaction(app_Cl1C7 read Cl1.C7_L1)
  tCl1C7_L1_ld.used

  val tCl1C7_L1_st: Transaction = Transaction(app_Cl1C7 write Cl1.C7_L1)
  tCl1C7_L1_st.used

  val tCl1C7_L2_ld: Transaction = Transaction(app_Cl1C7 read Cl1.L2)
  tCl1C7_L2_ld.used

  val tCl1C7_L2_st: Transaction = Transaction(app_Cl1C7 write Cl1.L2)
  tCl1C7_L2_st.used

  val tCl1C7_BK7_ld: Transaction = Transaction(app_Cl1C7 read BK7)
  tCl1C7_BK7_ld.used

  val tCl1C7_BK7_st: Transaction = Transaction(app_Cl1C7 write BK7)
  tCl1C7_BK7_st.used

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

  val tDMA_BK2_ETH: Scenario = Scenario(app_dma read BK2, app_dma write ETH)
  tDMA_BK2_ETH.used

  val tDMA_BK4_ETH: Scenario = Scenario(app_dma write BK4, app_dma read ETH)
  tDMA_BK4_ETH.used

  val tDMA_BK6_ETH: Scenario = Scenario(app_dma write BK6, app_dma read ETH)
  tDMA_BK6_ETH.used

}
