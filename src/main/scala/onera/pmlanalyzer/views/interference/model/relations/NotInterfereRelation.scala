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
import onera.pmlanalyzer.pml.model.relations.{AntiReflexiveSymmetricEndomorphism, Relation}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import sourcecode.Name

private[pmlanalyzer] final case class BasicNotInterfereRelation[
    L,
    R
] private[pmlanalyzer] (
    iniValues: Map[L, Set[R]]
)(using
    n: Name
) extends Relation[L, R](iniValues)

private[pmlanalyzer] final case class NotInterfereEndomorphism[
    L
] private[pmlanalyzer] (
    iniValues: Map[L, Set[L]]
)(using
    n: Name
) extends AntiReflexiveSymmetricEndomorphism[L](iniValues)

private[pmlanalyzer] object NotInterfereRelation {

  type NotInterfereRelation[L, R] = BasicNotInterfereRelation[L, R] |
    NotInterfereEndomorphism[L]

  trait Instances {

    /** Relation gathering user defined service non-interference caused by a
      * transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdNotInterfereWithService
        : BasicNotInterfereRelation[AtomicTransactionId, Service] =
      BasicNotInterfereRelation(Map.empty)

    /** Relation gathering user defined service non-interferences
      * @group interfere_relation
      */
    final implicit val serviceNotInterfere: NotInterfereEndomorphism[Service] =
      NotInterfereEndomorphism(
        Map.empty
      )

    /** Relation gathering user defined non-interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareNotInterfere
        : NotInterfereEndomorphism[Hardware] = NotInterfereEndomorphism(
      Map.empty
    )
  }
}
