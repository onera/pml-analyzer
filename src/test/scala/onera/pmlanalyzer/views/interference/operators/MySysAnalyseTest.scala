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
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.InterferenceTestExtension
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class MySysAnalyseTest extends AnyFlatSpec with should.Matchers {

  MySys.fullName should "contain the expected semantics distribution" taggedAs FastTests in {
    val semanticsDistribution =
      MySys.getSemanticsSize(ignoreExistingFile = true)
    semanticsDistribution(2) should be(40)
    semanticsDistribution(3) should be(33)
    semanticsDistribution(4) should be(4)
  }

  private val expectedResultsDirectoryPath = "mySys"

  it should "provide the verified interference and free" in {
    assume(
      InterferenceTestExtension.monosatLibraryLoaded,
      Message.monosatLibraryNotLoaded
    )
    val diff =
      Await.result(MySys.test(4, expectedResultsDirectoryPath), 10 minutes)
    if (diff.exists(_.nonEmpty)) {
      fail(diff.map(InterferenceTestExtension.failureMessage).mkString("\n"))
    }
  }

  it should "provide a consistent semantics reduction" taggedAs FastTests in {
    assume(
      InterferenceTestExtension.monosatLibraryLoaded,
      Message.monosatLibraryNotLoaded
    )
    MySys.computeSemanticReduction(ignoreExistingFiles = true) should be(
      BigDecimal(37) / 17
    )
  }

  it should "provide a consistent graph reduction" taggedAs FastTests in {
    assume(
      InterferenceTestExtension.monosatLibraryLoaded,
      Message.monosatLibraryNotLoaded
    )
    MySys.computeGraphReduction() should be(BigDecimal(71) / 34)
  }

}
