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

package onera.pmlanalyzer.pml.model

import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import sourcecode.{Enclosing, File, Line}

import scala.language.implicitConversions

/** Base class for all PML Node
  */
private[pmlanalyzer] abstract class PMLNode private[pml] (info: ReflexiveInfo)(
    using _enclosing: Enclosing
) extends SourceCodeTraceable {

  val owner: Owner = info.owner

  val lineInFile: Int = info.line.value

  val sourceFile: String =
    info.file.value.split('.').init.mkString(java.io.File.separator)

  /** Name of the node
    *
    * @group identifier
    */
  val name: Symbol

  /** Name of the type of PML node
    *
    * @group identifier
    */
  final val typeName: Symbol = Symbol(
    _enclosing.value.split(' ').head.split('.').last
  )

  /** Print a node only by its [[name]]
    * @group printer_function
    * @return
    *   string representation of a PMLNode
    */
  override def toString: String = name.name
}
