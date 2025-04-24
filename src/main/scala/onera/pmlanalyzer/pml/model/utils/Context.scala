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

package onera.pmlanalyzer.pml.model.utils
import onera.pmlanalyzer.pml.model.PMLNodeMap
import onera.pmlanalyzer.pml.model.relations.*

/** Trait gathering all relation instances
 */
abstract class Context
    extends LinkRelation.Instances
    with UseRelation.Instances
    with ProvideRelation.Instances
    with AuthorizeRelation.Instances
    with RoutingRelation.Instances
    with PMLNodeMap.Instances

object Context {

  final class EmptyContext
      extends Context
      with LinkRelation.EmptyInstances
      with UseRelation.EmptyInstances
      with ProvideRelation.EmptyInstances
      with AuthorizeRelation.EmptyInstances
      with RoutingRelation.EmptyInstances
      with PMLNodeMap.EmptyInstances
}
