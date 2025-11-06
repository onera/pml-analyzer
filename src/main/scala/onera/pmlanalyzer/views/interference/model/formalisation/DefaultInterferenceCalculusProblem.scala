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
import onera.pmlanalyzer.views.interference.model.formalisation.Comparator.EQ
import onera.pmlanalyzer.views.interference.model.formalisation.ModelElement.{
  EdgeId,
  NodeId
}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransaction,
  AtomicTransactionId,
  PhysicalTransaction,
  PhysicalTransactionId
}
import scalaz.Memo.immutableHashMapMemo

final case class DefaultInterferenceCalculusProblem(
    atomicTransactions: Map[AtomicTransactionId, AtomicTransaction],
    idToTransaction: Map[PhysicalTransactionId, PhysicalTransaction],
    exclusiveWithATr: Map[AtomicTransactionId, Set[AtomicTransactionId]],
    exclusiveWithTr: Map[PhysicalTransactionId, Set[
      PhysicalTransactionId
    ]],
    interfereWith: Map[Service, Set[Service]],
    maxSize: Option[Int],
    finalUserTransactionExclusiveOpt: Option[
      Map[UserTransactionId, Set[UserTransactionId]]
    ],
    transactionUserNameOpt: Option[
      Map[Set[AtomicTransactionId], Set[UserTransactionId]]
    ]
) extends InterferenceCalculusProblem
    with DefaultDecoder {

  private def undirectedEdgeId(l: MNode, r: MNode): EdgeId = Symbol(
    List(l, r).map(_.id.name).sorted.mkString("--")
  )

  private def nodeId(s: Set[Service]): NodeId = Symbol(
    s.toList.map(_.toString).sorted.mkString("<", "$", ">")
  )

  private val addNode: Set[Service] => MNode = immutableHashMapMemo { ss =>
    MNode(nodeId(ss))
  }

  private val addEdge: Set[MNode] => MEdge = immutableHashMapMemo {
    l =>
      MEdge(l.head, l.last, undirectedEdgeId(l.head, l.last))
  }

  private val transactionVar =
    (for {
      id <- idToTransaction.keySet
    } yield id -> MLit(id.id)).toMap

  // Add constraint C^1_{\Sys} i.e. transactions should not be exclusive
  private val exclusiveCst =
    for {
      (tr, exTrS) <- exclusiveWithTr.toSeq
      ex = exTrS - tr //removing tr to avoid var => not var
      if ex.nonEmpty
    } yield {
      val notEx = ex.map(tr2 => Not(transactionVar(tr2))).toSeq
      SimpleAssert(Implies(transactionVar(tr), And(notEx)))
    }
  
  // Add constraint C^2_{\Sys} cardinality constraint

  // association of the simple transaction path to its formatted name
  private val initialPathT = idToTransaction

  // the nodes of the service graph are the services grouped by exclusivity pairs
  private val serviceToNodes = interfereWith.transform((k, v) =>
    if (v.isEmpty)
      Set(addNode(Set(k)))
    else
      v.map(k2 => addNode(Set(k, k2)))
  )

  private val trToNode =
    (for {
      (t, atSet) <- initialPathT.toSeq
    } yield {
      t -> (for {
        at <- atSet
        t2 <- initialPathT.keySet -- exclusiveWithTr(t) - t
        at2 <- initialPathT(t2) -- exclusiveWithATr(at) - at
        s <- atomicTransactions(at)
        s2 <- atomicTransactions(at2)
        if s == s2 || interfereWith(s2).contains(s)
        n <- serviceToNodes(s)
      } yield n)
    }).toMap

  val nodeToTransaction: Map[MNode, Set[PhysicalTransactionId]] =
    trToNode.toSeq
      .flatMap((k, v) => v.map(k -> _))
      .groupMapReduce(_._2)((k, _) => Set(k))(_ ++ _)
  
  private val trToEdge =
    for {
      (tr, nSet) <- trToNode
    } yield {
      tr -> (for {
        nP <- nSet.subsets(2).toSet
      } yield addEdge(nP))
    }

  private val edgeToTr =
    trToEdge.toSeq
      .flatMap((k, v) => v.map(k -> _))
      .groupMapReduce(_._2)((k, _) => Set(k))(_ ++ _)

  val graph: MGraph =
    MGraph(trToNode.values.flatten.toSet, trToEdge.values.flatten.toSet)

  private val nodeVar =
    nodeToTransaction.transform((k, _) => MNodeLit(k, graph))

  private val edgeVar = edgeToTr.transform((k, _) => MEdgeLit(k, graph))

  private val nodeCst = for {
    (n, trs) <- nodeToTransaction.toSeq
  } yield {
    val atLeastTwo = Or(
      trs.subsets(2).map(ss => And(ss.map(transactionVar).toSeq)).toSeq
    )
    SimpleAssert(Equal(nodeVar(n), atLeastTwo))
  }

  private val edgeCst =
    for {
      (e, trS) <- edgeToTr.toSeq
    } yield SimpleAssert(Equal(edgeVar(e),
      And(
        Seq(
          Or(trS.map(transactionVar).toSeq),
          nodeVar(e.from),
          nodeVar(e.to)
        )
      )
    )
    )

  private val isFree =
    And(
      nodeVar.values.map(Not.apply).toSeq
    )

  private val nonEmpty = Or(nodeVar.values.toSeq)
  private val connected = Connected(graph)
  private val trContribToGraph = And(
    for {
      (tr, n) <- trToNode.toSeq
    } yield {
      if (n.isEmpty)
        Not(transactionVar(tr))
      else {
        val nV = n.map(nodeVar).toSeq
        Implies(
          transactionVar(tr),
          Or(nV)
        )
      }
    }
  )

  val transactionVars: Map[MLit, PhysicalTransactionId] =
    transactionVar.toSeq.groupMapReduce(_._2)(_._1)((l, _) => l)
  val nodeToServices: Map[MNode, Set[Service]] = serviceToNodes.toSeq
    .flatMap((k, v) => v.map(k -> _))
    .groupMapReduce(_._2)((k, _) => Set(k))(_ ++ _)

  val variables: Set[MLit] = transactionVars.keySet

  val litToNode: Map[MLit, Set[MNode]] =
    nodeToTransaction.toSeq
      .flatMap((k, v) => v.map(k -> _))
      .groupMapReduce((_, v) => transactionVar(v))((k, _) => Set(k))(_ ++ _)

  def instantiate(
      k: Int,
      computeFree: Boolean,
      implm: SolverImplm
  ): Solver = {
    val s = Solver(implm)
    graph.toLit(s)
    s.assertPB(transactionVar.values.toSeq, EQ, k)
    if (!computeFree) {
      for {
        c <- Set(connected, trContribToGraph, nonEmpty)
      } {
        s.assert(c)
      }
    } else
      s.assert(isFree)
    (nodeCst ++ edgeCst ++ exclusiveCst).foreach(_.assert(s))
    s
  }

}
