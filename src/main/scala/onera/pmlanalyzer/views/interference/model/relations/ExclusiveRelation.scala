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

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.relations.{
  AntiReflexiveSymmetricEndomorphism,
  Endomorphism
}
import onera.pmlanalyzer.pml.model.software.Application
import sourcecode.Name
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalAtomicTransactionId

final case class ExclusiveRelation[A] private (iniValues: Map[A, Set[A]])(using
    n: Name
) extends AntiReflexiveSymmetricEndomorphism[A](iniValues)

object ExclusiveRelation {
  trait GeneralInstances {

    /** Relation gathering user defined exclusive transactions
      * @group exclusive_relation
      */
    final implicit val transactionExclusive
        : ExclusiveRelation[PhysicalAtomicTransactionId] = ExclusiveRelation(
      Map.empty
    )
  }
  trait LibraryInstances {

    /** Relation gathering user defined exclusive scenarios
      * @group exclusive_relation
      */
    final implicit val userScenarioExclusive
        : ExclusiveRelation[UserScenarioId] = ExclusiveRelation(Map.empty)
  }
  trait ApplicationInstances {

    /** Relation gather user defined exclusive software
      * @group exclusive_relation
      */
    final implicit val swExclusive: ExclusiveRelation[Application] =
      ExclusiveRelation(Map.empty)
  }
}
