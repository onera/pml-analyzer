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

package keystone.pml

import onera.pmlanalyzer.*

/**
  * Model of the Keystone TCI6630K2L from Texas Instruments. This
  * platform is composed of:
  * (1) an eight C66 DSP pack, in which each core comes with dedicated L1 and L2 caches, and a memory
  *     extension and protection unit (MPAX);
  * (2) a four arm pack, in which each core comes with dedicated L1 caches, and a memory
  *     management unit (MMU);
  * (3) a central memory system that gives access to the platform’s SRAM (MSMC SRAM), and an external DDR.
  *     The memory access management is performed by the Multicore Shared Memory Controller (MSMC);
  * (4) a set of IO peripherals (e.g. GPIO, UART), and utility peripherals (e.g. Boot, Semaphores);
  * (5) a memory transfer peripheral (EDMA); (6) an ultra speed bus (TeraNet) connecting the peripherals,
  *     the memories, and the cores
  *
  * @param name name of the platform
  */

class KeystonePlatform(name: Symbol) extends Platform(name) {

  def this()(implicit implicitName: sourcecode.Name) = {
    this(Symbol(implicitName.value))
  }

  // central DMA
  val EDMA: Initiator = Initiator()

  /** -----------------------------------------------------------
    * Composite models
    * ----------------------------------------------------------- */

  // Composite representing Keystone CorePacs (dsp, sram, mpax)
  class CorePac(corePacName: Symbol) extends Composite(corePacName) {
    val dsp: Initiator = Initiator()
    val dsram: Target = Target()
    val isram: Target = Target()
    val mpax: Virtualizer = Virtualizer()

    // DSP can access to its MPAX and private SRAM
    dsp link mpax
    dsp link dsram
    dsp link isram
  }

  /** -----------------------------------------------------------
    * Global components
      ----------------------------------------------------------- */

  // 2 Banked MSMC SRAM
  case object MSMC_SRAM extends Composite("MSMC_SRAM") {
    val banks: IndexedSeq[Target] = (0 to 1).map(i => Target(s"Bank$i"))
  }

  // 2 Banked DDR
  case object DDR extends Composite("DDR") {
    val banks: IndexedSeq[Target] = (0 to 1).map(i => Target(s"Bank$i"))
  }

  // MSMC managing access to the external DDR and internal SRAM
  val MSMC: SimpleTransporter = SimpleTransporter()

  // Interconnect routing transaction between memory, DSP, ARM and peripherals
  val TeraNet: SimpleTransporter = SimpleTransporter()

  // PCIe peripheral
  val PCIe: Target = Target()

  // SPI peripheral
  val SPI: Target = Target()

  // Composite representing the single ARM Pac constituted of four ARM cores, the shared L2 cache and the MMU
  case object ARMPac extends Composite("ARMPac") {

    // Composite representing Keystone ARM cores and their internal L1 cache
    class ARMCore(armCoreName: Symbol) extends Composite(armCoreName) {
      val core: Initiator = Initiator()
      val L1: SimpleTransporter = SimpleTransporter()
      val mmu: Virtualizer = Virtualizer()

      // ARM access to its private L1 and MMU cache
      core link L1
      core link mmu
    }

    val cores: IndexedSeq[ARMCore] = 0 until 4 map (i => new ARMCore(s"ARM$i"))
  }

  val EDMARegister: Target = Target()

  // AXI controller used by ARMs to access directly to MSMC
  val AXI: SimpleTransporter = SimpleTransporter()

  // Consider eight DSP
  val corePacs: IndexedSeq[CorePac] =
    0 until 8 map (i => new CorePac(s"CorePac$i"))

  /** -----------------------------------------------------------
    * Physical connections
    * ----------------------------------------------------------- */

  // DSP MPAX is connected to the MSMC and the Teranet
  // DSP SRAM can also be accessed via Teranet
  corePacs.foreach { pac =>
    {
      pac.mpax link MSMC
      pac.mpax link TeraNet
      TeraNet link pac.dsram
    }
  }

  // DDR and internal MSMC SRAM banks are accessed through the MSMC
  DDR.banks foreach { bank => MSMC link bank }
  MSMC_SRAM.banks foreach { bank => MSMC link bank }

  // TeraNet is linked to MSMC, peripherals, DMA
  TeraNet link MSMC
  TeraNet link PCIe
  EDMA link TeraNet
  TeraNet link EDMARegister
  TeraNet link SPI

  // ARM MMU can access to the AXI or TeraNet directly
  ARMPac.cores foreach { _.mmu link AXI }
  ARMPac.cores foreach { _.mmu link TeraNet }

  // AXI controlled can access to the MSMC
  AXI link MSMC
}
