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

package simpleKeystone.pml

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Simple model of the Keystone platform illustrating the main features of PML.
  * The components of the architectures can be created using the constructors
  * provided in [[pml.model.hardware.BaseHardwareNodeBuilder]] For instance the
  * [[package.SimpleKeystonePlatform.dma]] is built with:
  * {{{val dma: Initiator = Initiator()}}} An axi-bus is built with:
  * {{{val axi-bus: SimpleTransporter= SimpleTransporter()}}} A peripheral or a
  * memory is built with {{{val sram: Target = Target()}}} Some components may
  * be composite, if you want to define one instance of composite you can use
  * the object instantiation pattern used for the TeraNet
  * {{{object TeraNet extends Composite}}} You can also define a type of
  * Composite that may be instantiated afterward, for instance here ARMCores are
  * defined as a composition of a core (initiator) and a cache (transporter). A
  * name is given as a parameter of the ARMCore class
  * {{{class ARMCore (name:Symbol) extends Composite}}} Then components can be
  * linked together, this operation simply connect the service of the same type
  * provided by the two components. For instance {{{ARM0.core link axi_bus}}}
  * links the [[pml.model.service.Load]] and [[pml.model.service.Store]] service
  * of the ARM0.core to the ones of the axi-bus Beware that all links are not
  * possible, for instance you cannot link two [[pml.model.hardware.Target]] or
  * a [[pml.model.hardware.Composite]] to another component.
  * @see
  *   [[pml.operators.Link.Ops]] for link operator definition
  * @see
  *   [[pml.model.hardware.BaseHardwareNodeBuilder]] for component instantiation
  * @param name
  *   the name of the final object merging all facets of the model
  */
class SimpleKeystonePlatform(name: Symbol) extends Platform(name) {

  /** Enable to provide the name implicitly
    * @param implicitName
    *   the name of the object/class inheriting from this class will be the name
    *   of platform
    */
  def this()(implicit implicitName: Name) = {
    this(Symbol(implicitName.value))
  }

  /** Initiator modelling the DMA
    * @group initiator
    */
  val dma: Initiator = Initiator()

  /** Composite modelling the Teranet
    * @group composite
    */
  object TeraNet extends Composite("TeraNet") {

    /** Transporter modelling the peripheral interconnect
      * @group transporter
      */
    val periph_bus: SimpleTransporter = SimpleTransporter()

    /** Transporter modelling the register interconnect
      * @group transporter
      */
    val config_bus: SimpleTransporter = SimpleTransporter()

    periph_bus link config_bus
  }

  /** Composite modelling memory subsystem
    * @group composite
    */
  object MemorySubsystem extends Composite("MemorySubsystem") {

    /** Transporter modelling the MSMC controller
      * @group transporter
      */
    val msmc: SimpleTransporter = SimpleTransporter()

    /** Transporter modelling the DDR controller
      * @group transporter
      */
    val ddr_ctrl: SimpleTransporter = SimpleTransporter()

    /** Target modelling the SRAM peripheral
      * @group transporter
      */
    val sram: Target = Target()

    msmc link sram
    msmc link ddr_ctrl
  }

  /** Composite representing Keystone ARM cores and their internal L1 cache
    * @group composite_def
    */
  class ARMCore(armName: Symbol) extends Composite(armName) {

    /** Enable to provide the name implicitly
      * @param implicitName
      *   the name of the object/class inheriting from this class will be the
      *   name of composite
      */
    def this()(implicit implicitName: Name) = {
      this(implicitName.value)
    }

    /** Initiator modelling an ARM Core
      * @group initiator
      */
    val core: Initiator = Initiator()

    /** Transporter modelling the cache of the core
      * @group target
      */
    val cache: Target = Target()

    // ARM access to its private L1 cache
    core link cache

  }

  /* -----------------------------------------------------------
   * Global components
   * ----------------------------------------------------------- */

  /** Composite modelling ARM0
    * @group composite
    */
  val ARM0 = new ARMCore()

  /** Composite modelling ARM1
    * @group composite
    */
  val ARM1 = new ARMCore()

  /** Initiator modelling ethernet peripheral
    * @group initiator
    */
  val eth: Initiator = Initiator()

  /** Transporter modelling AXI bus
    * @group transporter
    */
  val axi_bus: SimpleTransporter = SimpleTransporter()

  /** Target modelling SPI peripheral
    * @group target
    */
  val spi: Target = Target()

  /** Target modelling MPIC peripheral
    * @group target
    */
  val mpic: Target = Target()

  /** Target modelling SPI registers
    * @group target
    */
  val spi_reg: Target = Target()

  /** Target modelling DMA registers
    * @group target
    */
  val dma_reg: Target = Target()

  /** Target modelling external DDR
    * @group target
    */
  val ddr: Target = Target()

  /* -----------------------------------------------------------
   * Physical connections
      ----------------------------------------------------------- */

  // Each ARM core is connected to the internal interconnect
  ARM0.core link axi_bus
  ARM1.core link axi_bus

  ARM0.core link TeraNet.config_bus
  ARM1.core link TeraNet.config_bus

  // Eth connection to internal interconnect
  eth link TeraNet.periph_bus

  // Peripheral bus connections
  TeraNet.periph_bus link MemorySubsystem.msmc

  TeraNet.periph_bus link spi

  // Accesses to peripherals
  TeraNet.config_bus link dma_reg
  TeraNet.config_bus link spi_reg

  // Accesses to config registers
  axi_bus link mpic
  axi_bus link MemorySubsystem.msmc

  // MSMC connections
  MemorySubsystem.msmc link TeraNet.periph_bus

  MemorySubsystem.ddr_ctrl link ddr

  // DMA connections
  dma link TeraNet.periph_bus

}
