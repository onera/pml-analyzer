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

import fastparse.*
import fastparse.SingleLineWhitespace.*
import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.instances.keystone.KeystoneWithRosace
import onera.pmlanalyzer.pml.model.instances.mySys.MySys
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should

import scala.io.BufferedSource

class PostProcessTest extends AnyFlatSpecLike with should.Matchers {

  private def testParser[T](
      p: P[?] => P[T],
      source: BufferedSource,
      expected: T
  ): Unit = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      p
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res should equal(expected)
      case f: Parsed.Failure =>
        val error = f.trace().longAggregateMsg
        source.close()
        fail(error)
    }
  }

  for {
    (platform, (expectedResultPath, expectedSummary)) <- Seq(
      MySys -> (
        "mySys",
        (
          Map(2 -> BigInt(12), 3 -> BigInt(8), 4 -> BigInt(1)),
          Map(2 -> BigInt(28), 3 -> BigInt(8)),
          0.toDouble
        )
      ),
      KeystoneWithRosace -> (
        "keystone",
        (
          Map(2 -> BigInt(367), 3 -> BigInt(2774), 4 -> BigInt(14456)),
          Map(2 -> BigInt(827), 3 -> BigInt(5880), 4 -> BigInt(21092)),
          5.toDouble
        )
      )
    )
  } {
    s"For $platform, PostProcess parsers" should "parse a summary file without semantics" taggedAs UnitTests in {
      val expected = FileManager
        .getInterferenceAnalysisSummaryFileName(platform.fullName, None, None)
        .replace(".txt", "_no_semantics.txt")
      for {
        source <- FileManager.extractResource(s"$expectedResultPath/$expected")
      } yield {
        testParser(
          PostProcess.parseSummaryFile(using _),
          source,
          expectedSummary
        )
      }
    }

    it should "parse a summary file with semantics" taggedAs UnitTests in {
      val expected = FileManager
        .getInterferenceAnalysisSummaryFileName(platform.fullName, None, None)
        .replace(".txt", "_with_semantics.txt")
      for {
        source <- FileManager.extractResource(s"$expectedResultPath/$expected")
      } yield {
        testParser(
          PostProcess.parseSummaryFile(using _),
          source,
          expectedSummary
        )
      }
    }
  }
}
