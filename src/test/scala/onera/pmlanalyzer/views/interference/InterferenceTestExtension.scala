/** *****************************************************************************
  * Copyright (c) 2021. ONERA
  * This file is part of PML Analyzer
  *
  * PML Analyzer is free software ;
  * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation ;
  * either version 2 of  the License, or (at your option) any later version.
  *
  * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this program ;
  * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
  * **************************************************************************** */

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

  extension (x:ConfiguredPlatform) {

    def computeGraphReduction(): BigDecimal = {
      val graph = x.fullServiceGraphWithInterfere()
      val systemGraphSize = (graph.keySet ++ graph.values.flatten).size + graph.flatMap( p => p._2 map { x => Set(p._1, x) } ).toSet.size
      println(s"System graph size is: $systemGraphSize")
      val (nodeSize, edgeSize) = x.getAnanlysisGraphSize()
      println(s"Interference channel graph size is: ${nodeSize + edgeSize}")
      BigDecimal(systemGraphSize) / BigDecimal(nodeSize + edgeSize)
    }

    @deprecated("Inefficient implementation, should only count the model without storing them in files")
    def computeSemanticReduction() : BigDecimal = Await.result(
      x.computeKInterference(x.initiators.size, ignoreExistingAnalysisFiles = true, verboseResultFile = false)
        .map(resultFiles =>
          BigDecimal(x.getSemanticsSize.filter(_._1 >= 3).values.sum)/
            (for {
            i <- 3 to x.initiators.size
            resultFile <- resultFiles.find(_.getName == s"${x.fullName}_itf_$i.txt") ++
              resultFiles.find(_.getName == s"${x.fullName}_free_$i.txt")
          } yield {
                PostProcess.parseScenarioFile(Source.fromFile(resultFile)).length
              }).sum),
      1 minute)



    def test(max: Int, expectedResultsDirectoryPath: String): Future[Seq[Seq[ScenarioComparison]]] = {
      for {
        i <- 2 to List(max, x.initiators.size).min
      } {
        assert(FileManager.extractResource(s"$expectedResultsDirectoryPath/${x.fullName}_itf_$i.txt").isDefined)
        assert(FileManager.extractResource(s"$expectedResultsDirectoryPath/${x.fullName}_free_$i.txt").isDefined)
      }
      x.computeKInterference(List(max, x.initiators.size).min, ignoreExistingAnalysisFiles = true, verboseResultFile = false) map {
        resultFiles => {
          for {
            i <- 2 to List(max, x.initiators.size).min
            fileITF <- FileManager.extractResource(s"$expectedResultsDirectoryPath/${x.fullName}_itf_$i.txt")
            fileFree <- FileManager.extractResource(s"$expectedResultsDirectoryPath/${x.fullName}_free_$i.txt")
            rITFFile <- resultFiles.find(_.getName == s"${x.fullName}_itf_$i.txt")
            rFreeFile <- resultFiles.find(_.getName == s"${x.fullName}_free_$i.txt")
          } yield {
            List(fileITF, fileFree)
              .zip(List(rITFFile, rFreeFile))
              .flatMap(p => {
                val expected = PostProcess.parseScenarioFile(p._1)
                val found = PostProcess.parseScenarioFile(Source.fromFile(p._2))
                expected.diff(found).map(s => Missing(s)) ++ found.diff(expected).map(s => Unknown(s))
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

case class Missing(s: Seq[String]) extends ScenarioComparison {
  override def toString: String = s.mkString("||") + " not found"
}

case class Unknown(s: Seq[String]) extends ScenarioComparison {
  override def toString: String = s.mkString("||") + " not expected"
}