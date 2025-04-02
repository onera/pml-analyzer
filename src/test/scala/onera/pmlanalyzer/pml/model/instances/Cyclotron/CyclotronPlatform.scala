/** *****************************************************************************
 * Copyright (c) 2023. ONERA This file is part of PML Analyzer
 *
 * PML Analyzer is free software ; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation ; either version 2 of the License, or (at your
 * option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program ; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package onera.pmlanalyzer.pml.model.instances.Cyclotron

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.{File, Line, Name}

class CyclotronPlatform(name: Symbol, line: Line, file: File)
    extends Platform(name, line, file) {
  def this()(using givenName: Name, givenLine: Line, givenFile: File) = {
    this(Symbol(givenName.value), givenLine, givenFile)
  }

  object group extends Composite("group") {
    val L0: SimpleTransporter = SimpleTransporter()
    val L1: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()
    val other_port: SimpleTransporter = SimpleTransporter()

    L1 link L0
    L0 link L1
    other_port link L0
  }

  val dma: Initiator = Initiator()
  val eth: Target = Target()
  dma link eth
}
