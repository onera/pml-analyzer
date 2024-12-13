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

package onera.pmlanalyzer.pml.model.software

import onera.pmlanalyzer.pml.model.utils.Owner
import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder}
import sourcecode.Name

/** Util class to represent a data owned by a target
  * @see
  *   the possible constructors are provided by [[BaseSoftwareNodeBuilder]]
  * @param name
  *   the name of the data
  * @group software_class
  */
final class Data private (val name: Symbol) extends PMLNode {

  override def toString: String = name.name
}

/** Builder of [[Data]]
  * @group builder
  */
object Data extends PMLNodeBuilder[Data] {

  /** A data is defined by its name
    * @param name
    *   the name of the data
    * @param owner
    *   the implicit name of the platform
    * @return
    *   the data
    */
  def apply(name: Symbol)(implicit owner: Owner): Data = {
    _memo.getOrElseUpdate((owner.s, name), new Data(name))
  }

  /** A data can be defined by the implicit name used during the definition
    * (name of the variable enclosing the object)
    * @param name
    *   the implicit name of the data
    * @param owner
    *   the implicit name of the platform
    * @return
    *   the data
    */
  def apply()(implicit name: Name, owner: Owner): Data =
    apply(Symbol(name.value))
}
