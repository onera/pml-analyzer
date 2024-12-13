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

package onera.pmlanalyzer.pml.examples.simpleT1042

import onera.pmlanalyzer.pml.model.hardware._
import onera.pmlanalyzer.pml.operators._
import sourcecode.Name

class SimpleT1042Platform(name: Symbol) extends Platform(name) {

  // DMA
  val dma: Initiator = Initiator()

  // 2 Cached Cores
  val C1 = new CachedCore()
  val C2 = new CachedCore()
  // Ethernet initiator
  val eth: Initiator = Initiator()

  /* -----------------------------------------------------------
   * Global components
   * ----------------------------------------------------------- */

  // Composite representing cores and their internal L1 cache
  class CachedCore(coreName: Symbol) extends Composite(coreName) {

    def this()(implicit implicitName: Name) = {
      this(implicitName.value)
    }

    val core: Initiator = Initiator()
    val L1: Target = Target()
    val cpu: Target = Target()

    // ARM access to its private L1 cache
    core link L1
    core link cpu

  }

  // Interconnect
  val bus: SimpleTransporter = SimpleTransporter()

  // Register configuration bus
  val config_bus: SimpleTransporter = SimpleTransporter()

  // Memories peripheral
  val mem1: Target = Target()
  val mem2: Target = Target()

  // PCIe peripheral
  val pcie: Target = Target()
  // MPIC peripheral
  val mpic: Target = Target()
  // PCIe_reg peripheral
  val pcie_reg: Target = Target()
  // DMA_reg peripheral
  val dma_reg: Target = Target()

  /* -----------------------------------------------------------
   * Physical connections
      ----------------------------------------------------------- */

  // Each ARM core is connected to the internal interconnect
  C1.core link bus
  C2.core link bus

  // Eth connection to internal interconnect
  eth link bus

  // Memory connections
  bus link mem1
  bus link mem2

  // Interconnect to configuration bu
  bus link config_bus

  // Accesses to peripherals
  config_bus link dma_reg
  config_bus link pcie_reg
  config_bus link mpic

  // DMA connections
  dma link pcie
  dma link bus
}
