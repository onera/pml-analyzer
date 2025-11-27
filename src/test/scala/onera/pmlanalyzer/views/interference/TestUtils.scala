/** *****************************************************************************
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
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 * **************************************************************************** */

package onera.pmlanalyzer.views.interference

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.Tag

import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.Future
import scala.io.Source
import scala.language.postfixOps

object InterferenceTestExtension {

  object PerfTests extends Tag("PerfTests")
  object UnitTests extends Tag("UnitTests")
  object FastTests extends Tag("FastTests")

  var monosatLibraryLoaded: Boolean = true
  try {
    System.loadLibrary("monosat")
  } catch {
    case _: UnsatisfiedLinkError => monosatLibraryLoaded = false
  }

  extension [T: Analyse](x: T) {

    def test(
        max: Int,
        expectedResultsDirectoryPath: String,
        implm: SolverImplm,
        method: Method
    ): Future[Seq[Seq[MultiTransactionComparison]]] = {
      x.computeKInterference(
        List(max, x.getMaxSize).min,
        ignoreExistingAnalysisFiles = true,
        computeSemantics = false,
        verboseResultFile = false,
        onlySummary = false,
        implm,
        method
      ) map { resultFiles =>
        {
          for {
            i <- 2 to List(max, x.getMaxSize).min
            fileITF <- FileManager.extractResource(
              s"$expectedResultsDirectoryPath/${FileManager.getInterferenceAnalysisITFFileName(x.getName, i, None, None)}"
            )
            fileFree <- FileManager.extractResource(
              s"$expectedResultsDirectoryPath/${FileManager.getInterferenceAnalysisFreeFileName(x.getName, i, None, None)}"
            )
            rITFFile <- resultFiles.find(
              _.getName == FileManager.getInterferenceAnalysisITFFileName(
                x.getName,
                i,
                Some(method),
                Some(implm)
              )
            )
            rFreeFile <- resultFiles.find(
              _.getName == FileManager.getInterferenceAnalysisFreeFileName(
                x.getName,
                i,
                Some(method),
                Some(implm)
              )
            )
          } yield {
            List((fileITF, false), (fileFree, true))
              .zip(List(rITFFile, rFreeFile))
              .flatMap(p => {
                val expected = PostProcess.parseMultiTransactionFile(p._1._1)
                val found =
                  PostProcess.parseMultiTransactionFile(Source.fromFile(p._2))
                expected.diff(found).map(s => Missing(s, p._1._2)) ++ found
                  .diff(expected)
                  .map(s => Unknown(s, p._1._2))
              })
          }
        }
      }
    }
  }

  def failureMessage(diff: Seq[MultiTransactionComparison]): String = {
    if (diff.nonEmpty)
      s"""${diff.size} multi-transactions of size ${diff.head.s.size} are incorrect:
         |${diff.mkString("\n")}

         |""".stripMargin
    else
      ""
  }
}

sealed trait MultiTransactionComparison {
  val isFree: Boolean
  val s: Seq[String]
}

final case class Missing(s: Seq[String], isFree: Boolean)
    extends MultiTransactionComparison {
  override def toString: String =
    s"${s.size}-${if (isFree) "free" else "itf"} ${s.mkString("||")} not found"
}

final case class Unknown(s: Seq[String], isFree: Boolean)
    extends MultiTransactionComparison {
  override def toString: String =
    s"${s.size}-${if (isFree) "free" else "itf"} ${s.mkString("||")} not expected"
}
