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

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import sourcecode.{File, Line}

/** Class modelling simple transporters.
 *
 * @see
  *   the possible constructors are provided by [[BaseHardwareNodeBuilder]]
  * @param name
  *   the name of the node
  * @group transporter_class
  */
final class SimpleTransporter private (val name: Symbol, info: ReflexiveInfo)
    extends Transporter(info)

/** Builder of simple transporters
  * @group builder
  */
object SimpleTransporter extends BaseHardwareNodeBuilder[SimpleTransporter] {

  /** Direct builder from name
    * @param name
    *   the name of the object
    * @return
    *   the object
    */
  protected def builder(
      name: Symbol
  )(using givenInfo: ReflexiveInfo): SimpleTransporter =
    new SimpleTransporter(name, givenInfo)

}
