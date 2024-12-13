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

package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.{
  UserScenarioId,
  UserTransactionId
}
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

private[operators] trait Transform[L, R] {
  def apply(l: L): R
}

object Transform {

  trait BasicInstances {
    self: Platform =>

    /** Convert a physical id to the corresponding path of services
      * @group transform_operator
      */
    given Transform[PhysicalTransactionId, Option[PhysicalTransaction]] with {
      def apply(l: PhysicalTransactionId): Option[PhysicalTransaction] =
        transactionsByName.get(l)
    }

    /** Convert an application to the set of transaction id its trigger
      * @group transform_operator
      */
    given Transform[Application, Set[PhysicalTransactionId]] with {
      def apply(l: Application): Set[PhysicalTransactionId] =
        transactionsBySW.getOrElse(l, Set.empty)
    }
  }

  trait TransactionLibraryInstances {
    self: TransactionLibrary & Platform =>

    /** Convert a user transaction to its physical transaction id
      * @group transform_operator
      */
    given Transform[Transaction, Option[PhysicalTransactionId]] with {
      def apply(l: Transaction): Option[PhysicalTransactionId] =
        transactionByUserName.get(l.userName)
    }

    /** Convert a user defined scenario to the set of its physical scenario ids
      * @group transform_operator
      */
    given Transform[Scenario, Set[PhysicalTransactionId]] with {
      def apply(l: Scenario): Set[PhysicalTransactionId] =
        scenarioByUserName.getOrElse(l.userName, Set.empty)
    }

    /** Convert a user transaction id to its physical transaction id
      * @group transform_operator
      */
    given Transform[UserTransactionId, Option[PhysicalTransactionId]] with {
      def apply(l: UserTransactionId): Option[PhysicalTransactionId] =
        transactionByUserName.get(l)
    }

    /** Convert a user scenario id to the set of its physical scenario ids
      * @group transform_operator
      */
    given Transform[UserScenarioId, Set[PhysicalTransactionId]] with {
      def apply(l: UserScenarioId): Set[PhysicalTransactionId] =
        scenarioByUserName.getOrElse(l, Set.empty)
    }
  }
}
