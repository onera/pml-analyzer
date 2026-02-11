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

import onera.pmlanalyzer.views.interference.model.formalisation.ModelElement.{
  EdgeId,
  NodeId
}
import scalaz.Memo.immutableHashMapMemo

private[pmlanalyzer] trait InterferenceCalculusProblem {
  val system: TopologicalInterferenceSystem

  val graph: MGraph

  def instantiate(k: Int, computeFree: Boolean, implm: SolverImplm): Solver

  protected def undirectedEdgeId(l: MNode, r: MNode): EdgeId = Symbol(
    List(l, r).map(_.id.name).sorted.mkString("--")
  )

  protected def nodeId(s: Set[Symbol]): NodeId = Symbol(
    s.toList.map(_.name).sorted.mkString("<", "$", ">")
  )

  protected val addNode: Set[Symbol] => MNode = immutableHashMapMemo { ss =>
    MNode(nodeId(ss))
  }

  protected val addEdge: Set[MNode] => MEdge = immutableHashMapMemo { l =>
    MEdge(l.head, l.last, undirectedEdgeId(l.head, l.last))
  }

  // the nodes of the service graph are the services grouped by exclusivity pairs
  protected val serviceToNodes: Map[Symbol, Set[MNode]] =
    system.interfereWith.transform((k, v) =>
      require(
        v.nonEmpty,
        s"[ERROR] Service $k should at least interfere with itself"
      )
      // build all pairs of interfering services
      val allPairs =
        for {
          s <- v - k
        } yield addNode(Set(k, s))
      // if k is not interfering with other services, then create a singleton
      if (allPairs.isEmpty)
        Set(addNode(Set(k)))
      else
        allPairs.toSet
    )
}

private[pmlanalyzer] object InterferenceCalculusProblem {
  enum Method {
    case GroupedLitBased, Default
  }
}
