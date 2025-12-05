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

import onera.pmlanalyzer.*
import sourcecode.{File, Line, Name}

/** Simple model of the Keystone platform illustrating the main features of PML.
  * The components of the architectures can be created using the constructors
  * provided in [[pml.model.hardware.BaseHardwareNodeBuilder]] For instance the
  * [[MyProcPlatform.dma]] is built with:
  * {{{val dma: Initiator = Initiator()}}} An axi-bus is built with:
  * {{{val axi-bus: SimpleTransporter= SimpleTransporter()}}} A peripheral or a
  * memory is built with {{{val sram: Target = Target()}}} Some components may
  * be composite, if you want to define one instance of composite you can use
  * the object instantiation pattern used for the [[MyProcPlatform.TeraNet]]
  * {{{object TeraNet extends Composite}}} You can also define a type of
  * Composite that may be instantiated afterward, for instance here ARMCores are
  * defined as a composition of a core (initiator) and a cache (transporter). A
  * name is given as a parameter of the [[MyProcPlatform.ARMCore]] class. If you choose to do so please
  * note that you must use specific design patterns to ensure the correct naming of
  * inner components. The default constructor of your class MUST take as inputs
  * the name and the reflexive info. If your class is final (cannot be further refined)
  * then you can define an alternative constructor where the name and reflexive info are
  * deduced from the instantiation context as illustrated in [[MyProcPlatform.ARMCore]].
  * {{{final class ARMCore (name:Symbol) extends Composite}}} Then components can be
  * linked together, this operation simply connect the service of the same type
  * provided by the two components. For instance {{{ARM0.core link axi_bus}}}
  * links the [[pml.model.service.Load]] and [[pml.model.service.Store]] service
  * of the ARM0.core to the ones of the axi-bus Beware that all links are not
  * possible, for instance you cannot link two [[pml.model.hardware.Target]] or
  * a [[pml.model.hardware.Composite]] to another component.
 *
 * @see
  *   [[Link.Ops]] for link operator definition
  * @see
  *   [[pml.model.hardware.BaseHardwareNodeBuilder]] for component instantiation
 * @note
 * The [[file]] and [[line]] enable source code traceability for PML Nodes.
 * Note that these parameters are not explicitly provided by the used thanks to an alternative constructor for [[MyProcPlatform]]
 * where the name, line and file are derived implicitly (using keyword in the alternative constructor).
 * In this case we chose to remove access from the default constructor (private keyword)
 * @param name the name of the platform name of the final object merging all facets of the model
 * @param line the line where an instance of the this class will be defined
 * @param file the file in which an instance of this class will be defined
 */
class MyProcPlatform private (name: Symbol, line: Line, file: File)
    extends Platform(name, line, file) {

  /** Enable to provide the name implicitly
   *
   * @param givenName the name of the platform derived by sourcecode package
   * @param givenLine the line of the platform instantiation derived by sourcecode package
   * @param givenFile the file of the platform instantiation derived by sourcecode package
   */
  def this()(using givenName: Name, givenLine: Line, givenFile: File) = {
    this(Symbol(givenName.value), givenLine, givenFile)
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
  object MemorySubsystem extends Composite("MemorySubSystem") {

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
   *
   * @group composite_def
   * @note
   * The armInfo enables source code traceability for PML Nodes.
   * Note that these parameters are not explicitly provided by the used thanks to an alternative constructor for [[ARMCore]]
   * where the name, line and file are derived implicitly (using keyword in the alternative constructor).
   * In this case we chose to remove access from the default constructor (private keyword)
   * @param armName the name of the ARMCore instance of this class
   * @param armInfo structure containing source code traceability information
   */
  final class ARMCore private (
      armName: Symbol,
      armInfo: ReflexiveInfo,
      context: Context
  ) extends Composite(armName, armInfo, context) {

    /** Enable to provide the name implicitly
     *
     * @param givenName the name of the composite derived by sourcecode package
     * @param givenInfo structure containing source code traceability information derived by sourcecode package
     */
    def this()(implicit
        givenName: Name,
        givenInfo: ReflexiveInfo,
        givenContext: Context
    ) = {
      this(givenName.value, givenInfo, givenContext)
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

  // Accesses to config registers
  axi_bus link mpic
  axi_bus link MemorySubsystem.msmc

  // MSMC connections
  MemorySubsystem.msmc link TeraNet.periph_bus

  MemorySubsystem.ddr_ctrl link ddr

  // DMA connections
  dma link TeraNet.periph_bus

}
