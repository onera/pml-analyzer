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

package onera.pmlanalyzer.pml.examples.simpleKeystone

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.operators._
import onera.pmlanalyzer.pml.model.configuration.*

import scala.language.postfixOps

/** This trait contains a library of all transactions that can occur on the
  * platform One way to define a
  * [[pml.model.configuration.TransactionLibrary.Transaction]] or a
  * [[pml.model.configuration.TransactionLibrary.Scenario]] is to use the
  * read/write operators specifying which [[pml.model.software.Data]] is used by
  * which [[pml.model.software.Application]]. For instance
  * {{{val app4_wr_input_d : Transaction = Transaction(app4 write input_d)}}}
  * defines a read transaction called '''app4_wr_input_d''' between the
  * initiator of app4 and the input_d data. The location of the application and
  * the data are provided in the [[SimpleSoftwareAllocation]] trait.
  *
  * If you want to define several paths representing a multi-transaction use the
  * [[pml.model.configuration.TransactionLibrary.Scenario]] For instance
  * {{{val app1_rd_wr_L1 : Scenario = Scenario(app1_rd_L1, app1_wr_L1)}}}
  * defines a scenario named '''app1_rd_wr_L1''' where app1 is reading and
  * writing L1 cache
  * @note
  *   A transaction or a scenario is only '''declared''' here, it will be
  *   considered during the interference analysis if it is actually used. This
  *   is done in the [[SimpleKeystoneLibraryConfiguration]] files. A transaction
  *   should be a path from an initiator to a target, if several paths are
  *   possible a warning will be raised.
  * @see
  *   [[pml.operators.Use.Ops]] for read/write operator definitions
  */
trait SimpleKeystoneTransactionLibrary extends TransactionLibrary {
  self: SimpleKeystonePlatform with SimpleSoftwareAllocation =>

  /** t41: Each time an [[SimpleKeystonePlatform.eth]] frame arrives, it
    * transfers the payload of the frame to
    * [[SimpleKeystonePlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t41_app4_wr_input_d: Transaction = Transaction(app4 write input_d)

  /** t211: [[SimpleSoftwareAllocation.app21]] reads the last
    * [[SimpleKeystonePlatform.eth]] message from
    * [[SimpleKeystonePlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t211_app21_rd_input_d: Transaction = Transaction(app21 read input_d)

  /** t212: [[SimpleSoftwareAllocation.app21]] makes some input treatments on
    * the message, and makes it available for [[SimpleSoftwareAllocation.app1]]
    * in [[SimpleKeystonePlatform.ddr]]
    * @group transaction_def
    */
  val t212_app21_wr_d1: Transaction = Transaction(app21 write d1)

  /** t11: [[SimpleSoftwareAllocation.app1]] begins by reading the interrupt
    * code from [[SimpleKeystonePlatform.mpic]]
    * @group transaction_def
    */
  val t11_app1_rd_interrupt1: Transaction = Transaction(app1 read interrupt1)

  /** t12: [[SimpleSoftwareAllocation.app1]] reads its input data from
    * [[SimpleKeystonePlatform.ddr]]
    * @group transaction_def
    */
  val t12_app1_rd_d1: Transaction = Transaction(app1 read d1)

  /** t13: [[SimpleSoftwareAllocation.app1]] it stores its output data in
    * [[SimpleKeystonePlatform.ddr]]
    * @group transaction_def
    */
  val t13_app1_wr_d2: Transaction = Transaction(app1 write d2)

  private val app1_rd_L1: Transaction = Transaction(app1 read ARM0.cache)
  private val app1_wr_L1: Transaction = Transaction(app1 write ARM0.cache)

  /** t14: [[SimpleSoftwareAllocation.app1]] runs using
    * [[SimpleKeystonePlatform.ARM0]] cache
    * @group scenario_def
    */
  val t14_app1_rd_wr_L1: Scenario = Scenario(app1_rd_L1, app1_wr_L1)

  /** t221: [[SimpleSoftwareAllocation.app22]] reads output data of
    * [[SimpleSoftwareAllocation.app1]] from [[SimpleKeystonePlatform.ddr]]
    * @group transaction_def
    */
  val t221_app22_rd_d2: Transaction = Transaction(app22 read d2)

  /** t222: [[SimpleSoftwareAllocation.app22]] store the
    * [[SimpleKeystonePlatform.spi]] frame then in
    * [[SimpleKeystonePlatform.MemorySubsystem.sram]]
    * @group transaction_def
    */
  val t222_app22_wr_output_d: Transaction = Transaction(app22 write output_d)

  /** t223: [[SimpleSoftwareAllocation.app22]] wakes up the
    * [[SimpleKeystonePlatform.dma]] by writing the address of the
    * [[SimpleKeystonePlatform.spi]] frames into
    * [[SimpleKeystonePlatform.dma_reg]]
    * @group transaction_def
    */
  val t223_app22_st_dma_reg: Transaction = Transaction(app22 write dma_reg)

  /** When woke up, [[SimpleSoftwareAllocation.app3]] reads the
    * [[SimpleKeystonePlatform.spi]] frame from
    * [[SimpleKeystonePlatform.MemorySubsystem.sram]] and transfers it to
    * [[SimpleKeystonePlatform.spi]]
    * @group scenario_def
    */
  private val readFrame = Transaction(app3 read output_d)
  private val writeFrame = Transaction(app3 write output_spi_frame)
  val app3_transfer: Scenario =
    Scenario(readFrame, writeFrame)
}
