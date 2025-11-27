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

package onera.pmlanalyzer.views.interference.operators.analysis

import onera.pmlanalyzer.pml.model.instances.mySys.MySys
import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.InterferenceTestExtension
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.model.formalisation.{
  InterferenceCalculusProblem,
  SolverImplm
}
import org.chocosolver.solver.exception.InvalidSolutionException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class MySysAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "mySys"

  s"For ${MySys.fullName}, the analysis operator" should "compute expected semantics distribution" taggedAs FastTests in {
    val semanticsDistribution =
      MySys.getSemanticsSize(ignoreExistingFile = true)
    semanticsDistribution(2) should be(40)
    semanticsDistribution(3) should be(33)
    semanticsDistribution(4) should be(4)
  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {
    var interferenceComputationOK = true

    s"For ${MySys.fullName}, the analysis operator with $method method implemented with $implm" should "find the verified interference" taggedAs FastTests in {
      if (implm == Monosat) {
        if (!interferenceComputationOK)
          interferenceComputationOK = false
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      Try({
        Await.result(
          MySys.test(4, expectedResultsDirectoryPath, implm, method),
          10 minutes
        )
      }) match {
        case Failure(exception: InvalidSolutionException) =>
          interferenceComputationOK = false
          assume(false, exception.getMessage)
        case Failure(exception) =>
          interferenceComputationOK = false
          fail(exception.getMessage)
        case Success(diff) =>
          for { d <- diff.flatten }
            MySys.exportInterferenceGraphFromString(d.s.toSet)
          if (diff.exists(_.nonEmpty)) {
            interferenceComputationOK = false
            fail(diff.map(failureMessage).mkString("\n"))
          }
      }
    }

    it should "compute the interference based on the topological interference system export" taggedAs FastTests in {
      if (implm == Monosat) {
        if (!interferenceComputationOK)
          interferenceComputationOK = false
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      val TIS = MySys.computeTopologicalInterferenceSystem(4)
      Try({
        Await.result(
          TIS.test(4, expectedResultsDirectoryPath, implm, method),
          10 minutes
        )
      }) match {
        case Failure(exception: InvalidSolutionException) =>
          interferenceComputationOK = false
          assume(false, exception.getMessage)
        case Failure(exception) =>
          interferenceComputationOK = false
          fail(exception.getMessage)
        case Success(diff) =>
          for { d <- diff.flatten }
            MySys.exportInterferenceGraphFromString(d.s.toSet)
          if (diff.exists(_.nonEmpty)) {
            interferenceComputationOK = false
            fail(diff.map(failureMessage).mkString("\n"))
          }
      }
    }

    it should "provide a consistent semantics reduction" taggedAs FastTests in {
      assume(
        interferenceComputationOK,
        "ignoring test since interference computation failed"
      )
      if (implm == Monosat) {
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      val semanticReduction =
        MySys.computeSemanticReduction(
          implm,
          method
        )
      assume(
        semanticReduction != -1,
        "[ERROR] cannot compute semantic reduction"
      )
      semanticReduction should be(
        BigDecimal(37) / 17
      )
    }
    it should "provide a consistent graph reduction" taggedAs FastTests in {
      assume(
        interferenceComputationOK,
        "ignoring test since interference computation failed"
      )
      if (implm == Monosat) {
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      MySys.computeGraphReduction(implm, method) should be(BigDecimal(71) / 45)
    }
  }
}
