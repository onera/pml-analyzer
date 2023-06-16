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

package views.interference.model.relations

import pml.model.hardware.Hardware
import pml.model.relations.Relation
import pml.model.service.Service
import sourcecode.Name
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

case class NotInterfereRelation[L,R] private(iniValues: Map[L, Set[R]])(using n:Name) extends Relation[L,R](iniValues)

object NotInterfereRelation{
  trait Instances {
    /**
      * Relation gathering user defined service non-interference caused by a transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdNotInterfereWithService: NotInterfereRelation[PhysicalTransactionId,Service] = NotInterfereRelation(Map.empty)

    /**
      * Relation gathering user defined service non-interferences
      * @group interfere_relation
      */
    final implicit val serviceNotInterfereWithService: NotInterfereRelation[Service, Service] = NotInterfereRelation(Map.empty)

    /**
      * Relation gathering user defined non-interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareNotExclusive: NotInterfereRelation[Hardware, Hardware] = NotInterfereRelation(Map.empty)
  }
}