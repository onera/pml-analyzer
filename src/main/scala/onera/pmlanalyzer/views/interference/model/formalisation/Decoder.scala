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
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*

private[pmlanalyzer] trait Decoder {
  val system: TopologicalInterferenceSystem
  val variables: Set[MLit]
  val graph: MGraph
  val nodeToTransaction: Map[MNode, Set[PhysicalTransactionId]]
  val nodeToServices: Map[MNode, Set[Symbol]]
  val litToNode: Map[MLit, Set[MNode]]

  def decodeTrivialSolutions(implm: SolverImplm): Seq[
    (
        Boolean,
        Set[Set[PhysicalTransactionId]],
        Map[Set[PhysicalTransactionId], Set[Set[UserTransactionId]]]
    )
  ]

  def decodeModel(
      model: Set[MLit],
      modelIsFree: Boolean,
      implm: SolverImplm
  ): Set[Set[PhysicalTransactionId]]

  def decodeChannel(model: Set[PhysicalTransactionId]): Set[Symbol] = {
    if (model.size > system.maxSize)
      Set.empty
    else
      nodeToTransaction.keySet
        .filter(k => model.intersect(nodeToTransaction(k)).size >= 2)
        .flatMap(nodeToServices)
  }

  def decodeUserModel(
      physicalModel: Set[PhysicalTransactionId]
  ): Set[Set[UserTransactionId]] = {
    if (physicalModel.isEmpty)
      Set.empty
    else if (physicalModel.size > system.maxSize)
      Set.empty
    else {
      system.transactionUserNameOpt match {
        case Some(transactionUserName) => {
          val transaction = system.idToTransaction.view
            .filterKeys(physicalModel)
            .toMap

          val userNames = transaction.view
            .mapValues(transactionUserName)
            .toMap
            .transform((k, v) =>
              if (v.isEmpty) Set(UserTransactionId(k.id)) else v
            )

          val results = userNames.values.tail
            .foldLeft(userNames.values.head.map(n => Set(n)))((acc, names) =>
              for {
                p <- acc
                last <- system.finalUserTransactionExclusiveOpt match {
                  case Some(finalUserTransactionExclusive) =>
                    val x = names.filter(id =>
                      (finalUserTransactionExclusive(id) - id).intersect(p).isEmpty
                    )
                    x
                  case _ => names
                }
              } yield {
                p + last
              }
            )
          results
        }
        case None => Set(physicalModel.map(s => UserTransactionId(s.id)))
      }
    }
  }
}
