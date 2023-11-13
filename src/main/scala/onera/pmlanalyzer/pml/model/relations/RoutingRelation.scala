/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.pml.model.hardware.Initiator
import onera.pmlanalyzer.pml.model.service.Service
import sourcecode.Name

/**
  * relation used to encode the routing constraints
  *
  * @param iniValues initial values of the relation
  * @tparam L the left type
  * @tparam R the right type
  */
case class RoutingRelation[L, R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L, R](iniValues)

object RoutingRelation {
  /**
    * Instances of routing relations
    */
  trait Instances {

    /**
      * Relation gathering routing constraints
      * @group route_relation
      */
    final implicit val InitiatorRouting: RoutingRelation[(Initiator, Service, Service), Service] = RoutingRelation(Map.empty)
  }
}
