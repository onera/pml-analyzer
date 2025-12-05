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

import fastparse.*
import fastparse.SingleLineWhitespace.*
import monosat.Lit
import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.views.interference.model.formalisation
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.{
  CPSat,
  Choco,
  GCode,
  Monosat
}
import onera.pmlanalyzer.views.interference.operators.PostProcess
import org.chocosolver.solver.Model as ChocoModel
import org.chocosolver.solver.constraints.{
  Operator,
  Constraint as ChocoConstraint
}
import org.chocosolver.solver.expression.discrete.relational.ReExpression
import org.chocosolver.solver.variables.{BoolVar, UndirectedGraphVar}
import org.chocosolver.util.objects.graphs.UndirectedGraph
import org.chocosolver.util.objects.setDataStructures.SetType

import java.io.{File, FileWriter}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.sys.process.Process

private[pmlanalyzer] enum SolverImplm {
  case Monosat extends SolverImplm
  case Choco extends SolverImplm
  case GCode extends SolverImplm
  case CPSat extends SolverImplm

  override def toString: String =
    this match {
      case Monosat => "monosat"
      case Choco   => "choco"
      case GCode   => "gecode"
      case CPSat   => "cp-sat"
    }
}

sealed trait Solver {
  type Expression
  type BoolLit <: Expression
  type GraphLit
  type Constraint

  val implm: SolverImplm

  protected val memoBooLit = mutable.Map.empty[ALit, BoolLit]
  protected val memoGraph = mutable.Map.empty[MGraph, GraphLit]

  def assert(lt: Expr | Connected): Unit
  def assertPB(l: Seq[ALit], c: Comparator, k: Int): Unit
  def graphLit(g: MGraph): GraphLit
  def boolLit(a: MLit): BoolLit
  def getEdge(g: MGraph, id: String): Option[BoolLit]
  def getNode(g: MGraph, id: String): Option[BoolLit]
  def and(l: Seq[Expr]): Expression
  def or(l: Seq[Expr]): Expression
  def implies(l: Expr, r: Expr): Expression
  def eq(l: Expr, r: Expr): Expression
  def not(l: Expr): Expression
  def connected(g: MGraph): Constraint
  def exportGraph(g: MGraph, file: File): File
  def enumerateSolution(toGet: Set[MLit]): mutable.Set[Set[MLit]]
  def close(): Unit
}

private[pmlanalyzer] final class ChocoSolver extends Solver {
  type BoolLit = BoolVar
  type GraphLit = UndirectedGraphVar
  type Constraint = ChocoConstraint
  type Expression = ReExpression

  override val implm: SolverImplm = Choco

  private val memoEdges = mutable.Map.empty[String, BoolLit]
  private val memoNodes = mutable.Map.empty[String, BoolLit]
  private val model: ChocoModel = ChocoModel()

  def assert(lt: Expr | Connected): Unit = lt match {
    case a: Expr      => a.toExpr(this).post()
    case c: Connected => c.toConstraint(this).post()
  }

  def assertPB(l: Seq[ALit], c: Comparator, k: Int): Unit = {
    val op = c match {
      case Comparator.LT => Operator.LT
      case Comparator.LE => Operator.LE
      case Comparator.EQ => Operator.EQ
      case Comparator.GE => Operator.GE
      case Comparator.GT => Operator.GT
    }
    model.sum(l.map(_.toLit(this)).toArray, op.toString, k).post()
  }

  def graphLit(g: MGraph): GraphLit =
    memoGraph.getOrElseUpdate(
      g, {
        // define the upper graph bound (envelop), it contains all possible nodes and edges
        val UB =
          UndirectedGraph(
            model,
            g.nodes.size,
            SetType.BITSET,
            SetType.BITSET,
            false
          )
        // define the lesser graph bound (kernel), it contains the mandatory nodes and edges, here the empty graph
        val LB =
          UndirectedGraph(
            model,
            g.nodes.size,
            SetType.BITSET,
            SetType.BITSET,
            false
          )

        // Add the nodes to upper graph
        val nodeMap = (for {
          (n, i) <- g.nodes.toSeq.sortBy(_.id.name).zipWithIndex
        } yield {
          UB.addNode(i)
          n -> i
        }).toMap

        // Add the edges to upper graph
        for {
          e <- g.edges
        } {
          UB.addEdge(nodeMap(e.from), nodeMap(e.to))
        }

        // Create a graph variable
        val mG = model.graphVar("g", LB, UB)

        // Fetch the node variables from g
        val nodeVars = nodeMap.transform((k, _) =>
          memoNodes.getOrElseUpdate(k.id.name, model.boolVar(k.id.name))
        )
        model
          .nodesChanneling(
            mG,
            nodeVars.keySet.toArray.sortBy(nodeMap).map(nodeVars)
          )
          .post()

        // Fetch the edge variables from g
        val edgeVars =
          g.edges
            .map(e =>
              (e.from, e.to) ->
                memoEdges.getOrElseUpdate(
                  e.id.name,
                  model.boolVar(s"${e.from.id.name}--${e.to.id.name}")
                )
            )
            .toMap

        for {
          ((l, r), v) <- edgeVars
        } {
          model.edgeChanneling(mG, v, nodeMap(l), nodeMap(r)).post()
        }
        mG
      }
    )

  def boolLit(a: MLit): BoolLit =
    memoBooLit.getOrElseUpdate(
      a, {
        model.boolVar(a.id.name)
      }
    )

  def getEdge(g: MGraph, id: String): Option[BoolLit] =
    memoEdges.get(id)

  def getNode(g: MGraph, id: String): Option[BoolLit] =
    memoNodes.get(id)

  def and(l: Seq[Expr]): Expression =
    if (l.isEmpty) {
      model.boolVar(true)
    } else {
      l.head.toExpr(this).and(l.tail.map(_.toExpr(this)).toArray: _*)
    }

  def or(l: Seq[Expr]): Expression =
    if (l.isEmpty) {
      model.boolVar(false)
    } else {
      l.head.toExpr(this).or(l.tail.map(_.toExpr(this)).toArray: _*)
    }

  def implies(l: Expr, r: Expr): Expression =
    l.toExpr(this).imp(r.toExpr(this))

  def eq(l: Expr, r: Expr): Expression =
    l.toExpr(this).eq(r.toExpr(this))

  def not(l: Expr): Expression =
    l.toExpr(this).not()

  def connected(g: MGraph): Constraint =
    model.connected(graphLit(g))

  def exportGraph(g: MGraph, file: File): File = {
    val graphWriter = new FileWriter(file)
    graphWriter.write(g.toLit(this).getUB.graphVizExport())
    graphWriter.close()
    file
  }

  // WARNING Choco can send false solutions that are discarded
  def enumerateSolution(toGet: Set[MLit]): mutable.Set[Set[MLit]] = {
    val models = mutable.Set.empty[Set[MLit]]
    val solver = model.getSolver
    while (solver.solve()) {
      models += (for {
        l <- toGet
        if l.toLit(this).getValue == 1
      } yield {
        l
      })
    }
    models
  }

  def close(): Unit = {}
}

private[pmlanalyzer] final class MonoSatSolver extends Solver {
  type BoolLit = monosat.Lit
  type GraphLit = monosat.Graph
  type Constraint = monosat.Lit
  type Expression = monosat.Lit

  override val implm: SolverImplm = Monosat

  private val solver: monosat.Solver = monosat.Solver("-decide-theories")

  def assert(lt: Expr | Connected): Unit = lt match {
    case a: Expr      => solver.assertTrue(a.toExpr(this))
    case c: Connected => solver.assertTrue(c.toConstraint(this))
  }

  def assertPB(l: Seq[ALit], c: Comparator, k: Int): Unit = {
    val comparator = c match {
      case Comparator.LT => monosat.Comparison.LT
      case Comparator.LE => monosat.Comparison.LEQ
      case Comparator.EQ => monosat.Comparison.EQ
      case Comparator.GE => monosat.Comparison.GEQ
      case Comparator.GT => monosat.Comparison.GT
    }
    solver.assertPB(l.map(_.toLit(this)).asJava, comparator, k)
  }

  def graphLit(g: MGraph): GraphLit =
    memoGraph.getOrElseUpdate(
      g, {

        val mG = new monosat.Graph(solver)

        val nodeMap = (for {
          n <- g.nodes
        } yield {
          val res = mG.addNode(n.id.name)
          // must add a loop edge to encode that a node can exist even
          // if no edges are activated
          mG.addEdge(res, res, n.id.name)
          n -> res
        }).toMap

        for {
          e <- g.edges
        }
          mG.addUndirectedEdge(nodeMap(e.from), nodeMap(e.to), e.id.name)
        mG
      }
    )

  def boolLit(a: MLit): BoolLit = {
    memoBooLit.getOrElseUpdate(a, new monosat.Lit(solver, a.id.name))
  }

  def getEdge(g: MGraph, id: String): Option[BoolLit] = {
    graphLit(g).getEdgeVars.asScala.find(_.name() == id)
  }

  /**
   * A node is activated iff the loop edge is activated
   * @param g the graph literal
   * @param id the id of the node to get
   * @return a boolean variable true iff the node belongs to the graph
   */
  def getNode(g: MGraph, id: String): Option[BoolLit] = {
    graphLit(g).getEdgeVars.asScala.find(_.name() == id)
  }

  def and(l: Seq[Expr]): Expression =
    solver.and(l.map(_.toExpr(this)).asJava)

  def or(l: Seq[Expr]): Expression =
    solver.or(l.map(_.toExpr(this)).asJava)

  def implies(l: Expr, r: Expr): Expression =
    solver.implies(l.toExpr(this), r.toExpr(this))

  def eq(l: Expr, r: Expr): Expression =
    solver.equal(l.toExpr(this), r.toExpr(this))

  def not(l: Expr): Expression =
    solver.not(l.toExpr(this))

  def connected(g: MGraph): Constraint = {
    val mG = graphLit(g)
    val pairs = for {
      ss <- g.nodes.subsets(2)
      from <- getNode(g, ss.head.id.name)
      to <- getNode(g, ss.last.id.name)
    } yield {
      solver.implies(
        solver.and(from, to),
        mG.reaches(mG.getNode(ss.head.id.name), mG.getNode(ss.last.id.name))
      )
    }
    solver.and(pairs.toSeq.asJava)
  }

  def exportGraph(g: MGraph, file: File): File = {
    val graphWriter = new FileWriter(file)
    graphWriter.write(graphLit(g).draw())
    graphWriter.close()
    file
  }

  def enumerateSolution(toGet: Set[MLit]): mutable.Set[Set[MLit]] = {
    val models = mutable.Set.empty[Set[MLit]]
    while (solver.solve()) {
      models += (for {
        l <- toGet
        if l.toLit(this).value
      } yield {
        l
      })
      solver.assertOr(
        (for {
          k <- toGet
        } yield {
          val l = k.toLit(this)
          if (l.value)
            not(k)
          else
            l
        }).toSeq.asJava
      )
    }
    models
  }

  def close(): Unit = solver.close()
}

private[pmlanalyzer] sealed abstract class MiniZincSolver extends Solver {
  type Expression = String
  type BoolLit = String
  type GraphLit = Graph
  type Constraint = String

  final case class Graph(
      nMap: Map[String, (Int, BoolLit)],
      eMap: Map[String, (Int, BoolLit)],
      pre: Array[Int],
      post: Array[Int]
  )

  private val graphLitCache = mutable.Map.empty[MGraph, Graph]
  private val boolLitCache = mutable.Map.empty[MLit, String]

  // FIXME First step with file, but consider Stream from terminal
  private val miniZincFile: File =
    FileManager.analysisDirectory.getFile("exportMiniZinc.mzn")
  protected val fileWriter = FileWriter(miniZincFile)

  fileWriter.write("include \"connected.mzn\";\n")

  private def formatName(s: String) =
    s.replaceAll("[<>|]", "")
      .replaceAll("\\$|--", "_")

  def assert(lt: Expr | Connected): Unit =
    fileWriter.write(
      s"constraint(${lt match {
          case e: Expr      => e.toExpr(this)
          case c: Connected => c.toConstraint(this)
        }});\n"
    )

  def assertPB(l: Seq[ALit], c: Comparator, k: Int): Unit =
    fileWriter.write(
      s"constraint(${l.map(_.toLit(this)).mkString("(", " + ", ")")}${c match {
          case Comparator.LT => "<"
          case Comparator.LE => "<="
          case Comparator.EQ => "="
          case Comparator.GE => ">="
          case Comparator.GT => ">"
        }} $k);\n"
    )

  def graphLit(g: MGraph): GraphLit = graphLitCache.getOrElseUpdate(
    g, {
      val nMap =
        (for {
          (n, i) <- g.nodes.toSeq.sortBy(_.id.name).zipWithIndex
        } yield {
          n -> (i + 1, s"n$i")
        }).toMap
      val eMap =
        (for {
          (e, i) <- g.edges.toSeq.sortBy(_.id.name).zipWithIndex
        } yield {
          e -> (i + 1, s"e${nMap(e.from)._1}${nMap(e.to)._1}")
        }).toMap
      val pre =
        for {
          (eId, _) <- eMap.toArray.sortBy(_._2._1)
        } yield nMap(eId.from)._1
      val post =
        for {
          (eId, _) <- eMap.toArray.sortBy(_._2._1)
          (nId, _) <- nMap.get(eId.to)
        } yield nId

      fileWriter.write(
        s"""%% Nodes definition
           |set of int: Node = 1..${nMap.size};
           |${nMap.toSeq
            .sortBy(_._2._1)
            .map((k, v) => s"Node: ${formatName(k.id.name)} = ${v._1};")
            .mkString("\n")}
           |%% Node variables definition
           |${nMap.toSeq
            .sortBy(_._2._1)
            .map((k, v) => s"var bool: ${v._2};")
            .mkString("\n")}
           |%% Edges definition
           |set of int: Edge = 1..${eMap.size};
           |${eMap.toSeq
            .sortBy(_._2._1)
            .map((k, v) => s"Edge: ${formatName(k.id.name)} = ${v._1};")
            .mkString("\n")}
           |%% Edge variables definition
           |${eMap.toSeq
            .sortBy(_._2._1)
            .map((k, v) => s"var bool: ${v._2};")
            .mkString("\n")}
           |%% Graph definition
           |array[Edge] of Node: pre = ${pre.mkString("[", ", ", "]")};
           |array[Edge] of Node: post = ${post.mkString("[", ", ", "]")};
           |array[Node] of var bool: ns = ${nMap.keySet.toSeq
            .sortBy(x => nMap(x)._1)
            .map(x => nMap(x)._2)
            .mkString("[", ", ", "]")};
           |array[Edge] of var bool: es = ${eMap.keySet.toSeq
            .sortBy(x => eMap(x)._1)
            .map(x => eMap(x)._2)
            .mkString("[", ", ", "]")};
           |""".stripMargin
      )
      Graph(
        nMap.map((k, v) => k.id.name -> v),
        eMap.map((k, v) => k.id.name -> v),
        pre,
        post
      )
    }
  )

  def boolLit(a: MLit): BoolLit =
    boolLitCache.getOrElseUpdate(
      a, {
        fileWriter.write(s"var bool: ${formatName(a.id.name)};\n")
        formatName(a.id.name)
      }
    )

  def getEdge(g: MGraph, id: String): Option[BoolLit] =
    for {
      (_, b) <- graphLit(g).eMap.get(id)
    } yield b

  def getNode(g: MGraph, id: String): Option[BoolLit] =
    for {
      (_, b) <- graphLit(g).nMap.get(id)
    } yield b

  def and(l: Seq[Expr]): Expression =
    if (l.isEmpty)
      "true"
    else
      l.map(_.toExpr(this)).mkString("((", ") /\\ (", "))")

  def or(l: Seq[Expr]): Expression =
    if (l.isEmpty)
      "false"
    else
      l.map(_.toExpr(this)).mkString("((", ") \\/ (", "))")

  def implies(l: Expr, r: Expr): Expression =
    s"${l.toExpr(this)} -> ${r.toExpr(this)}"

  def eq(l: Expr, r: Expr): Expression =
    s"${l.toExpr(this)} = ${r.toExpr(this)}"

  def not(l: Expr): Expression =
    s"not ${l.toExpr(this)}"

  def connected(g: MGraph): Constraint =
    "connected(pre,post,ns,es)"

  def exportGraph(g: MGraph, file: File): File = ???

  def close(): Unit = {}

  private def parseValuation[$: P] =
    P(
      PostProcess.parseAtomicTransactionId ~ "=" ~ PostProcess.parseWord.! ~ "\n"
    )

  private def parseModel[$: P] =
    P(parseValuation.rep(min = 1) ~ "-".rep(min = 2) ~ "\n").map(
      _.collect { case (id, "true") =>
        id
      }
    )

  private def parseUnsat[$: P] =
    P("=====" ~ "UNSATISFIABLE" ~ "=====" ~ "\n").map(_ =>
      Seq.empty[Seq[String]]
    )

  private def parseModels[$: P] =
    P(Start ~ (parseModel.rep ~ "=".rep(min = 2) ~ "\n" | parseUnsat) ~ End)

  protected def enumerateSolution(
      toGet: Set[MLit],
      implm: SolverImplm
  ): mutable.Set[Set[MLit]] = {
    val litNames = toGet.map(x => x -> x.toLit(this)).toMap
    val mLitNames = litNames.groupMapReduce(_._2)(_._1)((l, r) => l)
    fileWriter.write(s"""output [
         |${litNames.values.map(x => s"\"$x = \\($x)\\n\"").mkString(",\n")}
         |]""".stripMargin)
    fileWriter.flush()
    fileWriter.close()
    val result = Process(
      s"minizinc -a --solver $implm ${miniZincFile.getPath}"
    ).lazyLines
    parse(result.iterator.map(_ + "\n"), parseModels(using _)) match {
      case Parsed.Success(res, _) =>
        mutable.Set(res.map(_.map(mLitNames).toSet): _*)
      case f: Parsed.Failure =>
        println(f.msg)
        mutable.Set.empty[Set[MLit]]
    }
  }
}

private[pmlanalyzer] final class GCodeSolver extends MiniZincSolver {

  val implm: SolverImplm = GCode

  def enumerateSolution(toGet: Set[MLit]): mutable.Set[Set[MLit]] =
    enumerateSolution(toGet, GCode)

}

private[pmlanalyzer] final class CPSatSolver extends MiniZincSolver {

  val implm: SolverImplm = CPSat

  def enumerateSolution(toGet: Set[MLit]): mutable.Set[Set[MLit]] =
    enumerateSolution(toGet, CPSat)
}

private[pmlanalyzer] object Solver {

  def apply(implm: SolverImplm): Solver = {
    implm match {
      case SolverImplm.Monosat => MonoSatSolver()
      case SolverImplm.Choco   => ChocoSolver()
      case SolverImplm.GCode   => GCodeSolver()
      case SolverImplm.CPSat   => CPSatSolver()
    }
  }
}
