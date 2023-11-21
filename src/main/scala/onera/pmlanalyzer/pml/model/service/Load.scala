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

package onera.pmlanalyzer.pml.model.service

/**
  * Base class for load services
  * @see the possible constructors are provided by [[BaseServiceBuilder]]
  * @param name the name of the node
  * @group service_class
  */
final class Load private(val name: Symbol) extends Service

/**
  * Builder of loads
  * @group builder
  */
object Load extends BaseServiceBuilder[Load] {

  /**
    * Direct builder from name
    * @param name the name of the object
    *  @return the object
    */
  protected def builder(name: Symbol): Load = new Load(name)

}
