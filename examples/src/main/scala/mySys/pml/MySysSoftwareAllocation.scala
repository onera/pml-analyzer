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

package mySys.pml

import mySys.pml
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

/** Definition and allocation of software and data to hardware for simple
  * Keystone. Declaring the application app1
  * {{{val app1: Application = Application()}}} Allocating app1 on
  * [[MyProcPlatform.ARM0]]
  * {{{app1 hostedBy ARM0.core}}} Declaring the data input_d
  * {{{val input_d: Data = Data()}}} Allocating app1 on
  * [[MyProcPlatform.MemorySubsystem.sram]]
  * {{{input_d hostedBy MemorySubsystem.sram}}}
  * @see
  *   [[pml.operators.Use.Ops]] for hostedBy operator definition
  */
trait MySysSoftwareAllocation {
  self: MyProcPlatform =>

  /* -----------------------------------------------------------
   * Application declaration
   * ----------------------------------------------------------- */

  /** [[app1]] is an asynchronous applicative activated each time a external
    * interrupt arrives
    * @group application
    */
  val app1: Application = Application()

  /** At each period [[app21]] reads the last Ethernet message from
    * [[MyProcPlatform.MemorySubsystem.sram]], makes some input
    * treatments on the message, and makes it available for [[app1]] in
    * [[MyProcPlatform.ddr]].
    * @group application
    */
  val app21: Application = Application()

  /** At each period [[app22]] reads output data of [[app1]] from
    * [[MyProcPlatform.ddr]]. It transforms them into
    * [[MyProcPlatform.spi]] frames. The frames are then store in
    * [[MyProcPlatform.MemorySubsystem.sram]]. And finally [[app22]]
    * wakes up [[app3]] by writing the address of the
    * [[MyProcPlatform.spi]] frames into
    * [[MyProcPlatform.dma_reg]].
    * @group application
    */
  val app22: Application = Application()

  /** [[app3]] is a microcode running on [[MyProcPlatform.dma]]. When
    * woke up, [[app3]] reads the [[MyProcPlatform.spi]] frame from
    * [[MyProcPlatform.MemorySubsystem.sram]] and transfers it to
    * [[MyProcPlatform.spi]]
    * @group application
    */
  val app3: Application = Application()

  /** [[app4]] is an asynchronous microcode running on the
    * [[MyProcPlatform.eth]] component. Each time an
    * [[MyProcPlatform.eth]] frame arrives, it transfers the payload of
    * the frame to [[MyProcPlatform.MemorySubsystem.sram]].
    * @group application
    */
  val app4: Application = Application()

  /* -----------------------------------------------------------
   * Data declaration
   * ----------------------------------------------------------- */

  /** Data written by [[MyProcPlatform.eth]] in
    * [[MyProcPlatform.MemorySubsystem.sram]] and read by [[app21]]
    * @group data
    */
  val ethernet_frame: Data = Data()

  /** Data written by [[app21]] in [[MyProcPlatform.ddr]] and read by
    * [[app1]]
    * @group data
    */
  val input_app1: Data = Data()

  /** Interrupt read by [[app1]]
    * @group data
    */
  val interrupt_code: Data = Data()

  /** Data written by [[app1]] in [[MyProcPlatform.ddr]] and read by
    * [[app22]]
    * @group data
    */
  val output_app1: Data = Data()

  /** Data written by [[app22]] in
    * [[MyProcPlatform.MemorySubsystem.sram]] and read by
    * [[MyProcPlatform.dma]]
    * @group data
    */
  val spi_frame: Data = Data()

  /** Register value written by [[app21]] in [[MyProcPlatform.dma_reg]]
    * @group data
    */
  val dma_reg_value: Data = Data()

  /** [[MyProcPlatform.spi]] frame put by [[MyProcPlatform.dma]]
    * on the [[MyProcPlatform.spi]] port
    * @group data
    */
  val output_spi_frame: Data = Data()

  /** Private cache of [[app1]]
    * @group data
    */
  val app1_cache: Data = Data()

  /* -----------------------------------------------------------
   * Data allocation
   * ----------------------------------------------------------- */

  input_app1 hostedBy ddr
  interrupt_code hostedBy mpic
  app1_cache hostedBy ARM0.cache
  output_app1 hostedBy ddr
  spi_frame hostedBy MemorySubsystem.sram
  dma_reg_value hostedBy dma_reg
  output_spi_frame hostedBy spi
  ethernet_frame hostedBy MemorySubsystem.sram

  /* -----------------------------------------------------------
   * Application allocation
   * ----------------------------------------------------------- */

  app1 hostedBy ARM0.core
  app21 hostedBy ARM1.core
  app22 hostedBy ARM1.core
  app3 hostedBy dma
  app4 hostedBy eth

}
