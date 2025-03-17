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

package onera.pmlanalyzer.pml.model.software

import onera.pmlanalyzer.pml.model.PMLNode
import sourcecode.{File, Line}

/** Class for the application executed on initiators
  * @see
  *   the possible constructors are provided by [[BaseSoftwareNodeBuilder]]
  * @param name
  *   the name of the node
  * @group software_class
  */
final class Application(val name: Symbol)(implicit _line: Line, _file: File) extends PMLNode(_line, _file)

/** Builder of [[Application]]
  * @group builder
  */
object Application extends BaseSoftwareNodeBuilder[Application] {

  /** Direct builder from name
    * @param name
    *   the name of the object
    * @return
    *   the object
    */
  protected def builder(name: Symbol)(implicit _line: Line, _file: File): Application = new Application(name)

}
