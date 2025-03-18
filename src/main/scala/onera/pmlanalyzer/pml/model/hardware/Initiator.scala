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

package onera.pmlanalyzer.pml.model.hardware

import sourcecode.{File, Line}

/** Class for the smart initiator, i.e. that can initiate transactions
 *
 * @see
  *   the possible constructors are provided by [[BaseHardwareNodeBuilder]]
  * @param name
  *   the name of the node
  * @group initiator_class
  */
final class Initiator private(val name: Symbol, line: Line, file: File) extends Hardware(line, file)

/** Builder of initiators
  * @group builder
  */
object Initiator extends BaseHardwareNodeBuilder[Initiator] {

  /** Direct builder from initiator name
    * @param name
    *   the name of the object
    * @return
    *   the object
    */
  protected def builder(name: Symbol)(using line: Line, file: File): Initiator = new Initiator(name, line, file)

}
