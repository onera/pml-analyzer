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

package generic.io

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.*
import sourcecode.Name

/** Universal Asynchronous Receiver Transmitter (UART)
 *
 * @group composite_def
 */
class Uart(name: String, uartInfo: ReflexiveInfo, uartContext: Context)
    extends Composite(Symbol(name), uartInfo, uartContext) {

  /**
   * Enable to provide the name implicitly
   *
   * @param givenName the name of the object/class inheriting from this class
   *                     will be the name of composite
   */
  def this()(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(givenName.value, givenInfo, givenContext)
  }

  /** Transporter modelling the UART controller
   *
   * @group transporter */
  val ctrl: SimpleTransporter = SimpleTransporter()

  /** Targets modelling the FIFOs for TX and RX
   *
   * @group transporter */
  val tx_fifo: Target = Target()
  val rx_fifo: Target = Target()

  /** Transporter modelling the slave port
   *
   * @group transporter */
  val slave_port: SimpleTransporter = SimpleTransporter()

  /** Controller interrupt registers
   *
   * @group target */
  val ctrl_interrupt_regs: Target = Target()

  // SPI connections
  slave_port link ctrl
  ctrl link tx_fifo
  ctrl link rx_fifo
  ctrl link ctrl_interrupt_regs
}
