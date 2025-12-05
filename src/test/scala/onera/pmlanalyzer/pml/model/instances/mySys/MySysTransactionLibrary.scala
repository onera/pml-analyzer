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

package onera.pmlanalyzer.pml.model.instances.mySys

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.*

import scala.language.postfixOps

/** This trait contains a library of all transactions that can occur on the
  * platform One way to define a
  * [[onera.pmlanalyzer.pml.model.configuration.Transaction]] is to use the
  * read/write operators specifying which [[pml.model.software.Data]] is used by
  * which [[pml.model.software.Application]]. For instance
  * {{{val app4_wr_input_d : Transaction = Transaction(app4 write input_d)}}}
  * defines a read transaction called '''app4_wr_input_d''' between the
  * initiator of app4 and the input_d data. The location of the application and
  * the data are provided in the [[MySysSoftwareAllocation]] trait.
  *
  * If you want to define several paths representing a multi-transaction use the
  * [[onera.pmlanalyzer.pml.model.configuration.Transaction]] For instance
  * {{{val app1_rd_wr_L1 : Transaction = Transaction(app1_rd_L1, app1_wr_L1)}}}
  * defines a transaction named '''app1_rd_wr_L1''' where app1 is reading and
  * writing L1 cache
  * @note
  *   A transaction or a transaction is only '''declared''' here, it will be
  *   considered during the interference analysis if it is actually used. This
  *   is done in the [[MySysTransactionLibrary]] files. A transaction
  *   should be a path from an initiator to a target, if several paths are
  *   possible a warning will be raised.
  * @see
  *   [[onera.pmlanalyzer.pml.operators.Use.Ops]] for read/write operator definitions
  */
trait MySysTransactionLibrary extends TransactionLibrary {
  self: MyProcPlatform with MySysSoftwareAllocation =>

  /** t11: [[MySysSoftwareAllocation.app1]] begins by reading the interrupt code
    * from [[MyProcPlatform.mpic]]
    * @group transaction_def
    */
  val t11: Transaction = Transaction(app1 read interrupt_code)

  /** t12: [[MySysSoftwareAllocation.app1]] reads its input data from
    * [[MyProcPlatform.ddr]]
    * @group transaction_def
    */
  val t12: Transaction = Transaction(app1 read input_app1)

  private val app1_rd_L1: Transaction = Transaction(app1 read ARM0.cache)
  private val app1_wr_L1: Transaction = Transaction(app1 write ARM0.cache)

  /** t13: [[MySysSoftwareAllocation.app1]] runs using [[MyProcPlatform.ARM0]]
    * cache
    *
    * @group transaction_def
    */
  val t13: Transaction = Transaction(app1_rd_L1, app1_wr_L1)

  /** t14: [[MySysSoftwareAllocation.app1]] it stores its output data in
    * [[MyProcPlatform.ddr]]
    * @group transaction_def
    */
  val t14: Transaction = Transaction(app1 write output_app1)

  /** t21: [[MySysSoftwareAllocation.app21]] reads [[MyProcPlatform.dma_reg]]
    * value
    * @group transaction_def
    */
  val t21: Transaction = Transaction(app21 read dma_reg_value)

  /** t22: [[MySysSoftwareAllocation.app22]] load the Ethernet frame in
    * [[MyProcPlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t22: Transaction = Transaction(app22 read ethernet_frame)

  /** t23: [[MySysSoftwareAllocation.app22]] stores the processed Ethernet frame
    * in [[MyProcPlatform.ddr]] and makes it available for
    * [[MySysSoftwareAllocation.app1]]
    * @group transaction_def
    */
  val t23: Transaction = Transaction(app22 write input_app1)

  /** t24: [[MySysSoftwareAllocation.app22]] reads the output of
    * [[MySysSoftwareAllocation.app1]] and transforms them into SPI frames
    * @group transaction_def
    */
  val t24: Transaction = Transaction(app22 read output_app1)

  /** t25: [[MySysSoftwareAllocation.app22]] writes the transformation in
    * [[MyProcPlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t25: Transaction = Transaction(app22 write spi_frame)

  /** t26: [[MySysSoftwareAllocation.app22]] wakes up the [[MyProcPlatform.dma]]
    * by writing the address of the [[MyProcPlatform.spi]] frames into
    * [[MyProcPlatform.dma_reg]]
    * @group transaction_def
    */
  val t26: Transaction = Transaction(app22 write dma_reg)

  /** t31: When woke up, [[MySysSoftwareAllocation.app3]] reads the
    * [[MyProcPlatform.spi]] frame from [[MyProcPlatform.MemorySubsystem.sram]]
    * and transfers it to [[MyProcPlatform.spi]]
    * @group transaction_def
    */
  private val readFrame = Transaction(app3 read spi_frame)
  private val writeFrame = Transaction(app3 write output_spi_frame)
  val t31: Transaction = Transaction(readFrame, writeFrame)

  /** t41: Each time an [[MyProcPlatform.eth]] frame arrives, it transfers the
    * payload of the frame to [[MyProcPlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t41: Transaction = Transaction(app4 write ethernet_frame)

}
