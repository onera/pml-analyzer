/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.views.interference.model.relations

import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.pml.model.relations.Relation
import onera.pmlanalyzer.pml.model.service.Service
import sourcecode.Name
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

final case class NotInterfereRelation[L, R] private (iniValues: Map[L, Set[R]])(
    using n: Name
) extends Relation[L, R](iniValues)

object NotInterfereRelation {
  trait Instances {

    /** Relation gathering user defined service non-interference caused by a
      * transaction
      * @group interfere_relation
      */
    final implicit val physicalTransactionIdNotInterfereWithService
        : NotInterfereRelation[PhysicalTransactionId, Service] =
      NotInterfereRelation(Map.empty)

    /** Relation gathering user defined service non-interferences
      * @group interfere_relation
      */
    final implicit val serviceNotInterfere
        : NotInterfereRelation[Service, Service] = NotInterfereRelation(
      Map.empty
    )

    /** Relation gathering user defined non-interfering hardware
      * @group interfere_relation
      */
    final implicit val hardwareNotInterfere
        : NotInterfereRelation[Hardware, Hardware] = NotInterfereRelation(
      Map.empty
    )
  }
}
