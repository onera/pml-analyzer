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

package simpleT1042.pml

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait SimpleT1042TransactionLibrary extends TransactionLibrary {
  self: SimpleT1042Platform with SimpleSoftwareAllocation =>

  /** ----------------------------------------------------------- Target usage
    * -----------------------------------------------------------
    */

  val app4_wr_input_d: Transaction = Transaction(app4 write input_d)
  val app21_rd_input_d: Transaction = Transaction(app21 read input_d)
  val app21_wr_d1: Transaction = Transaction(app21 write d1)
  val app1_rd_interrupt1: Transaction = Transaction(app1 read interrupt1)
  val app1_rd_d1: Transaction = Transaction(app1 read d1)
  val app1_wr_d2: Transaction = Transaction(app1 write d2)
  val app1_rd_L1: Transaction = Transaction(app1 read C1.L1)
  val app1_wr_L1: Transaction = Transaction(app1 write C1.L1)
  val app1_rd_wr_L1: Transaction = Transaction(app1_rd_L1, app1_wr_L1)
  val app22_rd_d2: Transaction = Transaction(app22 read d2)
  val app22_wr_output_d: Transaction = Transaction(app22 write output_d)
  val app22_st_dma_reg: Transaction = Transaction(app22 write dma_reg)

  /** ----------------------------------------------------------- DMA copies
    * -----------------------------------------------------------
    */
  private val app3_rd_output_d: Transaction = Transaction(app3 read output_d)
  private val app3_wr_output_pcie_frame: Transaction = Transaction(
    app3 write output_pcie_frame
  )
  val app3_transfer: Transaction =
    Transaction(app3_rd_output_d, app3_wr_output_pcie_frame)
}
