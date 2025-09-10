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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransaction,
  AtomicTransactionId
}

trait Transform[L, R] {
  def apply(l: L): R
}

object Transform {

  trait BasicInstances {
    self: Platform =>

    given [U, V](using t: Transform[U, Option[V]]): Transform[U, Set[V]] with {
      def apply(l: U): Set[V] =
        t.apply(l) match {
          case Some(value) => Set(value)
          case None        => Set.empty
        }
    }

    /** Convert a physical id to the corresponding path of services
      * @group transform_operator
      */
    given Transform[AtomicTransactionId, Option[AtomicTransaction]] with {
      def apply(l: AtomicTransactionId): Option[AtomicTransaction] =
        atomicTransactionsByName.get(l)
    }

    /** Convert an application to the set of transaction id its trigger
      * @group transform_operator
      */
    given Transform[Application, Set[AtomicTransactionId]] with {
      def apply(l: Application): Set[AtomicTransactionId] =
        atomicTransactionsBySW.getOrElse(l, Set.empty)
    }
  }

  trait TransactionLibraryInstances {
    self: TransactionLibrary & Platform =>

    /** Convert a user defined transaction to the set of its atomic transaction ids
      * @group transform_operator
      */
    given Transform[Transaction, Set[AtomicTransactionId]] with {
      def apply(l: Transaction): Set[AtomicTransactionId] =
        transactionByUserName.getOrElse(l.userName, Set.empty)
    }

    /** Convert a user transaction id to the set of its atomic transaction ids
      * @group transform_operator
      */
    given Transform[UserTransactionId, Set[AtomicTransactionId]] with {
      def apply(l: UserTransactionId): Set[AtomicTransactionId] =
        transactionByUserName.getOrElse(l, Set.empty)
    }
  }
}
