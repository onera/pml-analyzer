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

final case class GroupedLitInterferenceCalculusProblem(system:TopologicalInterferenceSystem) 
  extends InterferenceCalculusProblem
    with GroupedLitDecoder {

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
  private val initialPathT = system.idToTransaction.to(SortedMap)

  // the actual path will be the service that can be a channel, ie
  // a service that is a service (or exclusive to a service) of a different and non exclusive transaction
  // FIXME If a multi-path transaction, the following computation consider that one of the services used by
  // several atomic atomicTransactions could be an interference channel that is obviously false
  // and complexify the graph for no reason
  private val pathT: Map[PhysicalTransactionId, Set[AtomicTransaction]] =
    initialPathT.view
      .mapValues(s =>
        s.map(t =>
          system.atomicTransactions(t).filter(s =>
            system.atomicTransactions.keySet.exists(t2 =>
              t != t2 &&
                !system.exclusiveWithATr(t).contains(t2) &&
                system.atomicTransactions(t2)
                  .exists(s2 => s2 == s || system.interfereWith(s2).contains(s))
            )
          )
        )
      )
      .toMap

  // the nodes of the service graph are the services grouped by exclusivity pairs
  private val serviceToNodes = system.interfereWith.transform((k, v) =>
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

  private val edgesToTransactions =
    (for {
      (g, nSet) <- groupedLitToNodeSet.toSet
      nP <- nSet.flatten.subsets(2).toSet
    } yield {
      addUndirectedEdgeI(nP) -> groupedLitToTransactions(g)
    }).groupMapReduce(_._1)(_._2)(_ ++ _)

  val graph: MGraph = MGraph(
    groupedLitToNodeSet.values.flatten.flatten.toSet,
    edgesToTransactions.keySet
  )

  private val nodeVar =
    (for {
      n <- groupedLitToNodeSet.values.flatten.flatten.toSet
    } yield {
      n -> MNodeLit(n, graph)
    }).toMap

  // DEFINITION OF CONSTRAINTS

  // an edge is enabled iff at least one of the atomicTransactions using is selected
  private val edgeCst = edgesToTransactions
    .transform((k, trs) =>
      SimpleAssert(
        Equal(
          MEdgeLit(k, graph),
          And(
            Seq(
              Or(trs.map(transactionToGroupedLit).toSeq),
              nodeVar(k.from),
              nodeVar(k.to)
            )
          )
        )
      )
    )

  private val nonExclusiveSn = for {
    (l, trs) <- groupedLitToTransactions
    (l2, trs2) <- groupedLitToTransactions
    if l != l2
    if trs.forall(t => trs2.subsetOf(system.exclusiveWithTr(t)))
      || trs2.forall(t => trs.subsetOf(system.exclusiveWithTr(t)))
  } yield Not(And(Seq(l, l2)))

  private val nonExclusive =
    nonExclusiveSn.map(SimpleAssert.apply)

  // if the grouped lit contains only one transaction
  // then another one must be selected
  private val atLeastOnePhysicalModel =
    for {
      (g, trS) <- groupedLitToTransactions.toSeq
      if trS.size == 1
    } yield {
      SimpleAssert(
        Implies(
          g,
          Or((groupedLitToTransactions.keySet - g).toSeq)
        )
      )
    }

  val nodeToTransaction: Map[MNode, Set[PhysicalTransactionId]] =
    (for {
      (g, nS) <- groupedLitToNodeSet.toSet
      t <- groupedLitToTransactions(g)
      n <- nS.flatten
    } yield {
      n -> t
    }).groupMap(_._1)(_._2)

  val litToNode: Map[MLit, Set[MNode]] =
    groupedLitToNodeSet.transform((_, v) => v.flatten)

  val variables: Set[MLit] =
    groupedLitToTransactions.keySet

  def instantiate(
      k: Int,
      computeFree: Boolean,
      implm: SolverImplm
  ): Solver = {
    val s = Solver(implm)
    graph.toLit(s)
    s.assertPB(groupedLitToTransactions.keySet.toSeq, EQ, k)
    if (!computeFree) {
      val trivialFreeTransactions =
        transactionToGroupedLit.values.toSet
          .filter(
            groupedLitToNodeSet(_).forall(_.isEmpty)
          ) // All paths of the transactions are only private ones

      val contributeToGraph =
        And(
          for {
            (l, ns) <- groupedLitToNodeSet.toSeq
            nsL = ns.flatten.map(n => nodeVar(n)).toSeq
          } yield {
            Implies(l, Or(nsL))
          }
        )

      val nonEmptyGraph = Or(graph.nodes.map(n => nodeVar(n)).toSeq)
      val isNotTrivialFree = And(
        trivialFreeTransactions.map(v => Not(v)).toSeq
      )
      val isConnected = Connected(graph)
      for {
        c <- Seq(
          nonEmptyGraph,
          isNotTrivialFree,
          isConnected,
          contributeToGraph
        )
      } {
        s.assert(c)
      }
    } else {
      // free are only computed when the selected groupedLit does not share channel
      // so for each node n, \sum_{g, n \in \footprint(g)} g <= 1
      val isFree = And(
        for {
          (g, ns) <- groupedLitToNodeSet.toSeq
          gs = groupedLitToNodeSet
            .collect({
              case (k, v)
                  if k != g && v.flatten.intersect(ns.flatten).nonEmpty =>
                k
            })
            .toSeq
          if gs.nonEmpty
        } yield {
          Implies(g, Not(Or(gs)))
        }
      )
      s.assert(isFree)
    }
    (edgeCst.values.toSeq ++ nonExclusive ++ atLeastOnePhysicalModel)
      .foreach(_.assert(s))
    s
  }
}
