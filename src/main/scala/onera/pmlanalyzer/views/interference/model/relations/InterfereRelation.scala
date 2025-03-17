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

package onera.pmlanalyzer.views.interference.model.relations

import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.pml.model.relations.Relation
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

final case class InterfereRelation[L, R] private (iniValues: Map[L, Set[R]])
    extends Relation[L, R](iniValues)

//FIXME TO ENSURE CORRECTNESS THE INTERFERE ENDOMORPHISMS SHOULD BE ANTI-REFLEXIVE AND SYMMETRIC TO BE
//  CONSISTENT WITH INTERFERENCE SPECIFICATION BASE TRAIT
object InterfereRelation {
  trait Instances {

    /** Relation gathering user defined service interferences caused by a
      * transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdInterfereWithService
        : InterfereRelation[PhysicalTransactionId, Service] = InterfereRelation(
      Map.empty
    )

    /** Relation gathering user defined service interferences
      * @group interfere_relation
      */
    final implicit val serviceInterfere: InterfereRelation[Service, Service] =
      InterfereRelation(Map.empty)

    /** Relation gathering user defined interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareInterfere
        : InterfereRelation[Hardware, Hardware] = InterfereRelation(Map.empty)
  }
}
