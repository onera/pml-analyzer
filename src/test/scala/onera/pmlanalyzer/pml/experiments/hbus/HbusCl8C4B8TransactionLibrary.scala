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

trait HbusCl8C4B8TransactionLibrary extends TransactionLibrary {
  self: HbusCl8C4B8Platform with HbusCl8C4B8Software =>

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

  val tCl0C1_BK0_ld: Transaction = Transaction(app_Cl0C1 read BK0)
  tCl0C1_BK0_ld.used

  val tCl0C1_BK0_st: Transaction = Transaction(app_Cl0C1 write BK0)
  tCl0C1_BK0_st.used

  val tCl0C2_L1_ld: Transaction = Transaction(app_Cl0C2 read Cl0.C2_L1)
  tCl0C2_L1_ld.used

  val tCl0C2_L1_st: Transaction = Transaction(app_Cl0C2 write Cl0.C2_L1)
  tCl0C2_L1_st.used

  val tCl0C2_L2_ld: Transaction = Transaction(app_Cl0C2 read Cl0.L2)
  tCl0C2_L2_ld.used

  val tCl0C2_L2_st: Transaction = Transaction(app_Cl0C2 write Cl0.L2)
  tCl0C2_L2_st.used

  val tCl0C2_BK0_ld: Transaction = Transaction(app_Cl0C2 read BK0)
  tCl0C2_BK0_ld.used

  val tCl0C2_BK0_st: Transaction = Transaction(app_Cl0C2 write BK0)
  tCl0C2_BK0_st.used

  val tCl0C3_L1_ld: Transaction = Transaction(app_Cl0C3 read Cl0.C3_L1)
  tCl0C3_L1_ld.used

  val tCl0C3_L1_st: Transaction = Transaction(app_Cl0C3 write Cl0.C3_L1)
  tCl0C3_L1_st.used

  val tCl0C3_L2_ld: Transaction = Transaction(app_Cl0C3 read Cl0.L2)
  tCl0C3_L2_ld.used

  val tCl0C3_L2_st: Transaction = Transaction(app_Cl0C3 write Cl0.L2)
  tCl0C3_L2_st.used

  val tCl0C3_BK0_ld: Transaction = Transaction(app_Cl0C3 read BK0)
  tCl0C3_BK0_ld.used

  val tCl0C3_BK0_st: Transaction = Transaction(app_Cl0C3 write BK0)
  tCl0C3_BK0_st.used

  val tCl1C0_L1_ld: Transaction = Transaction(app_Cl1C0 read Cl1.C0_L1)
  tCl1C0_L1_ld.used

  val tCl1C0_L1_st: Transaction = Transaction(app_Cl1C0 write Cl1.C0_L1)
  tCl1C0_L1_st.used

  val tCl1C0_L2_ld: Transaction = Transaction(app_Cl1C0 read Cl1.L2)
  tCl1C0_L2_ld.used

  val tCl1C0_L2_st: Transaction = Transaction(app_Cl1C0 write Cl1.L2)
  tCl1C0_L2_st.used

  val tCl1C0_BK1_ld: Transaction = Transaction(app_Cl1C0 read BK1)
  tCl1C0_BK1_ld.used

  val tCl1C0_BK1_st: Transaction = Transaction(app_Cl1C0 write BK1)
  tCl1C0_BK1_st.used

  val tCl1C1_L1_ld: Transaction = Transaction(app_Cl1C1 read Cl1.C1_L1)
  tCl1C1_L1_ld.used

  val tCl1C1_L1_st: Transaction = Transaction(app_Cl1C1 write Cl1.C1_L1)
  tCl1C1_L1_st.used

  val tCl1C1_L2_ld: Transaction = Transaction(app_Cl1C1 read Cl1.L2)
  tCl1C1_L2_ld.used

  val tCl1C1_L2_st: Transaction = Transaction(app_Cl1C1 write Cl1.L2)
  tCl1C1_L2_st.used

  val tCl1C1_BK1_ld: Transaction = Transaction(app_Cl1C1 read BK1)
  tCl1C1_BK1_ld.used

  val tCl1C1_BK1_st: Transaction = Transaction(app_Cl1C1 write BK1)
  tCl1C1_BK1_st.used

  val tCl1C2_L1_ld: Transaction = Transaction(app_Cl1C2 read Cl1.C2_L1)
  tCl1C2_L1_ld.used

  val tCl1C2_L1_st: Transaction = Transaction(app_Cl1C2 write Cl1.C2_L1)
  tCl1C2_L1_st.used

  val tCl1C2_L2_ld: Transaction = Transaction(app_Cl1C2 read Cl1.L2)
  tCl1C2_L2_ld.used

  val tCl1C2_L2_st: Transaction = Transaction(app_Cl1C2 write Cl1.L2)
  tCl1C2_L2_st.used

  val tCl1C2_BK1_ld: Transaction = Transaction(app_Cl1C2 read BK1)
  tCl1C2_BK1_ld.used

  val tCl1C2_BK1_st: Transaction = Transaction(app_Cl1C2 write BK1)
  tCl1C2_BK1_st.used

  val tCl1C3_L1_ld: Transaction = Transaction(app_Cl1C3 read Cl1.C3_L1)
  tCl1C3_L1_ld.used

  val tCl1C3_L1_st: Transaction = Transaction(app_Cl1C3 write Cl1.C3_L1)
  tCl1C3_L1_st.used

  val tCl1C3_L2_ld: Transaction = Transaction(app_Cl1C3 read Cl1.L2)
  tCl1C3_L2_ld.used

  val tCl1C3_L2_st: Transaction = Transaction(app_Cl1C3 write Cl1.L2)
  tCl1C3_L2_st.used

  val tCl1C3_BK1_ld: Transaction = Transaction(app_Cl1C3 read BK1)
  tCl1C3_BK1_ld.used

  val tCl1C3_BK1_st: Transaction = Transaction(app_Cl1C3 write BK1)
  tCl1C3_BK1_st.used

  val tCl2C0_L1_ld: Transaction = Transaction(app_Cl2C0 read Cl2.C0_L1)
  tCl2C0_L1_ld.used

  val tCl2C0_L1_st: Transaction = Transaction(app_Cl2C0 write Cl2.C0_L1)
  tCl2C0_L1_st.used

  val tCl2C0_L2_ld: Transaction = Transaction(app_Cl2C0 read Cl2.L2)
  tCl2C0_L2_ld.used

  val tCl2C0_L2_st: Transaction = Transaction(app_Cl2C0 write Cl2.L2)
  tCl2C0_L2_st.used

  val tCl2C0_BK2_ld: Transaction = Transaction(app_Cl2C0 read BK2)
  tCl2C0_BK2_ld.used

  val tCl2C0_BK2_st: Transaction = Transaction(app_Cl2C0 write BK2)
  tCl2C0_BK2_st.used

  val tCl2C1_L1_ld: Transaction = Transaction(app_Cl2C1 read Cl2.C1_L1)
  tCl2C1_L1_ld.used

  val tCl2C1_L1_st: Transaction = Transaction(app_Cl2C1 write Cl2.C1_L1)
  tCl2C1_L1_st.used

  val tCl2C1_L2_ld: Transaction = Transaction(app_Cl2C1 read Cl2.L2)
  tCl2C1_L2_ld.used

  val tCl2C1_L2_st: Transaction = Transaction(app_Cl2C1 write Cl2.L2)
  tCl2C1_L2_st.used

  val tCl2C1_BK2_ld: Transaction = Transaction(app_Cl2C1 read BK2)
  tCl2C1_BK2_ld.used

  val tCl2C1_BK2_st: Transaction = Transaction(app_Cl2C1 write BK2)
  tCl2C1_BK2_st.used

  val tCl2C2_L1_ld: Transaction = Transaction(app_Cl2C2 read Cl2.C2_L1)
  tCl2C2_L1_ld.used

  val tCl2C2_L1_st: Transaction = Transaction(app_Cl2C2 write Cl2.C2_L1)
  tCl2C2_L1_st.used

  val tCl2C2_L2_ld: Transaction = Transaction(app_Cl2C2 read Cl2.L2)
  tCl2C2_L2_ld.used

  val tCl2C2_L2_st: Transaction = Transaction(app_Cl2C2 write Cl2.L2)
  tCl2C2_L2_st.used

  val tCl2C2_BK2_ld: Transaction = Transaction(app_Cl2C2 read BK2)
  tCl2C2_BK2_ld.used

  val tCl2C2_BK2_st: Transaction = Transaction(app_Cl2C2 write BK2)
  tCl2C2_BK2_st.used

  val tCl2C3_L1_ld: Transaction = Transaction(app_Cl2C3 read Cl2.C3_L1)
  tCl2C3_L1_ld.used

  val tCl2C3_L1_st: Transaction = Transaction(app_Cl2C3 write Cl2.C3_L1)
  tCl2C3_L1_st.used

  val tCl2C3_L2_ld: Transaction = Transaction(app_Cl2C3 read Cl2.L2)
  tCl2C3_L2_ld.used

  val tCl2C3_L2_st: Transaction = Transaction(app_Cl2C3 write Cl2.L2)
  tCl2C3_L2_st.used

  val tCl2C3_BK2_ld: Transaction = Transaction(app_Cl2C3 read BK2)
  tCl2C3_BK2_ld.used

  val tCl2C3_BK2_st: Transaction = Transaction(app_Cl2C3 write BK2)
  tCl2C3_BK2_st.used

  val tCl3C0_L1_ld: Transaction = Transaction(app_Cl3C0 read Cl3.C0_L1)
  tCl3C0_L1_ld.used

  val tCl3C0_L1_st: Transaction = Transaction(app_Cl3C0 write Cl3.C0_L1)
  tCl3C0_L1_st.used

  val tCl3C0_L2_ld: Transaction = Transaction(app_Cl3C0 read Cl3.L2)
  tCl3C0_L2_ld.used

  val tCl3C0_L2_st: Transaction = Transaction(app_Cl3C0 write Cl3.L2)
  tCl3C0_L2_st.used

  val tCl3C0_BK3_ld: Transaction = Transaction(app_Cl3C0 read BK3)
  tCl3C0_BK3_ld.used

  val tCl3C0_BK3_st: Transaction = Transaction(app_Cl3C0 write BK3)
  tCl3C0_BK3_st.used

  val tCl3C1_L1_ld: Transaction = Transaction(app_Cl3C1 read Cl3.C1_L1)
  tCl3C1_L1_ld.used

  val tCl3C1_L1_st: Transaction = Transaction(app_Cl3C1 write Cl3.C1_L1)
  tCl3C1_L1_st.used

  val tCl3C1_L2_ld: Transaction = Transaction(app_Cl3C1 read Cl3.L2)
  tCl3C1_L2_ld.used

  val tCl3C1_L2_st: Transaction = Transaction(app_Cl3C1 write Cl3.L2)
  tCl3C1_L2_st.used

  val tCl3C1_BK3_ld: Transaction = Transaction(app_Cl3C1 read BK3)
  tCl3C1_BK3_ld.used

  val tCl3C1_BK3_st: Transaction = Transaction(app_Cl3C1 write BK3)
  tCl3C1_BK3_st.used

  val tCl3C2_L1_ld: Transaction = Transaction(app_Cl3C2 read Cl3.C2_L1)
  tCl3C2_L1_ld.used

  val tCl3C2_L1_st: Transaction = Transaction(app_Cl3C2 write Cl3.C2_L1)
  tCl3C2_L1_st.used

  val tCl3C2_L2_ld: Transaction = Transaction(app_Cl3C2 read Cl3.L2)
  tCl3C2_L2_ld.used

  val tCl3C2_L2_st: Transaction = Transaction(app_Cl3C2 write Cl3.L2)
  tCl3C2_L2_st.used

  val tCl3C2_BK3_ld: Transaction = Transaction(app_Cl3C2 read BK3)
  tCl3C2_BK3_ld.used

  val tCl3C2_BK3_st: Transaction = Transaction(app_Cl3C2 write BK3)
  tCl3C2_BK3_st.used

  val tCl3C3_L1_ld: Transaction = Transaction(app_Cl3C3 read Cl3.C3_L1)
  tCl3C3_L1_ld.used

  val tCl3C3_L1_st: Transaction = Transaction(app_Cl3C3 write Cl3.C3_L1)
  tCl3C3_L1_st.used

  val tCl3C3_L2_ld: Transaction = Transaction(app_Cl3C3 read Cl3.L2)
  tCl3C3_L2_ld.used

  val tCl3C3_L2_st: Transaction = Transaction(app_Cl3C3 write Cl3.L2)
  tCl3C3_L2_st.used

  val tCl3C3_BK3_ld: Transaction = Transaction(app_Cl3C3 read BK3)
  tCl3C3_BK3_ld.used

  val tCl3C3_BK3_st: Transaction = Transaction(app_Cl3C3 write BK3)
  tCl3C3_BK3_st.used

  val tCl4C0_L1_ld: Transaction = Transaction(app_Cl4C0 read Cl4.C0_L1)
  tCl4C0_L1_ld.used

  val tCl4C0_L1_st: Transaction = Transaction(app_Cl4C0 write Cl4.C0_L1)
  tCl4C0_L1_st.used

  val tCl4C0_L2_ld: Transaction = Transaction(app_Cl4C0 read Cl4.L2)
  tCl4C0_L2_ld.used

  val tCl4C0_L2_st: Transaction = Transaction(app_Cl4C0 write Cl4.L2)
  tCl4C0_L2_st.used

  val tCl4C0_BK4_ld: Transaction = Transaction(app_Cl4C0 read BK4)
  tCl4C0_BK4_ld.used

  val tCl4C0_BK4_st: Transaction = Transaction(app_Cl4C0 write BK4)
  tCl4C0_BK4_st.used

  val tCl4C1_L1_ld: Transaction = Transaction(app_Cl4C1 read Cl4.C1_L1)
  tCl4C1_L1_ld.used

  val tCl4C1_L1_st: Transaction = Transaction(app_Cl4C1 write Cl4.C1_L1)
  tCl4C1_L1_st.used

  val tCl4C1_L2_ld: Transaction = Transaction(app_Cl4C1 read Cl4.L2)
  tCl4C1_L2_ld.used

  val tCl4C1_L2_st: Transaction = Transaction(app_Cl4C1 write Cl4.L2)
  tCl4C1_L2_st.used

  val tCl4C1_BK4_ld: Transaction = Transaction(app_Cl4C1 read BK4)
  tCl4C1_BK4_ld.used

  val tCl4C1_BK4_st: Transaction = Transaction(app_Cl4C1 write BK4)
  tCl4C1_BK4_st.used

  val tCl4C2_L1_ld: Transaction = Transaction(app_Cl4C2 read Cl4.C2_L1)
  tCl4C2_L1_ld.used

  val tCl4C2_L1_st: Transaction = Transaction(app_Cl4C2 write Cl4.C2_L1)
  tCl4C2_L1_st.used

  val tCl4C2_L2_ld: Transaction = Transaction(app_Cl4C2 read Cl4.L2)
  tCl4C2_L2_ld.used

  val tCl4C2_L2_st: Transaction = Transaction(app_Cl4C2 write Cl4.L2)
  tCl4C2_L2_st.used

  val tCl4C2_BK4_ld: Transaction = Transaction(app_Cl4C2 read BK4)
  tCl4C2_BK4_ld.used

  val tCl4C2_BK4_st: Transaction = Transaction(app_Cl4C2 write BK4)
  tCl4C2_BK4_st.used

  val tCl4C3_L1_ld: Transaction = Transaction(app_Cl4C3 read Cl4.C3_L1)
  tCl4C3_L1_ld.used

  val tCl4C3_L1_st: Transaction = Transaction(app_Cl4C3 write Cl4.C3_L1)
  tCl4C3_L1_st.used

  val tCl4C3_L2_ld: Transaction = Transaction(app_Cl4C3 read Cl4.L2)
  tCl4C3_L2_ld.used

  val tCl4C3_L2_st: Transaction = Transaction(app_Cl4C3 write Cl4.L2)
  tCl4C3_L2_st.used

  val tCl4C3_BK4_ld: Transaction = Transaction(app_Cl4C3 read BK4)
  tCl4C3_BK4_ld.used

  val tCl4C3_BK4_st: Transaction = Transaction(app_Cl4C3 write BK4)
  tCl4C3_BK4_st.used

  val tCl5C0_L1_ld: Transaction = Transaction(app_Cl5C0 read Cl5.C0_L1)
  tCl5C0_L1_ld.used

  val tCl5C0_L1_st: Transaction = Transaction(app_Cl5C0 write Cl5.C0_L1)
  tCl5C0_L1_st.used

  val tCl5C0_L2_ld: Transaction = Transaction(app_Cl5C0 read Cl5.L2)
  tCl5C0_L2_ld.used

  val tCl5C0_L2_st: Transaction = Transaction(app_Cl5C0 write Cl5.L2)
  tCl5C0_L2_st.used

  val tCl5C0_BK5_ld: Transaction = Transaction(app_Cl5C0 read BK5)
  tCl5C0_BK5_ld.used

  val tCl5C0_BK5_st: Transaction = Transaction(app_Cl5C0 write BK5)
  tCl5C0_BK5_st.used

  val tCl5C1_L1_ld: Transaction = Transaction(app_Cl5C1 read Cl5.C1_L1)
  tCl5C1_L1_ld.used

  val tCl5C1_L1_st: Transaction = Transaction(app_Cl5C1 write Cl5.C1_L1)
  tCl5C1_L1_st.used

  val tCl5C1_L2_ld: Transaction = Transaction(app_Cl5C1 read Cl5.L2)
  tCl5C1_L2_ld.used

  val tCl5C1_L2_st: Transaction = Transaction(app_Cl5C1 write Cl5.L2)
  tCl5C1_L2_st.used

  val tCl5C1_BK5_ld: Transaction = Transaction(app_Cl5C1 read BK5)
  tCl5C1_BK5_ld.used

  val tCl5C1_BK5_st: Transaction = Transaction(app_Cl5C1 write BK5)
  tCl5C1_BK5_st.used

  val tCl5C2_L1_ld: Transaction = Transaction(app_Cl5C2 read Cl5.C2_L1)
  tCl5C2_L1_ld.used

  val tCl5C2_L1_st: Transaction = Transaction(app_Cl5C2 write Cl5.C2_L1)
  tCl5C2_L1_st.used

  val tCl5C2_L2_ld: Transaction = Transaction(app_Cl5C2 read Cl5.L2)
  tCl5C2_L2_ld.used

  val tCl5C2_L2_st: Transaction = Transaction(app_Cl5C2 write Cl5.L2)
  tCl5C2_L2_st.used

  val tCl5C2_BK5_ld: Transaction = Transaction(app_Cl5C2 read BK5)
  tCl5C2_BK5_ld.used

  val tCl5C2_BK5_st: Transaction = Transaction(app_Cl5C2 write BK5)
  tCl5C2_BK5_st.used

  val tCl5C3_L1_ld: Transaction = Transaction(app_Cl5C3 read Cl5.C3_L1)
  tCl5C3_L1_ld.used

  val tCl5C3_L1_st: Transaction = Transaction(app_Cl5C3 write Cl5.C3_L1)
  tCl5C3_L1_st.used

  val tCl5C3_L2_ld: Transaction = Transaction(app_Cl5C3 read Cl5.L2)
  tCl5C3_L2_ld.used

  val tCl5C3_L2_st: Transaction = Transaction(app_Cl5C3 write Cl5.L2)
  tCl5C3_L2_st.used

  val tCl5C3_BK5_ld: Transaction = Transaction(app_Cl5C3 read BK5)
  tCl5C3_BK5_ld.used

  val tCl5C3_BK5_st: Transaction = Transaction(app_Cl5C3 write BK5)
  tCl5C3_BK5_st.used

  val tCl6C0_L1_ld: Transaction = Transaction(app_Cl6C0 read Cl6.C0_L1)
  tCl6C0_L1_ld.used

  val tCl6C0_L1_st: Transaction = Transaction(app_Cl6C0 write Cl6.C0_L1)
  tCl6C0_L1_st.used

  val tCl6C0_L2_ld: Transaction = Transaction(app_Cl6C0 read Cl6.L2)
  tCl6C0_L2_ld.used

  val tCl6C0_L2_st: Transaction = Transaction(app_Cl6C0 write Cl6.L2)
  tCl6C0_L2_st.used

  val tCl6C0_BK6_ld: Transaction = Transaction(app_Cl6C0 read BK6)
  tCl6C0_BK6_ld.used

  val tCl6C0_BK6_st: Transaction = Transaction(app_Cl6C0 write BK6)
  tCl6C0_BK6_st.used

  val tCl6C1_L1_ld: Transaction = Transaction(app_Cl6C1 read Cl6.C1_L1)
  tCl6C1_L1_ld.used

  val tCl6C1_L1_st: Transaction = Transaction(app_Cl6C1 write Cl6.C1_L1)
  tCl6C1_L1_st.used

  val tCl6C1_L2_ld: Transaction = Transaction(app_Cl6C1 read Cl6.L2)
  tCl6C1_L2_ld.used

  val tCl6C1_L2_st: Transaction = Transaction(app_Cl6C1 write Cl6.L2)
  tCl6C1_L2_st.used

  val tCl6C1_BK6_ld: Transaction = Transaction(app_Cl6C1 read BK6)
  tCl6C1_BK6_ld.used

  val tCl6C1_BK6_st: Transaction = Transaction(app_Cl6C1 write BK6)
  tCl6C1_BK6_st.used

  val tCl6C2_L1_ld: Transaction = Transaction(app_Cl6C2 read Cl6.C2_L1)
  tCl6C2_L1_ld.used

  val tCl6C2_L1_st: Transaction = Transaction(app_Cl6C2 write Cl6.C2_L1)
  tCl6C2_L1_st.used

  val tCl6C2_L2_ld: Transaction = Transaction(app_Cl6C2 read Cl6.L2)
  tCl6C2_L2_ld.used

  val tCl6C2_L2_st: Transaction = Transaction(app_Cl6C2 write Cl6.L2)
  tCl6C2_L2_st.used

  val tCl6C2_BK6_ld: Transaction = Transaction(app_Cl6C2 read BK6)
  tCl6C2_BK6_ld.used

  val tCl6C2_BK6_st: Transaction = Transaction(app_Cl6C2 write BK6)
  tCl6C2_BK6_st.used

  val tCl6C3_L1_ld: Transaction = Transaction(app_Cl6C3 read Cl6.C3_L1)
  tCl6C3_L1_ld.used

  val tCl6C3_L1_st: Transaction = Transaction(app_Cl6C3 write Cl6.C3_L1)
  tCl6C3_L1_st.used

  val tCl6C3_L2_ld: Transaction = Transaction(app_Cl6C3 read Cl6.L2)
  tCl6C3_L2_ld.used

  val tCl6C3_L2_st: Transaction = Transaction(app_Cl6C3 write Cl6.L2)
  tCl6C3_L2_st.used

  val tCl6C3_BK6_ld: Transaction = Transaction(app_Cl6C3 read BK6)
  tCl6C3_BK6_ld.used

  val tCl6C3_BK6_st: Transaction = Transaction(app_Cl6C3 write BK6)
  tCl6C3_BK6_st.used

  val tCl7C0_L1_ld: Transaction = Transaction(app_Cl7C0 read Cl7.C0_L1)
  tCl7C0_L1_ld.used

  val tCl7C0_L1_st: Transaction = Transaction(app_Cl7C0 write Cl7.C0_L1)
  tCl7C0_L1_st.used

  val tCl7C0_L2_ld: Transaction = Transaction(app_Cl7C0 read Cl7.L2)
  tCl7C0_L2_ld.used

  val tCl7C0_L2_st: Transaction = Transaction(app_Cl7C0 write Cl7.L2)
  tCl7C0_L2_st.used

  val tCl7C0_BK7_ld: Transaction = Transaction(app_Cl7C0 read BK7)
  tCl7C0_BK7_ld.used

  val tCl7C0_BK7_st: Transaction = Transaction(app_Cl7C0 write BK7)
  tCl7C0_BK7_st.used

  val tCl7C1_L1_ld: Transaction = Transaction(app_Cl7C1 read Cl7.C1_L1)
  tCl7C1_L1_ld.used

  val tCl7C1_L1_st: Transaction = Transaction(app_Cl7C1 write Cl7.C1_L1)
  tCl7C1_L1_st.used

  val tCl7C1_L2_ld: Transaction = Transaction(app_Cl7C1 read Cl7.L2)
  tCl7C1_L2_ld.used

  val tCl7C1_L2_st: Transaction = Transaction(app_Cl7C1 write Cl7.L2)
  tCl7C1_L2_st.used

  val tCl7C1_BK7_ld: Transaction = Transaction(app_Cl7C1 read BK7)
  tCl7C1_BK7_ld.used

  val tCl7C1_BK7_st: Transaction = Transaction(app_Cl7C1 write BK7)
  tCl7C1_BK7_st.used

  val tCl7C2_L1_ld: Transaction = Transaction(app_Cl7C2 read Cl7.C2_L1)
  tCl7C2_L1_ld.used

  val tCl7C2_L1_st: Transaction = Transaction(app_Cl7C2 write Cl7.C2_L1)
  tCl7C2_L1_st.used

  val tCl7C2_L2_ld: Transaction = Transaction(app_Cl7C2 read Cl7.L2)
  tCl7C2_L2_ld.used

  val tCl7C2_L2_st: Transaction = Transaction(app_Cl7C2 write Cl7.L2)
  tCl7C2_L2_st.used

  val tCl7C2_BK7_ld: Transaction = Transaction(app_Cl7C2 read BK7)
  tCl7C2_BK7_ld.used

  val tCl7C2_BK7_st: Transaction = Transaction(app_Cl7C2 write BK7)
  tCl7C2_BK7_st.used

  val tCl7C3_L1_ld: Transaction = Transaction(app_Cl7C3 read Cl7.C3_L1)
  tCl7C3_L1_ld.used

  val tCl7C3_L1_st: Transaction = Transaction(app_Cl7C3 write Cl7.C3_L1)
  tCl7C3_L1_st.used

  val tCl7C3_L2_ld: Transaction = Transaction(app_Cl7C3 read Cl7.L2)
  tCl7C3_L2_ld.used

  val tCl7C3_L2_st: Transaction = Transaction(app_Cl7C3 write Cl7.L2)
  tCl7C3_L2_st.used

  val tCl7C3_BK7_ld: Transaction = Transaction(app_Cl7C3 read BK7)
  tCl7C3_BK7_ld.used

  val tCl7C3_BK7_st: Transaction = Transaction(app_Cl7C3 write BK7)
  tCl7C3_BK7_st.used

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
