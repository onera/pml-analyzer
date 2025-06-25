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

import onera.pmlanalyzer.pml.examples.components.io.Uart
import onera.pmlanalyzer.pml.examples.components.memory.{CadenceDdrSdramController, DdrSdram}
import onera.pmlanalyzer.pml.examples.riscv.FU740.components.U74CoreComplex
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.{Load, Store}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Simple model of the SiFive FU740 SoC. */
class FU740Platform(
                     name: Symbol,
                     u74CoreCnt: Int,
                     sdramInputPortCnt: Int,
                     l2Partitioned: Boolean
) extends Platform(name) {

  /**
    * Enable to provide the name implicitly
    * @param implicitName the name of the object/class inheriting from this class
    *                     will be the name of platform
    */
  def this(
            u74CoreCnt: Int,
            sdramInputNb: Int,
            l2Partitioned: Boolean
  )(implicit implicitName: Name) = {
    this(Symbol(implicitName.value), u74CoreCnt, sdramInputNb, l2Partitioned)
  }

  /* -----------------------------------------------------------
   * Global components
   * ----------------------------------------------------------- */
  // FIXME Revert to N banks when we get a way to tag multiple paths, due to uncertainty on which bank has been traversed, as valid
  val cache_banks: Int = 1
  val channels_nb: Int = 4
  val ddr_banks_nb: Int = 4
  val ddr_gp_banks_nb: Int = 2

  val u74_cluster = new U74CoreComplex(
    "Cluster0",
    u74CoreCnt,
    channels_nb,
    cache_banks,
    l2Partitioned
  )
  val uart = new Uart()
  val ddr_ctrl = new CadenceDdrSdramController(sdramInputPortCnt)
  val ddr = new DdrSdram(ddr_banks_nb, ddr_gp_banks_nb, 1)

  /* -----------------------------------------------------------
   * Physical connections
  ----------------------------------------------------------- */

  // Connections to DDR controller
  ddr_ctrl.input_ports foreach (u74_cluster.mem_bus link _)

  // Connection between DDR controller and DDR memory device
  ddr_ctrl.scheduler link ddr.phy

  // Connections to peripherals
  u74_cluster.peripheral_tl_switch_1 link uart.slave_port
}
