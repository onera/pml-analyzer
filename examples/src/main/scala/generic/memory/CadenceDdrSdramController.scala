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

package generic.memory

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.*
import sourcecode.Name

class CadenceDdrSdramController(
    inputPortCnt: Int,
    name: Symbol,
    ctrlInfo: ReflexiveInfo,
    ctrlContext: Context
) extends Composite(name, ctrlInfo, ctrlContext) {

  def this(_inputPortCnt: Int)(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(_inputPortCnt, Symbol(givenName.value), givenInfo, givenContext)
  }

  // Transporter modelling the configurable input ports
  val input_ports: Seq[SimpleTransporter] =
    for (i <- 0 until inputPortCnt)
      yield SimpleTransporter(s"S$i")

  // Transporter modelling the DDR controller
  val arbiter: SimpleTransporter = SimpleTransporter()
  val scheduler: SimpleTransporter = SimpleTransporter()
  val cmd_queue: SimpleTransporter = SimpleTransporter()

  // Internal connections from input ports
  for (port <- input_ports) {
    port link arbiter
  }

  // Memory controller sub-units
  arbiter link cmd_queue
  cmd_queue link scheduler
}
