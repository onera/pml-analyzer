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

package onera.pmlanalyzer.views.interference

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.operators.Analyse.ConfiguredPlatform

import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

object InterferenceTestExtension {

  extension (x: ConfiguredPlatform) {

    def test(
        max: Int,
        expectedResultsDirectoryPath: String
    ): Future[Seq[Seq[ScenarioComparison]]] = {
      x.computeKInterference(
        List(max, x.initiators.size).min,
        ignoreExistingAnalysisFiles = true,
        computeSemantics = false,
        verboseResultFile = false,
        onlySummary = false
      ) map { resultFiles =>
        {
          for {
            i <- 2 to List(max, x.initiators.size).min
            fileITF <- FileManager.extractResource(
              s"$expectedResultsDirectoryPath/${x.fullName}_itf_$i.txt"
            )
            fileFree <- FileManager.extractResource(
              s"$expectedResultsDirectoryPath/${x.fullName}_free_$i.txt"
            )
            rITFFile <- resultFiles.find(
              _.getName == s"${x.fullName}_itf_$i.txt"
            )
            rFreeFile <- resultFiles.find(
              _.getName == s"${x.fullName}_free_$i.txt"
            )
          } yield {
            List(fileITF, fileFree)
              .zip(List(rITFFile, rFreeFile))
              .flatMap(p => {
                val expected = PostProcess.parseScenarioFile(p._1)
                val found = PostProcess.parseScenarioFile(Source.fromFile(p._2))
                expected.diff(found).map(s => Missing(s)) ++ found
                  .diff(expected)
                  .map(s => Unknown(s))
              })
          }
        }
      }
    }
  }

  def failureMessage(diff: Seq[ScenarioComparison]): String = {
    if (diff.nonEmpty)
      s"""${diff.size} scenarios of size ${diff.head.s.size} are incorrect:
         |${diff.mkString("\n")}

         |""".stripMargin
    else
      ""
  }
}

sealed trait ScenarioComparison {
  val s: Seq[String]
}

final case class Missing(s: Seq[String]) extends ScenarioComparison {
  override def toString: String = s.mkString("||") + " not found"
}

final case class Unknown(s: Seq[String]) extends ScenarioComparison {
  override def toString: String = s.mkString("||") + " not expected"
}
