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

package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.examples.mySys.MySysExport.MySys
import onera.pmlanalyzer.pml.exporters.FileManager
import org.chocosolver.solver.Model
import org.chocosolver.solver.constraints.Operator
import org.chocosolver.solver.variables.{BoolVar, UndirectedGraphVar}
import org.chocosolver.util.objects.graphs.UndirectedGraph
import org.chocosolver.util.objects.setDataStructures.SetType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.collection.mutable
import scala.concurrent.duration.{Duration, MILLISECONDS}

class MySysTestChoco extends AnyFlatSpec with should.Matchers {
  /**
   * Create transaction boolean variables
   */
  val transactions: Array[String] = Array("t11", "t12", "t13", "t14", "t21", "t22", "t23", "t24", "t25", "t26", "t31", "t41")

  val exclusiveTr: Map[String, Array[String]] =
    Map(
      "t11" -> Array("t12", "t13", "t14"),
      "t31" -> Array("t22", "t23", "t24", "t25", "t26"),
      "t12" -> Array("t11", "t13", "t14"),
      "t22" -> Array("t21", "t31", "t23", "t24", "t25", "t26"),
      "t13" -> Array("t11", "t12", "t14"),
      "t23" -> Array("t21", "t31", "t22", "t24", "t25", "t26"),
      "t14" -> Array("t11", "t12", "t13"),
      "t24" -> Array("t21", "t31", "t22", "t23", "t25", "t26"),
      "t41" -> Array.empty[String],
      "t25" -> Array("t21", "t31", "t22", "t23", "t24", "t26"),
      "t21" -> Array("t22", "t23", "t24", "t25", "t26"),
      "t26" -> Array("t21", "t31", "t22", "t23", "t24", "t25")
    )

  val nodes: Map[String, Int] = Seq(
    "sram_ld_st",
    "msmc_ld",
    "msmc_st",
    "ddr_ld_st",
    "ddr-ctrl_ld_st",
    "axi-bus_ld",
    "axi-bus_st",
    "dma_ld, dma-reg_ld",
    "dma_st, dma-reg_ld").zipWithIndex.toMap

  val edges: Seq[(String, String)] = Seq(
    "axi-bus_st" -> "msmc_st",
    "axi-bus_st" -> "ddr-ctrl_ld_st",
    "axi-bus_st" -> "ddr_ld_st",
    "axi-bus_ld" -> "msmc_ld",
    "axi-bus_ld" -> "ddr-ctrl_ld_st",
    "axi-bus_ld" -> "ddr_ld_st",
    "axi-bus_ld" -> "sram_ld_st",
    "msmc_st" -> "ddr-ctrl_ld_st",
    "msmc_st" -> "ddr_ld_st",
    "msmc_st" -> "sram_ld_st",
    "msmc_ld" -> "ddr-ctrl_ld_st",
    "msmc_ld" -> "ddr_ld_st",
    "msmc_ld" -> "sram_ld_st",
    "ddr-ctrl_ld_st" -> "ddr_ld_st",
    "sram_ld_st" -> "dma_ld, dma-reg_ld",
    "sram_ld_st" -> "dma_st, dma-reg_ld",
    "dma_st, dma-reg_ld" -> "dma_ld, dma-reg_ld"
  )

  //Association of transactions using a given channel
  val nodeToTr: Map[String, Array[String]] = Map(
    "axi-bus_ld" -> Array("t12", "t22", "t24"),
    "axi-bus_st" -> Array("t14", "t23", "t25"),
    "msmc_ld" -> Array("t12", "t22", "t24"),
    "msmc_st" -> Array("t14", "t23", "t25", "t41"),
    "sram_ld_st" -> Array("t22", "t25", "t31", "t41"),
    "ddr-ctrl_ld_st" -> Array("t12", "t14", "t23", "t24"),
    "ddr_ld_st" -> Array("t12", "t14", "t23", "t24"),
    "dma_ld, dma-reg_ld" -> Array("t31", "t21"),
    "dma_st, dma-reg_ld" -> Array("t31", "t21")
  )

  def compare(
               found: Set[(Set[String], String)],
               expected: Set[Set[String]],
               isITF: Boolean
             ): Unit = {
    val remaining = found.map(_._1).diff(expected)
    val missing =
      expected.diff(found.map(_._1)).map(s => s.toArray.sorted.mkString(" || "))
    if (remaining.nonEmpty) {
      val remainingAngGraph =
        for {
          (n, g) <- found
          if remaining.contains(n)
        } yield {
          s"${n.toArray.sorted.mkString(" || ")}\n $g"
        }

      println(s"[ERROR] following ${
        if (isITF) "itf" else "free"
      } are not expected \n${remainingAngGraph.mkString("\n")}")
    }
    if (missing.nonEmpty) {
      println(
        s"[ERROR] following ${if (isITF) "itf" else "free"} are missing \n${missing.mkString("\n")}"
      )
    }
    if (missing.nonEmpty || remaining.nonEmpty)
      fail()
  }

  case class Problem(
                      m: Model,
                      g: UndirectedGraphVar,
                      nodeVars: Map[String, BoolVar],
                      transactionVar: Map[String, BoolVar],
                      trToNode: Map[String, Array[String]]
                    )

  def buildSemanticAndGraphModel(k: Int): Problem = {
    val model: Model = Model()

    val transactionVar =
      (for {
        id <- transactions
      } yield id -> model.boolVar(id)).toMap

    // Add constraint C^1_{\Sys} i.e. transactions should not be exclusive
    for {
      (tr, ex) <- exclusiveTr
      if ex.nonEmpty
    } {
      val notEx = ex.map(tr2 => transactionVar(tr2).not())
      transactionVar(tr).imp(notEx.head.and(notEx.tail: _*)).post()
    }

    // Add constraint C^2_{\Sys} cardinality constraint
    model.sum(transactionVar.values.toArray, Operator.EQ.toString, k).post()

    // define the upper graph bound (envelop), it contains all possible nodes and edges
    val UB =
      UndirectedGraph(model, nodes.size, SetType.BITSET, SetType.BITSET, false)
    // define the lesser graph bound (kernel), it contains the mandatory nodes and edges, here the empty graph
    val LB =
      UndirectedGraph(model, nodes.size, SetType.BITSET, SetType.BITSET, false)

    // Add the nodes to upper graph
    for {
      (_, i) <- nodes
    } {
      UB.addNode(i)
    }

    // Add the edges to upper graph
    for {
      (l, r) <- edges
    } {
      UB.addEdge(nodes(l), nodes(r))
    }

    // Create a graph variable
    val g: UndirectedGraphVar = model.graphVar("q", LB, UB)

    // Fetch the node variables from g
    val nodeVars = nodes.transform((k, _) => model.boolVar(k))
    model
      .nodesChanneling(g, nodeVars.keySet.toArray.sortBy(nodes).map(nodeVars))
      .post()

    // Fetch the edge variables from g
    val edgeVars =
      edges.map((l, r) => (l, r) -> model.boolVar(s"${l}--$r")).toMap
    for {
      ((l, r), v) <- edgeVars
    } {
      model.edgeChanneling(g, v, nodes(l), nodes(r)).post()
    }

    val trToNode =
      (for {
        tr <- transactions
        nodes = nodeToTr.collect({ case (k, v) if v.contains(tr) => k }).toArray
      } yield {
        tr -> nodes
      }).toMap

    // Add constraint C_\Node i.e. node belongs to graph iff at least two transactions using it are activated
    for {
      (nodeId, transactions) <- nodeToTr
    } {
      val sumTransactions =
        model.sum(s"sum$nodeId", transactions.map(transactionVar).toArray: _*)
      nodeVars(nodeId).eq(sumTransactions.ge(2)).post()
    }

    // Retrieve the edges used by transactions
    val edgeToTr = (for {
      (l, r) <- edges
      commonTr = nodeToTr(l).toSet.intersect(nodeToTr(r).toSet)
    } yield {
      if (commonTr.isEmpty) {
        println(s"[ERROR] edges must always be used by one transaction")
      }
      (l, r) -> commonTr.map(transactionVar).toArray
    }).toMap

    // Add constraint C_\Edge i.e. edge belongs to graph iff the nodes are in the graph and one transaction using the
    // edge is activated
    for {
      ((l, r), e) <- edgeVars
    } {
      val usedTr = edgeToTr((l, r))
      e.eq(nodeVars(l).and(nodeVars(r).and(usedTr.head.or(usedTr.tail: _*))))
        .post()
    }
    Problem(model, g, nodeVars, transactionVar, trToNode)
  }

  def buildSolveAndCompareITFModel(k: Int): Unit = {
    val Problem(model, g, nodeVars, transactionVar, trToNode) =
      buildSemanticAndGraphModel(k)

    // Defining connectivity constraint
    model.connected(g).post()

    // Defining empty or non-empty constraint
    model.or(nodeVars.values.toArray: _*).post()

    // Defining transaction contribution to channel constraint
    val trContribToGraph =
      for {
        (tr, n) <- trToNode.toArray
      } yield {
        if (n.isEmpty)
          transactionVar(tr).not()
        else {
          val nV = n.map(nodeVars)
          transactionVar(tr).imp(nV.head.or(nV.tail: _*))
        }
      }
    trContribToGraph.head.and(trContribToGraph.tail: _*).post()

    val itf = mutable.Set.empty[(Set[String], String)]
    val solver = model.getSolver

    val start = System.currentTimeMillis()
    while (solver.solve()) {
      val result = transactionVar
        .collect({ case (_, v) if v.getValue == 1 => v.getName })
        .toSet
      itf += ((result, g.getValue.toString))
    } // implicitly enumerate solutions

    println(
      s"[INFO] $k multi-transaction analysis itf: ${itf.size} done in ${Duration(System.currentTimeMillis() - start, MILLISECONDS)}"
    )

    for {
      fileITF <- FileManager.extractResource(
        s"mySys/${FileManager.getInterferenceAnalysisITFFileName(MySys, k)}"
      )
    } yield {
      val expectedITF =
        PostProcess.parseMultiTransactionFile(fileITF).map(_.toSet).toSet
      compare(itf.toSet, expectedITF, true)
    }
  }

  def buildSolveAndCompareFreeModel(k: Int): Unit = {
    val Problem(model, g, nodeVars, transactionVar, _) =
      buildSemanticAndGraphModel(k)

    model.and(nodeVars.values.map(_.not()).toArray: _*).post

    val free = mutable.Set.empty[(Set[String], String)]
    val solver = model.getSolver

    val start = System.currentTimeMillis()
    while (solver.solve()) {
      val result = transactionVar
        .collect({ case (_, v) if v.getValue == 1 => v.getName })
        .toSet
      free += ((result, g.getValue.toString))
    } // implicitly enumerate solutions

    println(
      s"[INFO] $k multi-transaction analysis free: ${free.size} done in ${Duration(System.currentTimeMillis() - start, MILLISECONDS)}"
    )

    for {
      fileFree <- FileManager.extractResource(
        s"mySys/${FileManager.getInterferenceAnalysisFreeFileName(MySys, k)}"
      )
    } yield {
      val expectedFree =
        PostProcess.parseMultiTransactionFile(fileFree).map(_.toSet).toSet
      compare(free.toSet, expectedFree, false)
    }
  }

  "Choco" should "find the same results as MONOSAT" in {
    for { k <- 2 to 4 } {
      buildSolveAndCompareITFModel(k)
      buildSolveAndCompareFreeModel(k)
    }
  }
}