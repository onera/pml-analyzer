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

package onera.pmlanalyzer.views.interference.model.formalisation

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransaction,
  AtomicTransactionId,
  PhysicalTransaction,
  PhysicalTransactionId
}
import sourcecode.File

final case class TopologicalInterferenceSystem(
    atomicTransactions: Map[AtomicTransactionId, AtomicTransaction],
    idToTransaction: Map[PhysicalTransactionId, PhysicalTransaction],
    exclusiveWithATr: Map[AtomicTransactionId, Set[AtomicTransactionId]],
    exclusiveWithTr: Map[PhysicalTransactionId, Set[
      PhysicalTransactionId
    ]],
    interfereWith: Map[Service, Set[Service]],
    maxSize: Int,
    finalUserTransactionExclusiveOpt: Option[
      Map[UserTransactionId, Set[UserTransactionId]]
    ],
    transactionUserNameOpt: Option[
      Map[Set[AtomicTransactionId], Set[UserTransactionId]]
    ],
    platformName: String,
    platformSourceFile: Option[String]
)
