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

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Platform}
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators._
import onera.pmlanalyzer.views.interference.operators.Analyse.ConfiguredPlatform

import java.io.{File, FileWriter}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.{BufferedSource, Source}
import scala.math.Ordering.Implicits._

/** Base trait providing proof that an element is post processable
  * @tparam T
  *   the type of the component (contravariant)
  */
private[operators] trait PostProcess[-T] {
  def interferenceDiff(x: T, that: Platform): Seq[File]

  def parseITFScenarioFile(x: T): Array[Seq[String]]

  def parseITFScenarioFile(x: T, n: Int): Array[Seq[String]]

  def parseFreeScenarioFile(x: T, n: Int): Array[Seq[String]]

  def sortPLByITFImpact(x: T, max: Option[Int]): Future[Set[File]]

  def sortMultiPathByITFImpact(x: T, max: Option[Int]): Future[Set[File]]

}

object PostProcess {

  /* ------------------------------------------------------------------------------------------------------------------
   * EXTENSION METHODS
   * --------------------------------------------------------------------------------------------------------------- */

  /** If an element x of type T can be post processed then the operator can be
    * used as follows:
    *
    * If y is a [[pml.model.hardware.Platform]] then we can compare the
    * interferences computed for x and y {{{x.interferenceDiff(y)}}} Try to find
    * and parse itf results for the considered element
    * {{{x.parseITFScenarioFile()}}} Try to find and parse the n-itf results for
    * the considered element {{{x.parseITFScenarioFile(n)}}} Try to find and
    * parse the n-itf-free results for the considered element
    * {{{x.parseFreeScenarioFile(n)}}} Compute for each hardware component the
    * number of up to n-itf scenario where the component is involved in the
    * interference channel. The result is provided in a file of the analysis
    * directory {{{x.sortPLByITFImpact(n)}}} Compute for each multi path
    * transaction the number of up to itf scenario involving at least one of its
    * branches The result is provided in a file of the analysis directory
    * {{{x.sortMultiPathByITFImpact(n)}}}
    */
  trait Ops {

    /** Extension method class
      */
    extension [T](self: T) {

      /** Compare the interference results with another element and store the
        * result in a dedicated file in the analysis folder
        * @param that
        *   the other element
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the location of the result files
        */
      def interferenceDiff(that: Platform)(using
          ev: PostProcess[T]
      ): Seq[File] = ev.interferenceDiff(self, that)

      /** Try to find and parse itf results for the considered element
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the set of set of scenario identifiers that are interference
        *   scenarios
        */
      def parseITFScenarioFile()(using ev: PostProcess[T]): Array[Seq[String]] =
        ev.parseITFScenarioFile(self)

      /** Try to find and parse the n-itf results for the considered element
        * @param n
        *   the maximal size of transaction per itf scenario
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the set of set of scenario identifiers that are interference
        *   scenarios
        */
      def parseITFScenarioFile(n: Int)(using
          ev: PostProcess[T]
      ): Array[Seq[String]] = ev.parseITFScenarioFile(self, n)

      /** Try to find and parse the n-itf-free results for the considered
        * element
        * @param n
        *   the maximal size of transaction per itf scenario
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the set of set of scenario identifiers that are interference free
        *   scenarios
        */
      def parseFreeScenarioFile(n: Int)(using
          ev: PostProcess[T]
      ): Array[Seq[String]] = ev.parseFreeScenarioFile(self, n)

      /** Compute for each hardware component the number of itf scenario where
        * the component is involved in the interference channel The result is
        * provided in a file of the analysis directory
        * @param max
        *   the optional maximal size of the considered itf
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the location of the result files
        */
      def sortPLByITFImpact(max: Option[Int])(using
          ev: PostProcess[T]
      ): Set[File] =
        Await.result(ev.sortPLByITFImpact(self, max), Duration.Inf)

      /** Compute for each multi path transaction the number of itf scenario
        * involving at least one of its branches The result is provided in a
        * file of the analysis directory
        * @param max
        *   the optional maximal size of the considered itf
        * @param ev
        *   the proof that any element of T is analysable
        * @return
        *   the location of the result files
        */
      def sortMultiPathByITFImpact(max: Option[Int])(using
          ev: PostProcess[T]
      ): Set[File] =
        Await.result(ev.sortMultiPathByITFImpact(self, max), Duration.Inf)
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** A platform is post processable
    */
  given PostProcess[ConfiguredPlatform] with {

    def interferenceDiff(x: ConfiguredPlatform, that: Platform): Seq[File] = {
      for {
        size <- 2 to Math.min(x.initiators.size, that.initiators.size)
        thisITFFile <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(x, size)
        )
        thatITFFile <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(that, size)
        )
      } yield {
        val file = FileManager.analysisDirectory.getFile(
          s"${x.fullName}_diff_${that.fullName}_itf_$size.txt"
        )
        val sThisITF = Source.fromFile(thisITFFile)
        val sThatITF = Source.fromFile(thatITFFile)
        val thisITF = parseScenarioFile(sThisITF)
        val thatITF = parseScenarioFile(sThatITF)
        sThatITF.close()
        sThisITF.close()
        val thisDiffThat = thisITF.diff(thatITF).sorted
        val thatDiffThis = thatITF.diff(thisITF).sorted
        val writer = new FileWriter(file)
        if (thisDiffThat.isEmpty)
          writer.write(s"All itf of ${x.fullName} are in ${that.fullName}\n")
        else {
          writer.write(
            s"The following ${thisDiffThat.length} itf of ${x.fullName} are not in ${that.fullName}\n"
          )
          thisDiffThat.foreach(d => writer.write(s"$d\n"))
        }
        if (thatDiffThis.isEmpty)
          writer.write(s"All itf of ${that.fullName} are in ${x.fullName}\n")
        else {
          writer.write(
            s"The following ${thatDiffThis.length} itf in ${that.fullName} are not in ${x.fullName}\n"
          )
          thatDiffThis.foreach(d => writer.write(s"$d\n"))
        }
        writer.flush()
        writer.close()
        println(
          Message.successfulITFDifferenceExportInfo(
            size,
            x.fullName,
            that.fullName,
            file.getAbsolutePath
          )
        )
        file
      }
    }

    def parseITFScenarioFile(x: ConfiguredPlatform): Array[Seq[String]] =
      for {
        k <- (2 to x.initiators.size).toArray
        sc <- parseITFScenarioFile(x, k)
      } yield sc

    def parseITFScenarioFile(
        x: ConfiguredPlatform,
        n: Int
    ): Array[Seq[String]] = {
      for {
        file <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(x, n)
        )
      } yield {
        val s = Source.fromFile(file)
        val result = parseScenarioFile(s)
        s.close()
        result
      }
    } getOrElse Array.empty

    def parseFreeScenarioFile(x: ConfiguredPlatform): Array[Seq[String]] =
      for {
        k <- (2 to x.initiators.size).toArray
        sc <- parseFreeScenarioFile(x, k)
      } yield sc

    def parseFreeScenarioFile(
        x: ConfiguredPlatform,
        n: Int
    ): Array[Seq[String]] = {
      for {
        file <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisFreeFileName(x, n)
        )
      } yield {
        val s = Source.fromFile(file)
        val result = parseScenarioFile(s)
        s.close()
        result
      }
    } getOrElse Array.empty

    def sortPLByITFImpact(
        x: ConfiguredPlatform,
        max: Option[Int]
    ): Future[Set[File]] = {
      x.computeKInterference(
        max.getOrElse(x.initiators.size),
        ignoreExistingAnalysisFiles = false,
        computeSemantics = false,
        verboseResultFile = false,
        onlySummary = false
      ) map { resultFiles =>
        resultFiles
          .filter(_.getName.contains("channel"))
          .map(resultFile => {
            val size =
              resultFile.getName.split("\\D+").filter(_.nonEmpty).last.toInt
            val file = FileManager.analysisDirectory
              .getFile(s"${x.fullName}_HW_importance_factor_itf_$size.txt")
            val writer = new FileWriter(file)
            writer.write(
              PLInvolvedInITF(x, resultFile).toSeq
                .sortBy(-_._2)
                .map(p => s"${p._1},${p._2}")
                .mkString("\n")
            )
            writer.close()
            file
          })
      }
    }

    private def PLInvolvedInITF(
        x: ConfiguredPlatform,
        file: File
    ): Map[Hardware, Int] = {
      import x._
      val results = parseChannel(Source.fromFile(file))
      val plByScenario = results
        .map(p =>
          p._1
            .flatMap(s =>
              x.directHardware.filter(_.services.exists(s2 => s2.toString == s))
            )
            .toSet -> p._2
        )
      x.directHardware.foldLeft(Map.empty[Hardware, Int])((localMap, pl) => {
        val impacted = plByScenario.filter(_._1.contains(pl)).map(_._2).sum
        if (impacted > 0)
          localMap.updated(pl, localMap.getOrElse(pl, 0) + impacted)
        else
          localMap
      })
    }

    def parseChannel(source: BufferedSource): Seq[(Seq[String], Int)] = {
      val res = source
        .getLines()
        .filter(_.head == '{')
        .map(s => {
          val split = s.split(":")
          val services = split.head.split("[{, }]").filter(_.nonEmpty).toSeq
          val size = (for {
            s <- split.last.split(" ").find(_.nonEmpty)
          } yield s.toInt).getOrElse(-1)
          services -> size
        })
        .toSeq
      source.close()
      res
    }

    def sortMultiPathByITFImpact(
        x: ConfiguredPlatform,
        max: Option[Int]
    ): Future[Set[File]] =
      x.computeKInterference(
        max.getOrElse(x.initiators.size),
        ignoreExistingAnalysisFiles = false,
        computeSemantics = false,
        verboseResultFile = false,
        onlySummary = false
      ) map { resultFiles =>
        {
          val multiPathsTransactions = x match {
            case c: TransactionLibrary =>
              x.multiPathsTransactions.flatMap(s => {
                val transactionNames = s.map(x.transactionsName)
                (for {
                  scenarios <- c.scenarioUserName.get(transactionNames)
                  if scenarios.nonEmpty
                } yield {
                  scenarios.map(x => Set(x.toString))
                }) getOrElse Set(transactionNames.map(_.toString))
              })
            case _ =>
              x.multiPathsTransactions.map(
                _.map(x.transactionsName).map(_.toString)
              )
          }
          resultFiles
            .filter(_.getName.contains("itf"))
            .map(resultFile => {
              val size =
                resultFile.getName.split("\\D+").filter(_.nonEmpty).last.toInt
              val file = FileManager.analysisDirectory.getFile(
                s"${x.fullName}_Routing_importance_factor_itf_$size.txt"
              )
              val writer = new FileWriter(file)
              writer.write({
                val source = Source.fromFile(file)
                val itf = parseScenarioFile(source)
                source.close()
                multiPathsTransactions
                  .groupMapReduce(k => k)(k =>
                    itf.count(sc => sc.exists(s => k.contains(s)))
                  )(_ + _)
                  .toSeq
                  .sortBy(-_._2)
                  .map(p => s"${p._1.mkString("{", ",", "}")},${p._2}")
                  .mkString("\n")
              })
              writer.close()
              file
            })
        }
      }

    @deprecated("this indicator should not be used for now, not useful info")
    def sortSWByITFImpact(
        x: ConfiguredPlatform,
        max: Option[Int]
    ): Future[File] = {
      x.computeKInterference(
        max.getOrElse(x.initiators.size),
        ignoreExistingAnalysisFiles = false,
        computeSemantics = false,
        verboseResultFile = false,
        onlySummary = false
      ) map { resultFiles =>
        {
          val file = FileManager.analysisDirectory.getFile(
            s"${x.fullName}_SW_importance_factor.txt"
          )
          val writer = new FileWriter(file)
          writer.write(
            resultFiles
              .filter(_.getName.contains("itf"))
              .foldLeft(Map.empty[Set[Application], Int])((acc, file) => {
                val involved = SWInvolvedInITF(x, file)
                (acc.toSeq ++ involved.toSeq).groupMapReduce(_._1)(_._2)(_ + _)
              })
              .toSeq
              .sortBy(-_._2)
              .map(p => s"${p._1.mkString("{", ",", "}")},${p._2}")
              .mkString("\n")
          )
          writer.close()
          file
        }
      }
    }

    // FIXME If using a transaction library, the ids are UserScenarios and not PhysicalScenarios, need to know the difference
    // to retrieve the scenarios used by a sw
    private def SWInvolvedInITF(
        x: ConfiguredPlatform,
        file: File
    ): Map[Set[Application], Int] = {
      val results = parseScenarioFile(Source.fromFile(file))
      val swByScenario = results
        .map(
          _.flatMap(s =>
            x.applications.filter(sw =>
              x.transactionsBySW(sw).exists(s2 => s2.toString == s)
            )
          ).toSet
        )
      swByScenario.groupMapReduce(s => s)(_ => 1)(_ + _)
    }
  }

  private def extractSize(in: Iterable[String]): Map[Int, Int] =
    in.filter(_.startsWith("[INFO] size "))
      .map(_.split("over").head)
      .map(s => {
        val data = s.split(':')
        data.head.filter(_.isDigit).toInt -> data.last.filter(_.isDigit).toInt
      })
      .toMap

  def parseSummaryFile(
      platform: Platform
  ): Option[(Map[Int, Int], Map[Int, Int], Double)] =
    for {
      file <- FileManager.analysisDirectory.locate(
        FileManager.getInterferenceAnalysisSummaryFileName(platform)
      )
    } yield {
      val source = Source.fromFile(file)
      val lines = source
        .getLines()
        .toSeq

      val indexBeginItf =
        lines.indexWhere(_.contains("Computed ITF"))
      val indexBeginFree =
        lines.indexWhere(_.contains("Computed ITF-free"))
      val analysisTime: Double =
        (for {
          s <- lines.find(_.startsWith("Computation time"))
        } yield {
          s.replaceAll("[^\\d.]", "").toDouble
        }).getOrElse(-1.0)
      val itfSizes = extractSize(lines.slice(indexBeginItf, indexBeginFree))
      val freeSizes = extractSize(lines.slice(indexBeginFree, lines.length))
      source.close()
      (itfSizes, freeSizes, analysisTime)
    }

  def parseSemanticsSizeFile(platform: Platform): Option[Map[Int, BigInt]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getSemanticSizeFileName(platform)
      )
    } yield {
      val source = Source.fromFile(file)
      val res = source
        .getLines()
        .toSeq
        .drop(1)
        .map(_.split(","))
        .map(s =>
          s.head.filter(_.isDigit).toInt -> BigInt(
            s.last.filter(_.isDigit)
          )
        )
        .toMap
      source.close()
      res
    }
  }

  def parseScenarioFile(source: BufferedSource): Array[Seq[String]] = {
    val res = source
      .getLines()
      .filter(_.head == '<')
      .map(_.replaceAll("[<> ]*", ""))
      .map(_.split("\\|\\|").toSeq.sorted)
      .toArray
      .sortBy(_.mkString("||"))
    source.close()
    res
  }
}
