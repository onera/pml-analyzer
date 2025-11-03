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
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.formalisation.Comparator.*
import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.views.interference.model.formalisation.ModelElement.*
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{Channel, PhysicalTransaction, PhysicalTransactionId}
import onera.pmlanalyzer.views.interference.model.specification.{ApplicativeTableBasedInterferenceSpecification, InterferenceSpecification}

import java.io.File
import scala.jdk.CollectionConverters.*

trait ModelElement

object ModelElement {
  type NodeId = Symbol
  type EdgeId = Symbol
  type LitId = Symbol

}

enum Comparator {
  case LT,LE,EQ,GE,GT
}

trait ALit extends ModelElement {
  def toLit (s:Solver): s.BoolLit
}

trait Expr extends ModelElement{
  def toExpr(s:Solver): s.Expression
}

trait Assert extends ModelElement {
  def assert(s: Solver): Unit
}

final case class SimpleAssert(l: Expr) extends Assert {
  def assert(s: Solver): Unit = s.assert(l)

  override def toString: String = s"assert(\n\t$l\n)"
}

final case class AssertPB(l: Set[ALit], comp: Comparator, k: Int)
    extends Assert {
  def assert(s: Solver): Unit =
    s.assertPB(l.toSeq, comp, k)

  override def toString: String =
    s"""assert(
       |${l.mkString("\t",",\n\t","")},
       |\t$comp,
       |\t$k
      |)""".stripMargin
}

final case class MNode(id: NodeId) extends ModelElement{
  override def toString: String = id.name
}

final case class MNodeLit(n:MNode, graph:MGraph) extends ALit with Expr {
  def toLit(s: Solver): s.BoolLit =
      s.getNode(graph, n.id.name) match {
        case Some(value) => value
        case None => throw Exception(s"Unknown node ${n.id.name}")
      }
  
  def toExpr(s: Solver): s.Expression = toLit(s)

  override def toString: String = s"v_${n.id.name}"
}

final case class MGraph(nodes: Set[MNode], edges: Set[MEdge])
    extends ModelElement {
  def toLit(s:Solver) : s.GraphLit = s.graphLit(this)
  def exportGraph(s:Solver, file:File): Unit = 
    s.exportGraph(this,file)
}

final case class MEdge(from: MNode, to: MNode, id: EdgeId)
    extends ModelElement

final case class MEdgeLit(edge: MEdge, graph: MGraph) extends ALit with Expr {
  def toLit (s:Solver) : s.BoolLit =
    s.getEdge(graph, edge.id.name) match {
      case Some(value) => value
      case None => throw Exception(s"Unknown edge ${edge.id.name}")
    }

  def toExpr(s: Solver): s.Expression = toLit(s)

  override def toString: String = s"v_${edge.id.name}"
}

final case class MLit(id: LitId) extends ALit with Expr {
  def toLit (s:Solver): s.BoolLit = s.boolLit(this)
  def toExpr(s: Solver): s.Expression = s.boolLit(this)

  override def toString: String = id.name
}

final case class And(l: Seq[Expr]) extends Expr {
  def toExpr (s:Solver): s.Expression = s.and(l)

  override def toString: String =
    s"""and(
       |${l.mkString("\t",",\n\t","\n)")}""".stripMargin
}

final case class Or(l: Seq[Expr]) extends Expr {
  def toExpr (s:Solver): s.Expression = s.or(l)

  override def toString: String =
    s"""or(
       |${l.mkString("\t", ",\n\t", "\n)")}""".stripMargin
}

final case class Implies(l: Expr, r: Expr) extends Expr {
  def toExpr (s:Solver): s.Expression = s.implies(l,r)

  override def toString: String =
    s"""implies(
       |\t$l,
       |\t$r
       |)""".stripMargin
}

final case class Not(l: Expr) extends Expr {
  def toExpr (s:Solver): s.Expression = s.not(l)

  override def toString: String =
    s"not($l)"
}

final case class Equal(l: Expr, r: Expr) extends Expr {
  def toExpr (s:Solver): s.Expression = s.eq(l,r)

  override def toString: String =
    s"""eq(
       |\t$l,
       |\t$r
       |)""".stripMargin
}

final case class Connected(graph:MGraph) {
  def toConstraint (s:Solver): s.Constraint = s.connected(graph)
}

class Model(
    val platform: Platform with InterferenceSpecification,
    val groupedTransactions: Map[MLit, Set[PhysicalTransactionId]],
    val litToNodeSet: Map[MLit, Set[Set[MNode]]],
    val idToTransaction: Map[PhysicalTransactionId, PhysicalTransaction],
    val exclusiveTransactions: Map[PhysicalTransactionId, Set[
      PhysicalTransactionId
    ]],
    val serviceGraph: MGraph,
    val isFree: Expr,
    val isITF: Seq[Expr | Connected],
    val pbCst: Set[AssertPB],
    val simpleCst: Set[SimpleAssert],
    val nodeToServices: Map[MNode, Set[Service]],
    val serviceToTransactionLit: Map[Service, Set[PhysicalTransactionId]],
    val maxSize: Option[Int],
    val implm: SolverImplm
) extends ModelElement {

  private val nodeToTransaction =
    nodeToServices.transform((_, v) => v.flatMap(serviceToTransactionLit))

  def decodeUserModel(
      physicalModel: Set[PhysicalTransactionId]
  ): Set[Set[UserTransactionId]] = platform match {
    case _ if physicalModel.isEmpty                  => Set.empty
    case _ if maxSize.exists(physicalModel.size > _) => Set.empty
    case spec: TransactionLibrary =>
      val transaction = idToTransaction.view
        .filterKeys(physicalModel)
        .toMap

      val userNames = transaction.view
        .mapValues(spec.transactionUserName)
        .toMap
        .transform((k, v) => if (v.isEmpty) Set(UserTransactionId(k.id)) else v)

      val results = userNames.values.tail
        .foldLeft(userNames.values.head.map(n => Set(n)))((acc, names) =>
          for {
            p <- acc
            last <- platform match {
              case app: ApplicativeTableBasedInterferenceSpecification =>
                val x = names.filter(id =>
                  app.finalUserTransactionExclusive(id).intersect(p).isEmpty
                )
                x
              case _ => names
            }
          } yield {
            p + last
          }
        )
      results
    case _ => Set(physicalModel.map(s => UserTransactionId(s.id)))
  }

  // FIXME Are we integrating exclusive service in the channel even if not used in the transaction?
  def decodeChannel(model: Set[PhysicalTransactionId]): Channel = {
    if (maxSize.exists(model.size > _))
      Set.empty
    else
      nodeToTransaction.keySet
        .filter(k => model.intersect(nodeToTransaction(k)).size >= 2)
        .flatMap(nodeToServices)
  }

  def decodeModel(
      model: Set[MLit],
      modelIsFree: Boolean
  ): Set[Set[PhysicalTransactionId]] = {
    //Do not consider models that are above the max size
    if (maxSize.exists(model.size > _))
      Set.empty
    //if the model is a single group of transactions containing only one physical transaction
    //then it cannot be a model (at least two transactions are needed)
    else if (model.size == 1 && groupedTransactions(model.head).size == 1) {
      Set.empty
    //if the model is a set of group, each one containing one physical transaction
    //then the model is the concatenation of these transactions
    } else if (model.forall(v => groupedTransactions(v).size == 1)) {
      Set(model map { v => groupedTransactions(v).head })
    //otherwise need to enumerate the set of non-exclusive transactions
    } else {
      val s = Solver(implm)
      val transactionIds = model.flatMap(groupedTransactions)
      val variables = transactionIds
        .map(k => k -> MLit(Symbol(k.id.name)))
        .toMap
      //selecting a transaction implies forbidding all exclusive transactions
      // \forall_{t} v_t => \bigwedge_{t' \in exclusive(t)} \neg v_{t'} 
      variables.foreach(kv =>
        s.assert(
          Implies(
            kv._2,
            And(
              exclusiveTransactions(kv._1)
                .intersect(variables.keySet)
                .map(variables)
                .map(Not.apply)
                .toSeq
            )
          )
        )
      )
      //at least one transaction must be selected per group
      s.assert(
        And(model
          .map(groupedTransactions)
          .map(st => Or(st.map(variables).toSeq))
          .toSeq)
      )
      //the model cannot contain more that maxSize transaction
      for { m <- maxSize } yield s.assertPB(
        variables.values.toSeq,
        LE,
        m
      )
      //if considering only one group, at least two transactions must be selected
      if (model.size == 1)
        s.assertPB(variables.values.toSeq, GE, 2)
      
      //if the footprint of a set of transaction contains some nodes of the interference channel graph
      //then no more than one transaction per group must be selected (otherwise it is not free)
      if (modelIsFree)
        model
          .filter(m => litToNodeSet(m).exists(_.nonEmpty))
          .foreach(l =>
            s.assertPB(groupedTransactions(l).map(variables).toSeq,LE, 1)
          )
      val decodedModels =
        collection.mutable.Set.empty[Set[PhysicalTransactionId]]

      for {
        aLits <- s.enumerateSolution(variables.values.toSet)
        model = variables.collect({case (k,v) if aLits.contains(v) => k})
      } {
        decodedModels += model.toSet
      }
      decodedModels.toSet
    }
  }

  def instantiate(k: Int, computeFree: Boolean): Solver = {
    val s = Solver(implm)
    serviceGraph.toLit(s)
    if(!computeFree) {
      for {
        c <- isITF
      } {
        s.assert(c)
      }
    } else
      s.assert(isFree)
    pbCst.foreach(_.assert(s))
    simpleCst.foreach(_.assert(s))
    s.assertPB(groupedTransactions.keySet.toSeq, EQ, k)
    s
  }
}

object Model {
  def apply(
      platform: Platform with InterferenceSpecification,
      groupedTransactions: Map[MLit, Set[PhysicalTransactionId]],
      litToNodeSet: Map[MLit, Set[Set[MNode]]],
      idToTransaction: Map[PhysicalTransactionId, PhysicalTransaction],
      exclusiveTransactions: Map[PhysicalTransactionId, Set[
        PhysicalTransactionId
      ]],
      serviceGraph: MGraph,
      isFree: Expr,
      isITF: Seq[Expr|Connected],
      pbCst: Set[AssertPB],
      simpleCst: Set[SimpleAssert],
      nodeToServices: Map[MNode, Set[Service]],
      serviceToTransactionLit: Map[Service, Set[PhysicalTransactionId]],
      implm: SolverImplm,
      maxSize: Option[Int] = None
  ): Model =
    new Model(
      platform,
      groupedTransactions,
      litToNodeSet,
      idToTransaction,
      exclusiveTransactions,
      serviceGraph,
      isFree,
      isITF,
      pbCst,
      simpleCst,
      nodeToServices,
      serviceToTransactionLit,
      maxSize,
      implm
    )
}
