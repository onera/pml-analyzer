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
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransactionId,
  Path,
  PhysicalTransaction,
  PhysicalTransactionId
}
import onera.pmlanalyzer.views.interference.operators.PostProcess

import scala.io.BufferedSource

private[pmlanalyzer] final case class TopologicalInterferenceSystem(
    atomicTransactions: Map[AtomicTransactionId, Path[Symbol]],
    idToTransaction: Map[PhysicalTransactionId, PhysicalTransaction],
    exclusiveWithATr: Map[AtomicTransactionId, Set[AtomicTransactionId]],
    exclusiveWithTr: Map[PhysicalTransactionId, Set[
      PhysicalTransactionId
    ]],
    interfereWith: Map[Symbol, Set[Symbol]],
    maxSize: Int,
    finalUserTransactionExclusiveOpt: Option[
      Map[UserTransactionId, Set[UserTransactionId]]
    ],
    transactionUserNameOpt: Option[
      Map[Set[AtomicTransactionId], Set[UserTransactionId]]
    ],
    name: String,
    sourceFile: String
)

private[pmlanalyzer] object TopologicalInterferenceSystem {

  def apply(
      platformName: String,
      maxSize: Int,
      sourceFile: String
  ): Option[TopologicalInterferenceSystem] =
    for {
      atomicTransactions <- PostProcess.parseAtomicTransactionTable(
        platformName
      )
      idToTransaction <- PostProcess.parseTransactionTable(platformName)
      exclusiveWithATr <- PostProcess.parseAtomicTransactionInterfereTable(
        platformName
      )
      exclusiveWithTr <- PostProcess.parseTransactionInterfereTable(
        platformName
      )
      interfereWith <- PostProcess.parseServiceInterfereTable(platformName)
    } yield {
      val userTable =
        for {
          m <- PostProcess.parseUserTransactionTable(platformName)
        } yield {
          m.groupMapReduce(_._2)((k, _) => Set(k))(_ ++ _)
            .withDefaultValue(Set.empty)
        }

      TopologicalInterferenceSystem(
        atomicTransactions,
        idToTransaction,
        exclusiveWithATr,
        exclusiveWithTr,
        interfereWith,
        maxSize,
        PostProcess.parseUserExclusiveTransactionTable(platformName),
        userTable,
        platformName,
        sourceFile
      )
    }

  def apply(
      platformName: String,
      maxSize: Int,
      sourceFile: String,
      atomicTransactionsS: BufferedSource,
      idToTransactionS: BufferedSource,
      exclusiveWithATrS: BufferedSource,
      exclusiveWithTrS: BufferedSource,
      interfereWithS: BufferedSource,
      finalUserTransactionExclusiveOpt: Option[BufferedSource],
      transactionUserNameOpt: Option[BufferedSource]
  ): TopologicalInterferenceSystem = {
    val userTable =
      for {
        f <- transactionUserNameOpt
      } yield {
        PostProcess
          .parseUserTransactionTable(f)
          .groupMapReduce(_._2)((k, _) => Set(k))(_ ++ _)
          .withDefaultValue(Set.empty)
      }
    TopologicalInterferenceSystem(
      PostProcess.parseAtomicTransactionTable(atomicTransactionsS),
      PostProcess.parseTransactionTable(idToTransactionS),
      PostProcess.parseAtomicTransactionInterfereTable(exclusiveWithATrS),
      PostProcess.parseTransactionInterfereTable(exclusiveWithTrS),
      PostProcess.parseServiceInterfereTable(interfereWithS),
      maxSize,
      for { f <- finalUserTransactionExclusiveOpt } yield PostProcess
        .parseUserExclusiveTransactionTable(f),
      userTable,
      platformName,
      sourceFile
    )
  }
}
