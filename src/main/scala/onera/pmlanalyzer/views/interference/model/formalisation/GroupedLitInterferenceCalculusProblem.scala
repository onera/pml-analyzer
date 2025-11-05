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
  LitId,
  NodeId
}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*
import scalaz.Memo.immutableHashMapMemo

import scala.collection.immutable.SortedMap

final case class GroupedLitInterferenceCalculusProblem(
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
    with GroupedLItDecoder {

  private def undirectedEdgeId(l: MNode, r: MNode): EdgeId = Symbol(
    List(l, r).map(_.id.name).sorted.mkString("--")
  )

  private def nodeId(s: Set[Service]): NodeId = Symbol(
    s.toList.map(_.toString).sorted.mkString("<", "$", ">")
  )

  private val addNode: Set[Service] => MNode = immutableHashMapMemo { ss =>
    MNode(nodeId(ss))
  }

  private val addEdge: ((MNode, MNode)) => MEdge = immutableHashMapMemo {
    (l, r) =>
      MEdge(l, r, undirectedEdgeId(l, r))
  }

  private val addLit: LitId => MLit = immutableHashMapMemo { s => MLit(s) }

  // DEFINITION OF VARIABLES

  // association of the simple transaction path to its formatted name
  private val initialPathT = idToTransaction.to(SortedMap)

  // the actual path will be the service that can be a channel, ie
  // a service that is a service (or exclusive to a service) of a different and non exclusive transaction
  // FIXME If a multi-path transaction, the following computation consider that one of the services used by
  // several atomic atomicTransactions could be an interference channel that is obviously false
  // and complexify the graph for no reason
  private val pathT: Map[PhysicalTransactionId, Set[AtomicTransaction]] =
    initialPathT.view
      .mapValues(s =>
        s.map(t =>
          atomicTransactions(t).filter(s =>
            atomicTransactions.keySet.exists(t2 =>
              t != t2 &&
                !exclusiveWithATr(t).contains(t2) &&
                atomicTransactions(t2)
                  .exists(s2 => s2 == s || interfereWith(s2).contains(s))
            )
          )
        )
      )
      .toMap

  // the nodes of the service graph are the services grouped by exclusivity pairs
  private val serviceToNodes = interfereWith.transform((k, v) =>
    if (v.isEmpty)
      Set(addNode(Set(k)))
    else
      v.map(k2 => addNode(Set(k, k2)))
  )

  val nodeToServices: Map[MNode, Set[Service]] = serviceToNodes.keySet
    .flatMap(k => serviceToNodes(k).map(_ -> k))
    .groupMap(_._1)(_._2)

  private val reducedNodePath = pathT
    .transform((_, v) => v.map(t => t.map(serviceToNodes)))

  private val transactionToGroupedLit = reducedNodePath
    .groupMap(_._2)(_._1)
    .values
    .flatMap(ss => ss.map(_ -> ss))
    .groupMapReduce(_._1)(x => addLit(groupedTransactionsLitId(x._2.toSet).id))(
      (l, _) => l
    )

  val groupedLitToTransactions: Map[MLit, Set[PhysicalTransactionId]] =
    transactionToGroupedLit
      .groupMap(_._2)(_._1)
      .transform((_, v) => v.toSet)

  val groupedLitToNodeSet: Map[MLit, Set[Set[MNode]]] = transactionToGroupedLit
    .groupMapReduce(_._2)(kv => reducedNodePath(kv._1).map(_.toSet.flatten))(
      (l, _) => l
    )

  // Add an undirected edge for any set containing two nodes
  private val addUndirectedEdgeI: Set[MNode] => MEdge = immutableHashMapMemo {
    lr =>
      MEdge(lr.head, lr.last, undirectedEdgeId(lr.head, lr.last))
  }

  private val addUndirectedEdge = (lr: Set[Service]) => {
    if (lr.size == 1) {
      for {
        ns <- serviceToNodes(lr.head).subsets(2).toSet
      } yield addUndirectedEdgeI(ns)
    } else {
      serviceToNodes(lr.head).flatMap(n1 =>
        serviceToNodes(lr.last).map(n2 => addUndirectedEdgeI(Set(n1, n2)))
      )
    }
  }

  // an edge is added to service graph iff one of transaction use it:
  // * the transaction must not be transparent
  // * the edge must not contain a service considered as non impacted
  // FIXME * an edge is added between all nodes sharing a common service
  private val edgesToTransactions: Map[MEdge, Set[PhysicalTransactionId]] =
    pathT.keySet
      .flatMap(s =>
        val pathEdges = for {
          t <- pathT(s) if t.size > 1
          servCouple <- t.sliding(2)
          edge <- addUndirectedEdge(servCouple.toSet)
        } yield edge -> s
        val serviceEdges = {
          for {
            t <- pathT(s)
            service <- t
            e <- addUndirectedEdge(Set(service))
          } yield e -> s
        }
        pathEdges ++ serviceEdges
      )
      .groupMap(_._1)(_._2)

  val graph: MGraph = MGraph(nodeToServices.keySet, edgesToTransactions.keySet)

  // DEFINITION OF CONSTRAINTS

  // an edge is enabled iff at least one of the atomicTransactions using is selected
  private val edgeCst = edgesToTransactions
    .transform((k, trs) =>
      SimpleAssert(
        Equal(
          MEdgeLit(k, graph),
          Or(trs.map(transactionToGroupedLit).toSeq)
        )
      )
    )

  private val trivialFreeTransactions =
    transactionToGroupedLit.values.toSet
      .filter(
        groupedLitToNodeSet(_).forall(_.isEmpty)
      ) // All paths of the transactions are only private ones

  private val contributeToGraph =
    And(
      for {
        (l, ns) <- groupedLitToNodeSet.toSeq
        nsL = ns.flatten.map(n => MNodeLit(n, graph)).toSeq
      } yield {
        Implies(l, Or(nsL))
      }
    )

  private val nonEmptyGraph = Or(graph.nodes.map(n => MNodeLit(n, graph)).toSeq)
  private val isNotTrivialFree = And(
    trivialFreeTransactions.map(v => Not(v)).toSeq
  )
  private val isConnected = Connected(graph)
  private val isITF: Set[Expr | Connected] =
    Seq(
      nonEmptyGraph,
      isNotTrivialFree,
      isConnected,
      contributeToGraph
    ).toSet

  // free are only computed when the selected groupedLit
  // so for each node n, \sum_{g, n \in \footprint(g)} g <= 1
  private val isFree = And(
    for {
      (g, ns) <- groupedLitToNodeSet.toSeq
      gs = groupedLitToNodeSet
        .collect({
          case (k, v) if k != g && v.flatten.intersect(ns.flatten).nonEmpty =>
            k
        })
        .toSeq
      if gs.nonEmpty
    } yield {
      Implies(g, Not(Or(gs)))
    }
  )

  private val nonExclusiveSn = for {
    (l, trs) <- groupedLitToTransactions
    (l2, trs2) <- groupedLitToTransactions
    if l != l2
    if trs.forall(t => trs2.subsetOf(exclusiveWithTr(t)))
      || trs2.forall(t => trs.subsetOf(exclusiveWithTr(t)))
  } yield Not(And(Seq(l, l2)))

  private val nonExclusive =
    nonExclusiveSn.map(SimpleAssert.apply)

  val nodeToTransaction: Map[MNode, Set[PhysicalTransactionId]] =
    (for {
      (g, nS) <- groupedLitToNodeSet.toSet
      t <- groupedLitToTransactions(g)
      n <- nS.flatten
    } yield {
      n -> t
    }).groupMap(_._1)(_._2)

  val litToNode: Map[MLit, Set[MNode]] =
    nodeToTransaction.toSeq
      .flatMap((k, v) => v.map(k -> _))
      .groupMapReduce((_, v) => transactionToGroupedLit(v))((k, _) => Set(k))(
        _ ++ _
      )

  def instantiate(
      k: Int,
      computeFree: Boolean,
      implm: SolverImplm
  ): Solver = {
    val s = Solver(implm)
    s.assertPB(groupedLitToTransactions.keySet.toSeq, EQ, k)
    if (!computeFree) {
      for {
        c <- isITF
      } {
        s.assert(c)
      }
    } else {
      s.assert(isFree)
    }
    (edgeCst.values.toSeq ++ nonExclusive).foreach(_.assert(s))
    s
  }
}
