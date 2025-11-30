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

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.views.interference.model.formalisation.Comparator.{
  GE,
  LE
}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

private[pmlanalyzer] trait GroupedLitDecoder extends Decoder {

  val system: TopologicalInterferenceSystem
  val groupedLitToTransactions: Map[MLit, Set[PhysicalTransactionId]]
  val groupedLitToNodeSet: Map[MLit, Set[Set[MNode]]]

  def decodeTrivialSolutions(implm: SolverImplm): Seq[
    (
        Boolean,
        Set[Set[PhysicalTransactionId]],
        Map[Set[PhysicalTransactionId], Set[Set[UserTransactionId]]]
    )
  ] =
    for {
      (k, v) <- litToNode.toSeq
      isFree = v.isEmpty
      if groupedLitToTransactions(k).size >= 2
      physical = decodeModel(Set(k), isFree, implm)
      if physical.nonEmpty
      userDefined = physical.groupMapReduce(p => p)(
        decodeUserModel
      )(_ ++ _)
    } yield (isFree, physical, userDefined)

  def decodeModel(
      model: Set[MLit],
      modelIsFree: Boolean,
      implm: SolverImplm
  ): Set[Set[PhysicalTransactionId]] = {
    // Do not consider models that are above the max size
    if (model.size > system.maxSize)
      Set.empty
    // if the model is a single group of transactions containing only one physical transaction
    // then it cannot be a model (at least two transactions are needed)
    else if (
      model.size == 1 && groupedLitToTransactions(model.head).size == 1
    ) {
      Set.empty
      // if the model is a set of group, each one containing one physical transaction
      // then the model is the concatenation of these transactions
    } else if (model.forall(v => groupedLitToTransactions(v).size == 1)) {
      Set(model map { v => groupedLitToTransactions(v).head })
      // otherwise need to enumerate the set of non-exclusive transactions
    } else {
      // create a new solver of the same implementation that main solver
      val s = Solver(implm)
      val transactionIds = model.flatMap(groupedLitToTransactions)
      val trVariables = transactionIds
        .map(k => k -> MLit(Symbol(k.id.name)))
        .toMap
      // selecting a transaction implies forbidding all exclusive transactions
      // \forall_{t} v_t => \bigwedge_{t' \in exclusive(t)} \neg v_{t'}
      trVariables.foreach(kv =>
        s.assert(
          Implies(
            kv._2,
            And(
              system
                .exclusiveWithTr(kv._1)
                .intersect(trVariables.keySet)
                .map(trVariables)
                .map(Not.apply)
                .toSeq
            )
          )
        )
      )
      // at least one transaction must be selected per group
      s.assert(
        And(
          model
            .map(groupedLitToTransactions)
            .map(st => Or(st.map(trVariables).toSeq))
            .toSeq
        )
      )
      s.assertPB(
        trVariables.values.toSeq,
        LE,
        system.maxSize
      )
      // if considering only one group, at least two transactions must be selected
      if (model.size == 1)
        s.assertPB(trVariables.values.toSeq, GE, 2)

      // if the footprint of a set of transaction contains some nodes of the interference channel graph
      // then no more than one transaction per group must be selected (otherwise it is not free)
      if (modelIsFree)
        model
          .filter(m => groupedLitToNodeSet(m).exists(_.nonEmpty))
          .foreach(l =>
            s.assertPB(
              groupedLitToTransactions(l).map(trVariables).toSeq,
              LE,
              1
            )
          )
      val decodedModels =
        collection.mutable.Set.empty[Set[PhysicalTransactionId]]

      for {
        aLits <- s.enumerateSolution(trVariables.values.toSet)
        model = trVariables.collect({ case (k, v) if aLits.contains(v) => k })
      } {
        decodedModels += model.toSet
      }
      decodedModels.toSet
    }
  }
}
