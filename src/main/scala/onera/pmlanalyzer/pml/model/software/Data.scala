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

import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder, PMLNodeMap}
import sourcecode.{File, Line, Name}

/** Util class to represent a data owned by a target
  * @see
  *   the possible constructors are provided by [[BaseSoftwareNodeBuilder]]
  * @param name
  *   the name of the data
  * @group software_class
  */
final class Data private (val name: Symbol, info: ReflexiveInfo)
    extends Software(info) {

  override def toString: String = name.name
}

/** Builder of [[Data]]
  * @group builder
  */
object Data extends BaseSoftwareNodeBuilder[Data] {

  /** Direct builder from name
   * @param name
   *   the name of the object
   * @return
   *   the object
   */
  protected def builder(
      name: Symbol
  )(using givenInfo: ReflexiveInfo): Data =
    new Data(name, givenInfo)
}
