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

import monosat.Logic._
import monosat.{Comparison, Graph, Lit, Solver}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.service.Service
import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.views.interference.model.formalisation.ProblemElement._
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{Channel, PhysicalScenario, PhysicalScenarioId}
import onera.pmlanalyzer.views.interference.model.specification.{ApplicativeTableBasedInterferenceSpecification, InterferenceSpecification}

import scala.jdk.CollectionConverters._

trait ProblemElement

object ProblemElement {
  type NodeId = Symbol
  type EdgeId = Symbol
  type LitId = Symbol

}

trait ALit extends ProblemElement {
  val toLit: Solver => Lit
}

trait Assert extends ProblemElement {
  def assert(s: Solver): Unit
}

case class SimpleAssert(l: ALit) extends Assert {
  def assert(s: Solver): Unit = s.assertTrue(l.toLit(s))
}

case class AssertPB(l: Set[ALit], comp: Comparison, k: Int) extends Assert {
  def assert(s: Solver): Unit = s.assertPB(l.map(_.toLit(s)).toSeq.asJava, comp, k)
}

case class MNode(id: NodeId) extends ProblemElement

case class MGraph(nodes: Set[MNode], edges: Set[MEdge]) extends ProblemElement {
  val toGraph: Solver => Graph = immutableHashMapMemo {
    s => {
      val g = new Graph(s)

      val addNode: MNode => Int = immutableHashMapMemo {
        s => g.addNode(s.id.name)
      }

      val addEdge: MEdge => Lit = immutableHashMapMemo {
        e => g.addUndirectedEdge(addNode(e.from), addNode(e.to), e.id.name)
      }

      nodes.foreach(addNode)
      edges.foreach(addEdge)
      g
    }
  }
}

case class MEdge(from: MNode, to: MNode, id: EdgeId) extends ProblemElement

case class MEdgeLit(edge: MEdge, graph: MGraph) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => graph.toGraph(s).getEdge(edge.id.name).l }
}

case class MLit(id: LitId) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => new Lit(s, id.name) }
}

case class And(l: ALit*) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => and(l.map(_.toLit(s)).asJava) }
}

case class Or(l: ALit*) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => or(l.map(_.toLit(s)).asJava) }
}

case class Implies(l: ALit, r: ALit) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => implies(l.toLit(s), r.toLit(s)) }
}

case class Not(l: ALit) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => not(l.toLit(s)) }
}

case class Equal(l: ALit, r: ALit) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => equal(l.toLit(s), r.toLit(s)) }
}

case class Reaches(graph: MGraph, from: MNode, to: MNode) extends ALit {
  val toLit: Solver => Lit = immutableHashMapMemo { s => {
    val g = graph.toGraph(s)
    g.reaches(g.getNode(from.id.name), g.getNode(to.id.name))
  }
  }
}

class Problem(val platform: Platform with InterferenceSpecification,
              val groupedScenarios: Map[MLit, Set[PhysicalScenarioId]],
              val litToNodeSet: Map[MLit, Set[Set[MNode]]],
              val idToScenario: Map[PhysicalScenarioId, PhysicalScenario],
              val exclusiveScenarios: Map[PhysicalScenarioId, Set[PhysicalScenarioId]],
              val serviceGraph: MGraph,
              val isFree: ALit,
              val isITF: ALit,
              val pbCst: Set[AssertPB],
              val simpleCst: Set[SimpleAssert],
              val nodeToServices: Map[MNode, Set[Service]],
              val serviceToScenarioLit: Map[Service, Set[PhysicalScenarioId]],
              val maxSize: Option[Int]
             ) extends ProblemElement {

  private val nodeToScenario = nodeToServices.transform((_, v) => v.flatMap(serviceToScenarioLit))

  def decodeUserModel(physicalModel: Set[PhysicalScenarioId]): Set[Set[UserScenarioId]] = platform match {
    case _ if physicalModel.isEmpty => Set.empty
    case _ if maxSize.isDefined && physicalModel.size > maxSize.get => Set.empty
    case spec: TransactionLibrary =>
      val scenario = idToScenario
        .view
        .filterKeys(physicalModel)
        .toMap

      val userNames = scenario
        .view
        .mapValues(spec.scenarioUserName)
        .toMap
        .transform((k, v) => if (v.isEmpty) Set(UserScenarioId(k.id)) else v)

      val results = userNames.values.tail
        .foldLeft(userNames.values.head.map(n => Set(n)))((acc, names) =>
          for {
            p <- acc
            last <- platform match {
              case app: ApplicativeTableBasedInterferenceSpecification =>
                val x = names.filter(id =>
                  app.finalUserScenarioExclusive(id).intersect(p).isEmpty)
                x
              case _ => names
            }
          } yield {
            p + last
          })
      results
    case _ => Set(physicalModel.map(s => UserScenarioId(s.id)))
  }

  //FIXME Are we integrating exclusive service in the channel even if not used in the scenario?
  def decodeChannel(model: Set[PhysicalScenarioId]): Channel = {
    if (maxSize.isDefined && model.size > maxSize.get)
      Set.empty
    else
      nodeToScenario
        .keySet
        .filter(k => model.intersect(nodeToScenario(k)).size >= 2)
        .flatMap(nodeToServices)
  }

  def decodeModel(model: Set[MLit], isFree: Boolean): Set[Set[PhysicalScenarioId]] = {
    if (maxSize.isDefined && model.size > maxSize.get)
      Set.empty
    else if (model.size == 1 && groupedScenarios(model.head).size == 1) {
      Set.empty
    } else if (model.forall(v => groupedScenarios(v).size == 1)) {
      Set(model map { v => groupedScenarios(v).head })
    } else {
      val s = Problem.getNewSolver("-decide-theories")
      val scenarios = model.flatMap(groupedScenarios)
      val variables = scenarios
        .map(k => k -> new Lit(s, k.id.name))
        .toMap
      variables.foreach(kv => s.assertTrue(implies(kv._2, and(exclusiveScenarios(kv._1).intersect(variables.keySet).map(variables).map(not).asJava))))
      s.assertAnd(model.map(groupedScenarios).map(st => or(st.map(variables).asJava)).asJava)
      for {m <- maxSize} yield s.assertPB(variables.values.toSeq.asJava, LEQ, m)
      if (model.size == 1)
        s.assertPB(variables.values.toSeq.asJava, GEQ, 2)
      if (isFree)
        model
          .filter(m => litToNodeSet(m).exists(_.nonEmpty))
          .foreach(l => s.assertAtMostOne(groupedScenarios(l).map(variables).asJava))
      val decodedModels = collection.mutable.Set.empty[Set[PhysicalScenarioId]]
      while (s.solve()) {
        val (positiveModel, negativeModel) = variables.keySet.partition(k => variables(k).value())
        decodedModels += positiveModel
        s.assertOr((positiveModel.map(variables).map(not) ++ negativeModel.map(variables)).asJava)
      }
      s.close()
      decodedModels.toSet
    }
  }

  def instantiate(k: Int): Solver = {
    val s = Problem.getNewSolver("-decide-theories")
    serviceGraph.toGraph(s)
    s.assertTrue(or(isITF.toLit(s), isFree.toLit(s)))
    pbCst.foreach(_.assert(s))
    simpleCst.foreach(_.assert(s))
    s.assertPB(groupedScenarios.keySet.map(_.toLit(s)).toSeq.asJava, EQ, k)
    s
  }
}

object Problem {
  def apply(platform: Platform with InterferenceSpecification,
            groupedScenarios: Map[MLit, Set[PhysicalScenarioId]],
            litToNodeSet: Map[MLit, Set[Set[MNode]]],
            idToScenario: Map[PhysicalScenarioId, PhysicalScenario],
            exclusiveScenarios: Map[PhysicalScenarioId, Set[PhysicalScenarioId]],
            serviceGraph: MGraph,
            isFree: ALit,
            isITF: ALit,
            pbCst: Set[AssertPB],
            simpleCst: Set[SimpleAssert],
            nodeToServices: Map[MNode, Set[Service]],
            serviceToScenarioLit: Map[Service, Set[PhysicalScenarioId]],
            maxSize: Option[Int] = None
           ): Problem =
    new Problem(
      platform,
      groupedScenarios,
      litToNodeSet,
      idToScenario,
      exclusiveScenarios,
      serviceGraph,
      isFree,
      isITF,
      pbCst,
      simpleCst,
      nodeToServices,
      serviceToScenarioLit,
      maxSize)

  private def getNewSolver(s:String) : Solver = new Solver(s)
}