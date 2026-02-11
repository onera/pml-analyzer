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

package onera.pmlanalyzer.views.interference.model.specification

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation

private[pmlanalyzer] trait ApplicativeTableBasedInterferenceSpecification
    extends TableBasedInterferenceSpecification
    with ExclusiveRelation.LibraryInstances {
  self: Platform & TransactionLibrary =>

  /** Relation encoding the exclusivity constraints over
    * [[onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId]] considered
    * by the user
    * @group exclusive_relation
    */
  final lazy val finalUserTransactionExclusive
      : Map[UserTransactionId, Set[UserTransactionId]] = {
    val exclusive = finalExclusive(purifiedTransactions.keySet)
    relationToMap(
      transactionByUserName.keySet,
      (l, r) =>
        l == r || (
          transactionId(transactionByUserName(l)) == transactionId(
            transactionByUserName(r)
          ) || userTransactionExclusive(l).contains(r)
            || exclusive(transactionId(transactionByUserName(l)))
              .contains(transactionId(transactionByUserName(r)))
            || transactionSW(l)
              .flatMap(sw => swExclusive.get(sw).getOrElse(Set.empty))
              .intersect(transactionSW(r))
              .nonEmpty
        )
    )
  }
}
