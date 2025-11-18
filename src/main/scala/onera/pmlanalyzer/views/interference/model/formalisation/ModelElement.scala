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

import onera.pmlanalyzer.views.interference.model.formalisation.ModelElement.*

import java.io.File
import scala.jdk.CollectionConverters.*

trait ModelElement

object ModelElement {
  type NodeId = Symbol
  type EdgeId = Symbol
  type LitId = Symbol

}

enum Comparator {
  case LT, LE, EQ, GE, GT
}

trait ALit extends ModelElement {
  def toLit(s: Solver): s.BoolLit
}

trait Expr extends ModelElement {
  def toExpr(s: Solver): s.Expression
}

trait Assert extends ModelElement {
  def assert(s: Solver): Unit
}

final case class SimpleAssert(l: Expr) extends Assert {
  def assert(s: Solver): Unit = s.assert(l)

  override def toString: String = s"assert(\n\t$l\n)"
}

final case class PB(l: Seq[Expr], comparator: Comparator, k: Int) extends Expr {
  def toExpr(s: Solver): s.Expression = ???
}

final case class AssertPB(l: Set[ALit], comp: Comparator, k: Int)
    extends Assert {
  def assert(s: Solver): Unit =
    s.assertPB(l.toSeq, comp, k)

  override def toString: String =
    s"""assert(
       |${l.mkString("\t", ",\n\t", "")},
       |\t$comp,
       |\t$k
      |)""".stripMargin
}

final case class MNode(id: NodeId) extends ModelElement {
  override def toString: String = id.name
}

final case class MNodeLit(n: MNode, graph: MGraph) extends ALit with Expr {
  def toLit(s: Solver): s.BoolLit =
    s.getNode(graph, n.id.name) match {
      case Some(value) => value
      case None        => throw Exception(s"Unknown node ${n.id.name}")
    }

  def toExpr(s: Solver): s.Expression = toLit(s)

  override def toString: String = s"v_${n.id.name}"
}

final case class MGraph(nodes: Set[MNode], edges: Set[MEdge])
    extends ModelElement {
  def toLit(s: Solver): s.GraphLit = s.graphLit(this)
  def exportGraph(s: Solver, file: File): Unit =
    s.exportGraph(this, file)
}

final case class MEdge(from: MNode, to: MNode, id: EdgeId) extends ModelElement

final case class MEdgeLit(edge: MEdge, graph: MGraph) extends ALit with Expr {
  def toLit(s: Solver): s.BoolLit =
    s.getEdge(graph, edge.id.name) match {
      case Some(value) => value
      case None        => throw Exception(s"Unknown edge ${edge.id.name}")
    }

  def toExpr(s: Solver): s.Expression = toLit(s)

  override def toString: String = s"v_${edge.id.name}"
}

final case class MLit(id: LitId) extends ALit with Expr {
  def toLit(s: Solver): s.BoolLit = s.boolLit(this)
  def toExpr(s: Solver): s.Expression = s.boolLit(this)

  override def toString: String = id.name
}

final case class And(l: Seq[Expr]) extends Expr {
  def toExpr(s: Solver): s.Expression = s.and(l)

  override def toString: String =
    s"""and(
       |${l.mkString("\t", ",\n\t", "\n)")}""".stripMargin
}

final case class Or(l: Seq[Expr]) extends Expr {
  def toExpr(s: Solver): s.Expression = s.or(l)

  override def toString: String =
    s"""or(
       |${l.mkString("\t", ",\n\t", "\n)")}""".stripMargin
}

final case class Implies(l: Expr, r: Expr) extends Expr {
  def toExpr(s: Solver): s.Expression = s.implies(l, r)

  override def toString: String =
    s"""implies(
       |\t$l,
       |\t$r
       |)""".stripMargin
}

final case class Not(l: Expr) extends Expr {
  def toExpr(s: Solver): s.Expression = s.not(l)

  override def toString: String =
    s"not($l)"
}

final case class Equal(l: Expr, r: Expr) extends Expr {
  def toExpr(s: Solver): s.Expression = s.eq(l, r)

  override def toString: String =
    s"""eq(
       |\t$l,
       |\t$r
       |)""".stripMargin
}

final case class Connected(graph: MGraph) {
  def toConstraint(s: Solver): s.Constraint = s.connected(graph)
}
