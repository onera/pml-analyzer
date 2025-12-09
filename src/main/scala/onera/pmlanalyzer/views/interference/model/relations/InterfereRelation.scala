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
import onera.pmlanalyzer.pml.model.relations.{Endomorphism, Relation}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId

private[pmlanalyzer] final case class BasicInterfereRelation[
    L,
    R
] private[pmlanalyzer] (
    iniValues: Map[L, Set[R]]
) extends Relation[L, R](iniValues)

private[pmlanalyzer] final case class InterfereEndomorphism[
    L
] private[pmlanalyzer] (
    iniValues: Map[L, Set[L]]
) extends Endomorphism[L](iniValues)

private[pmlanalyzer] object InterfereRelation {

  type InterfereRelation[L, R] = BasicInterfereRelation[L, R] |
    InterfereEndomorphism[L]

  trait Instances {

    /** Relation gathering user defined service interferences caused by a
      * transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdInterfereWithService
        : BasicInterfereRelation[AtomicTransactionId, Service] =
      BasicInterfereRelation(Map.empty)

    /** Relation gathering user defined service interferences
      * @group interfere_relation
      */
    final implicit val serviceInterfere: InterfereEndomorphism[Service] =
      InterfereEndomorphism(Map.empty)

    /** Relation gathering user defined interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareInterfere: InterfereEndomorphism[Hardware] =
      InterfereEndomorphism(Map.empty)
  }
}
