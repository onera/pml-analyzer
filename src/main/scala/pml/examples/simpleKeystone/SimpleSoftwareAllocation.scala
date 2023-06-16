/** *****************************************************************************
  * Copyright (c) 2021. ONERA
  * This file is part of PML Analyzer
  *
  * PML Analyzer is free software ;
  * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation ;
  * either version 2 of  the License, or (at your option) any later version.
  *
  * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this program ;
  * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
  * **************************************************************************** */

package pml.examples.simpleKeystone

import pml.model.software.{Application, Data}
import pml.operators._

import scala.language.postfixOps

/**
  * Definition and allocation of software and data to hardware for simple Keystone.
  * Declaring the application app1
  * {{{val app1: Application = Application()}}}
  * Allocating app1 on [[pml.examples.simpleKeystone.SimpleKeystonePlatform.ARM0]]
  * {{{app1 hostedBy ARM0.core}}}
  * Declaring the data input_d
  * {{{val input_d: Data = Data()}}}
  * Allocating app1 on [[pml.examples.simpleKeystone.SimpleKeystonePlatform.MemorySubsystem.sram]]
  * {{{input_d hostedBy MemorySubsystem.sram}}}
  * @see [[pml.operators.Use.Ops]] for hostedBy operator definition
  */
trait SimpleSoftwareAllocation {
  self: SimpleKeystonePlatform =>

  /* -----------------------------------------------------------
    * Application declaration
    * ----------------------------------------------------------- */

  /** [[app1]] is an asynchronous applicative activated each time a external interrupt arrives
    * @group application
    */
  val app1: Application = Application()

  /** At each period [[app21]] reads the last Ethernet message from [[SimpleKeystonePlatform.MemorySubsystem.sram]],
    * makes some input treatments on the message, and makes it available for [[app1]] in [[SimpleKeystonePlatform.ddr]].
    * @group application
    */
  val app21: Application = Application()

  /** At each period [[app22]] reads output data of [[app1]] from [[SimpleKeystonePlatform.ddr]].
    * It transforms them into [[SimpleKeystonePlatform.spi]] frames.
    * The frames are then store in [[SimpleKeystonePlatform.MemorySubsystem.sram]]. And finally [[app22]] wakes up [[app3]]
    * by writing the address of the [[SimpleKeystonePlatform.spi]] frames into [[SimpleKeystonePlatform.dma_reg]].
    * @group application
    * */
  val app22: Application = Application()

  /** [[app3]] is a microcode running on [[SimpleKeystonePlatform.dma]]. When woke up,
    * [[app3]] reads the [[SimpleKeystonePlatform.spi]] frame from [[SimpleKeystonePlatform.MemorySubsystem.sram]]
    * and transfers it to [[SimpleKeystonePlatform.spi]]
    * @group application
    * */
  val app3: Application = Application()

  /** [[app4]] is an asynchronous microcode running on the [[SimpleKeystonePlatform.eth]] component.
    * Each time an [[SimpleKeystonePlatform.eth]] frame arrives, it transfers the payload of the
    * frame to [[SimpleKeystonePlatform.MemorySubsystem.sram]].
    * @group application
    * */
  val app4: Application = Application()


  /* -----------------------------------------------------------
    * Data declaration
    * ----------------------------------------------------------- */

  /** Data written by [[SimpleKeystonePlatform.eth]] in [[SimpleKeystonePlatform.MemorySubsystem.sram]] and read by [[app21]]
    * @group data */
  val input_d: Data = Data()
  /** Data written by [[app21]] in [[SimpleKeystonePlatform.ddr]] and read by [[app1]]
    * @group data */
  val d1: Data = Data()
  /** Interrupt read by [[app1]]
    * @group data */
  val interrupt1: Data = Data()
  /** Data written by [[app1]] in [[SimpleKeystonePlatform.ddr]] and read by [[app22]]
    *  @group data */
  val d2: Data = Data()
  /** Data written by [[app22]] in [[SimpleKeystonePlatform.MemorySubsystem.sram]] and read by [[SimpleKeystonePlatform.dma]]
    *  @group data*/
  val output_d: Data = Data()
  /** Register value written by [[app21]] in [[SimpleKeystonePlatform.dma_reg]]
    *  @group data */
  val dma_red_value: Data = Data()
  /** [[SimpleKeystonePlatform.spi]] frame put by [[SimpleKeystonePlatform.dma]] on the [[SimpleKeystonePlatform.spi]] port
    *  @group data */
  val output_spi_frame: Data = Data()

  /* -----------------------------------------------------------
    * Data allocation
    * ----------------------------------------------------------- */

  input_d hostedBy MemorySubsystem.sram
  d1 hostedBy ddr
  interrupt1 hostedBy mpic
  d2 hostedBy ddr
  output_d hostedBy MemorySubsystem.sram
  dma_red_value hostedBy dma_reg
  output_spi_frame hostedBy spi

  /* -----------------------------------------------------------
    * Application allocation
    * ----------------------------------------------------------- */

  app1 hostedBy ARM0.core
  app21 hostedBy ARM1.core
  app22 hostedBy ARM1.core
  app3 hostedBy dma
  app4 hostedBy eth

}
