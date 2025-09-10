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

import onera.pmlanalyzer.pml.examples.riscv.FU740.pml
import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.hardware.Target
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

import scala.language.postfixOps

/**
  * This trait contains a library of all transactions that can occur on the platform
  * One way to define a [[pml.model.configuration.TransactionLibrary.Transaction]] or
  * a [[pml.model.configuration.TransactionLibrary.Scenario]] is to use the read/write operators specifying
  * which [[pml.model.software.Data]] is used by which [[pml.model.software.Application]].
  * The location of the application and the data are provided in the [[FU740SoftwareAllocation]] trait.
  *
  * If you want to define several paths representing a multi-transaction use the [[pml.model.configuration.TransactionLibrary.Scenario]]
  *
  * @note A transaction or a scenario is only '''declared''' here, it will be considered during the interference analysis if it is
  *      actually used. This is done in the [[FU740LibraryConfiguration]] files.
  *      A transaction should be a path from an initiator to a target, if several paths are possible a warning will be raised.
  * @see [[pml.operators.Use.Ops]] for read/write operator definitions
  * */
trait FU740TransactionLibrary extends TransactionLibrary {
  self: FU740Platform with FU740SoftwareAllocation =>

  /*
   * We model cached memory accesses using explicit scenarios, capturing the
   * accesses to the updated caches. This could be:
   * - implicit, mapping the data in all updated targets.
   * - programmatic, based on the configuration of the cache hierarchy.
   *   E.g. a non-inclusive hierarchy results in all searched cache levels
   *   being updated, where in an exclusive hierarchy only the Top-level is
   *   updated.
   */

  private val t0_rd_dtim = Transaction(app0 read ds)
  private val t0_wr_dtim = Transaction(app0 read ds)
  private val t0_rd_mem = Transaction(app0 read d0.Mem)
  private val t0_wr_mem = Transaction(app0 write d0.Mem)
  private val t0_rd_l2 = Transaction(app0 read d0.L2Cache)
  private val t0_wr_l2 = Transaction(app0 write d0.L2Cache)
  private val t0_l2_refill = Transaction(app0 write d0.L2Cache)

  val t0_0: Transaction = Transaction(t0_rd_dtim)
  val t0_1: Transaction = Transaction(t0_wr_dtim)
  val t0_2: Transaction = Transaction(t0_rd_l2)
  val t0_3: Transaction = Transaction(t0_wr_l2)
  val t0_4: Transaction = Transaction(t0_rd_mem, t0_l2_refill)
  val t0_5: Transaction = Transaction(t0_wr_mem, t0_l2_refill)

  private val t1_rd_l1 = Transaction(app1 read d1.DL1Cache)
  private val t1_wr_l1 = Transaction(app1 write d1.DL1Cache)
  private val t1_rd_l2 = Transaction(app1 read d1.L2Cache)
  private val t1_wr_l2 = Transaction(app1 write d1.L2Cache)
  private val t1_rd_mem = Transaction(app1 read d1.Mem)
  private val t1_wr_mem = Transaction(app1 write d1.Mem)
  private val t1_l1_refill = Transaction(app1 write d1.DL1Cache)
  private val t1_l2_refill = Transaction(app1 write d1.L2Cache)
  private val t1_rd_lim = Transaction(app1 read di)
  private val t1_wr_lim = Transaction(app1 write di)

  val t1_0: Transaction = Transaction(t1_rd_l1)
  val t1_1: Transaction = Transaction(t1_wr_l1)
  val t1_2: Transaction = Transaction(t1_rd_l2, t1_l1_refill)
  val t1_3: Transaction = Transaction(t1_wr_l2, t1_l1_refill)
  val t1_6: Transaction = Transaction(t1_rd_mem, t1_l2_refill, t1_l1_refill)
  val t1_7: Transaction = Transaction(t1_wr_mem, t1_l2_refill, t1_l1_refill)
  val t1_4: Transaction = Transaction(t1_rd_lim)
  val t1_5: Transaction = Transaction(t1_wr_lim)

  private val t2_rd_l1 = Transaction(app2 read d2.DL1Cache)
  private val t2_wr_l1 = Transaction(app2 write d2.DL1Cache)
  private val t2_wr_l2 = Transaction(app2 write d2.L2Cache)
  private val t2_rd_l2 = Transaction(app2 read d2.L2Cache)
  private val t2_rd_mem = Transaction(app2 read d2.Mem)
  private val t2_wr_mem = Transaction(app2 write d2.Mem)
  private val t2_l1_refill = Transaction(app2 write d2.DL1Cache)
  private val t2_l2_refill = Transaction(app2 write d2.L2Cache)
  private val t2_rd_uart = Transaction(app2 read du)

  val t2_0: Transaction = Transaction(t2_rd_l1)
  val t2_1: Transaction = Transaction(t2_wr_l1)
  val t2_2: Transaction = Transaction(t2_rd_l2, t2_l1_refill)
  val t2_3: Transaction = Transaction(t2_wr_l2, t2_l1_refill)
  val t2_4: Transaction = Transaction(t2_rd_mem, t2_l2_refill, t2_l1_refill)
  val t2_5: Transaction = Transaction(t2_wr_mem, t2_l2_refill, t2_l1_refill)
  val t2_6: Transaction = Transaction(t2_rd_uart)

  private val t3_rd_l1 = Transaction(app3 read d3.DL1Cache)
  private val t3_wr_l1 = Transaction(app3 write d3.DL1Cache)
  private val t3_rd_l2 = Transaction(app3 read d3.L2Cache)
  private val t3_wr_l2 = Transaction(app3 write d3.L2Cache)
  private val t3_rd_mem = Transaction(app3 read d3.Mem)
  private val t3_wr_mem = Transaction(app3 write d3.Mem)
  private val t3_l1_refill = Transaction(app3 write d3.DL1Cache)
  private val t3_l2_refill = Transaction(app3 write d3.L2Cache)

  val t3_0: Transaction = Transaction(t3_rd_l1)
  val t3_1: Transaction = Transaction(t3_wr_l1)
  val t3_2: Transaction = Transaction(t3_rd_l2, t3_l1_refill)
  val t3_3: Transaction = Transaction(t3_wr_l2, t3_l1_refill)
  val t3_4: Transaction = Transaction(t3_rd_mem, t3_l2_refill, t3_l1_refill)
  val t3_5: Transaction = Transaction(t3_wr_mem, t3_l2_refill, t3_l1_refill)

  private val t4_rd_l1 = Transaction(app4 read d4.DL1Cache)
  private val t4_wr_l1 = Transaction(app4 write d4.DL1Cache)
  private val t4_rd_l2 = Transaction(app4 read d4.L2Cache)
  private val t4_wr_l2 = Transaction(app4 write d4.L2Cache)
  private val t4_rd_mem = Transaction(app4 read d4.Mem)
  private val t4_wr_mem = Transaction(app4 write d4.Mem)
  private val t4_l1_refill = Transaction(app4 write d4.DL1Cache)
  private val t4_l2_refill = Transaction(app4 write d4.L2Cache)

  val t4_0: Transaction = Transaction(t4_rd_l1)
  val t4_1: Transaction = Transaction(t4_wr_l1)
  val t4_2: Transaction = Transaction(t4_rd_l2, t4_l1_refill)
  val t4_3: Transaction = Transaction(t4_wr_l2, t4_l1_refill)
  val t4_4: Transaction = Transaction(t4_rd_mem, t4_l2_refill, t4_l1_refill)
  val t4_5: Transaction = Transaction(t4_wr_mem, t4_l2_refill, t4_l1_refill)
}
