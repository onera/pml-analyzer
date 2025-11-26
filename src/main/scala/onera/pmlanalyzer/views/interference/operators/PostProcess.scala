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
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.Default
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.operators.Analyse.ConfiguredPlatform
import fastparse.*
import fastparse.SingleLineWhitespace.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.service.Load
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransactionId,
  Path,
  PhysicalTransactionId
}

import java.io.{File, FileWriter}
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.{BufferedSource, Source}
import scala.math.Ordering.Implicits.*

/** Base trait providing proof that an element is post processable
  * @tparam T
  *   the type of the component (contravariant)
  */
private[operators] trait PostProcess[-T] {

  def interferenceDiff(
      x: T,
      that: T,
      method: Method,
      implm: SolverImplm
  ): Seq[File]

  def parseITFMultiTransactionFile(
      x: T,
      method: Option[Method],
      implm: Option[SolverImplm]
  ): Array[Seq[String]]

  def parseITFMultiTransactionFile(
      x: T,
      n: Int,
      method: Option[Method],
      implm: Option[SolverImplm]
  ): Array[Seq[String]]

  def parseFreeMultiTransactionFile(
      x: T,
      n: Int,
      method: Option[Method],
      implm: Option[SolverImplm]
  ): Array[Seq[String]]

  def parseFreeMultiTransactionFile(
      x: T,
      method: Option[Method],
      implm: Option[SolverImplm]
  ): Array[Seq[String]]
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
    * {{{x.parseITFMultiTransactionFile()}}} Try to find and parse the n-itf results for
    * the considered element {{{x.parseITFMultiTransactionFile(n)}}} Try to find and
    * parse the n-itf-free results for the considered element
    * {{{x.parseFreeMultiTransactionFile(n)}}} Compute for each hardware component the
    * number of up to n-itf where the component is involved in the
    * interference channel. The result is provided in a file of the analysis
    * directory {{{x.sortPLByITFImpact(n)}}} Compute for each multi path
    * transaction the number of up to itf involving at least one of its
    * branches The result is provided in a file of the analysis directory
    * {{{x.sortMultiPathByITFImpact(n)}}}
    */
  trait Ops {

    /** Extension method class
      */
    extension [T](self: T) {

      /** Compare the interference results with another element and store the
       * result in a dedicated file in the analysis folder
       *
       * @param that
       * the other element
       * @param ev
       * the proof that any element of T is analysable
       * @return
       * the location of the result files
       */
      def interferenceDiff(that: T, method: Method, implm: SolverImplm)(using
          ev: PostProcess[T]
      ): Seq[File] = ev.interferenceDiff(self, that, method, implm)

      /** Try to find and parse itf results for the considered element
       *
       * @param ev
       * the proof that any element of T is analysable
       * @return
       * the set of multi-transaction identifiers that are n-itf
       */
      def parseITFMultiTransactionFile(
          method: Option[Method],
          implm: Option[SolverImplm]
      )(using
          ev: PostProcess[T]
      ): Array[Seq[String]] =
        ev.parseITFMultiTransactionFile(self, method, implm)

      /** Try to find and parse the n-itf results for the considered element
       *
       * @param n
       * the maximal size of transaction per itf
       * @param ev
       * the proof that any element of T is analysable
       * @return
       * the set of multi-transaction identifiers that are n-itf
       */
      def parseITFMultiTransactionFile(
          n: Int,
          method: Option[Method],
          implm: Option[SolverImplm]
      )(using
          ev: PostProcess[T]
      ): Array[Seq[String]] =
        ev.parseITFMultiTransactionFile(self, n, method, implm)

      /** Try to find and parse the n-itf-free results for the considered
       * element
       *
       * @param n
       * the maximal size of transaction per itf-free
       * @param ev
       * the proof that any element of T is analysable
       * @return
       * the set of multi-transaction identifiers that are interference free
       */
      def parseFreeMultiTransactionFile(
          n: Int,
          method: Option[Method],
          implm: Option[SolverImplm]
      )(using
          ev: PostProcess[T]
      ): Array[Seq[String]] =
        ev.parseFreeMultiTransactionFile(self, n, method, implm)
    }

    extension [T <: ConfiguredPlatform](self: T) {

      /** Compute for each hardware component the number of itf where
         * the component is involved in the interference channel The result is
         * provided in a file of the analysis directory
         *
         * @param max
         * the optional maximal size of the considered itf
         * @param ev
         * the proof that any element of T is analysable
         * @return
         * the location of the result files
         */
      def sortPLByITFImpact(
          max: Option[Int],
          implm: SolverImplm,
          method: Method
      )(using
          ev: PostProcess[T]
      ): Set[File] =
        Await.result(
          PostProcess.sortPLByITFImpact(self, max, implm, method),
          Duration.Inf
        )

      /** Compute for each multi path transaction the number of itf
         * involving at least one of its branches The result is provided in a
         * file of the analysis directory
         *
         * @param max
         * the optional maximal size of the considered itf
         * @param ev
         * the proof that any element of T is analysable
         * @return
         * the location of the result files
         */
      def sortMultiPathByITFImpact(
          max: Option[Int],
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using
          ev: PostProcess[T]
      ): Set[File] =
        Await.result(
          PostProcess.sortMultiPathByITFImpact(self, max, implm, method),
          Duration.Inf
        )
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** A platform is post processable
    */
  given [T](using ev: Analyse[T]): PostProcess[T] with {

    def interferenceDiff(
        x: T,
        that: T,
        method: Method,
        implm: SolverImplm
    ): Seq[File] = {
      for {
        size <- 2 to Math.min(ev.getMaxSize(x), ev.getMaxSize(that))
        thisITFFile <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(
            ev.getName(x),
            size,
            Some(method),
            Some(implm)
          )
        )
        thatITFFile <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(
            ev.getName(that),
            size,
            Some(method),
            Some(implm)
          )
        )
      } yield {
        val file = FileManager.analysisDirectory.getFile(
          s"${ev.getName(x)}_diff_${ev.getName(that)}_itf_$size.txt"
        )
        val sThisITF = Source.fromFile(thisITFFile)
        val sThatITF = Source.fromFile(thatITFFile)
        val thisITF = parseMultiTransactionFile(sThisITF)
        val thatITF = parseMultiTransactionFile(sThatITF)
        sThatITF.close()
        sThisITF.close()
        val thisDiffThat = thisITF.diff(thatITF).sorted
        val thatDiffThis = thatITF.diff(thisITF).sorted
        val writer = new FileWriter(file)
        if (thisDiffThat.isEmpty)
          writer.write(
            s"All itf of ${ev.getName(x)} are in ${ev.getName(that)}\n"
          )
        else {
          writer.write(
            s"The following ${thisDiffThat.length} itf of ${ev.getName(x)} are not in ${ev.getName(that)}\n"
          )
          thisDiffThat.foreach(d => writer.write(s"$d\n"))
        }
        if (thatDiffThis.isEmpty)
          writer.write(
            s"All itf of ${ev.getName(that)} are in ${ev.getName(x)}\n"
          )
        else {
          writer.write(
            s"The following ${thatDiffThis.length} itf in ${ev.getName(that)} are not in ${ev.getName(x)}\n"
          )
          thatDiffThis.foreach(d => writer.write(s"$d\n"))
        }
        writer.flush()
        writer.close()
        println(
          Message.successfulITFDifferenceExportInfo(
            size,
            ev.getName(x),
            ev.getName(that),
            file.getAbsolutePath
          )
        )
        file
      }
    }

    def parseITFMultiTransactionFile(
        x: T,
        method: Option[Method],
        implm: Option[SolverImplm]
    ): Array[Seq[String]] =
      for {
        k <- (2 to ev.getMaxSize(x)).toArray
        sc <- parseITFMultiTransactionFile(x, k, method, implm)
      } yield sc

    def parseITFMultiTransactionFile(
        x: T,
        n: Int,
        method: Option[Method],
        implm: Option[SolverImplm]
    ): Array[Seq[String]] = {
      for {
        file <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisITFFileName(
            ev.getName(x),
            n,
            method,
            implm
          )
        )
      } yield {
        val s = Source.fromFile(file)
        val result = parseMultiTransactionFile(s)
        s.close()
        result
      }
    } getOrElse Array.empty

    def parseFreeMultiTransactionFile(
        x: T,
        method: Option[Method],
        implm: Option[SolverImplm]
    ): Array[Seq[String]] =
      for {
        k <- (2 to ev.getMaxSize(x)).toArray
        sc <- parseFreeMultiTransactionFile(x, k, method, implm)
      } yield sc

    def parseFreeMultiTransactionFile(
        x: T,
        n: Int,
        method: Option[Method],
        implm: Option[SolverImplm]
    ): Array[Seq[String]] = {
      for {
        file <- FileManager.analysisDirectory.locate(
          FileManager.getInterferenceAnalysisFreeFileName(
            ev.getName(x),
            n,
            method,
            implm
          )
        )
      } yield {
        val s = Source.fromFile(file)
        val result = parseMultiTransactionFile(s)
        s.close()
        result
      }
    } getOrElse Array.empty
  }

  def sortPLByITFImpact(
      x: ConfiguredPlatform,
      max: Option[Int],
      implm: SolverImplm = Monosat,
      method: Method = Default
  ): Future[Set[File]] = {
    x.computeKInterference(
      max.getOrElse(x.initiators.size),
      ignoreExistingAnalysisFiles = false,
      computeSemantics = false,
      verboseResultFile = false,
      onlySummary = false,
      implm,
      method
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
    val plByMultiTransaction = results
      .map(p =>
        p._1
          .flatMap(s =>
            x.directHardware.filter(_.services.exists(s2 => s2.toString == s))
          )
          .toSet -> p._2
      )
    x.directHardware.foldLeft(Map.empty[Hardware, Int])((localMap, pl) => {
      val impacted =
        plByMultiTransaction.filter(_._1.contains(pl)).map(_._2).sum
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
      max: Option[Int],
      implm: SolverImplm = Monosat,
      method: Method = Default
  ): Future[Set[File]] =
    x.computeKInterference(
      max.getOrElse(x.initiators.size),
      ignoreExistingAnalysisFiles = false,
      computeSemantics = false,
      verboseResultFile = false,
      onlySummary = false,
      implm,
      method
    ) map { resultFiles =>
      {
        val multiPathsTransactions = x match {
          case c: TransactionLibrary =>
            x.multiPathsTransactions.flatMap(s => {
              val transactionNames = s.map(x.atomicTransactionsName)
              (for {
                userTransactionIds <- c.transactionUserName
                  .get(transactionNames)
                if userTransactionIds.nonEmpty
              } yield {
                userTransactionIds.map(x => Set(x.toString))
              }) getOrElse Set(transactionNames.map(_.toString))
            })
          case _ =>
            x.multiPathsTransactions.map(
              _.map(x.atomicTransactionsName).map(_.toString)
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
              val itf = parseMultiTransactionFile(source)
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
      max: Option[Int],
      implm: SolverImplm = Monosat,
      method: Method = Default
  ): Future[File] = {
    x.computeKInterference(
      max.getOrElse(x.initiators.size),
      ignoreExistingAnalysisFiles = false,
      computeSemantics = false,
      verboseResultFile = false,
      onlySummary = false,
      implm,
      method
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

  // FIXME If using a transaction library, the ids are UserTransaction and not PhysicalTransaction, need to know the difference
  // to retrieve the transactions used by a sw
  private def SWInvolvedInITF(
      x: ConfiguredPlatform,
      file: File
  ): Map[Set[Application], Int] = {
    val results = parseMultiTransactionFile(Source.fromFile(file))
    val swByMultiTransaction = results
      .map(
        _.flatMap(s =>
          x.applications.filter(sw =>
            x.atomicTransactionsBySW(sw).exists(s2 => s2.toString == s)
          )
        ).toSet
      )
    swByMultiTransaction.groupMapReduce(s => s)(_ => 1)(_ + _)
  }

  private def parseWord[$: P] =
    CharsWhile(c => !Set(' ', '\n', ',').contains(c))

  private def parsePlatformName[$: P] =
    P("Platform" ~/ "Name" ~ ":" ~ parseWord ~ "\n")

  private def parseComputationMethod[$: P] =
    P("Computation" ~/ "Method" ~ ":" ~ parseWord ~ "\n")

  private def parseSolver[$: P] =
    P("Solver" ~/ ":" ~ parseWord ~ "\n")

  private def parseFilePath[$: P] =
    P("File" ~/ ":" ~ parseWord ~ "\n")

  private def parseDate[$: P] =
    P("Date" ~/ ":" ~ parseWord ~ "\n")

  private def parseHeader[$: P] =
    P(
      parsePlatformName ~ parseComputationMethod ~ parseSolver ~ parseFilePath ~ parseDate
    )

  private def parseSectionSep[$: P] =
    P(CharsWhile(_ != '\n') ~ "\n")

  private def parseITFHeader[$: P] =
    P("Computed" ~ "ITF" ~ "\n")

  private def parseFreeHeader[$: P] =
    P("Computed" ~ "ITF-free" ~ "\n")

  private def parseSize[$: P] =
    P(
      "[INFO]" ~/ "size" ~ digit.rep(min = 1).! ~ ":" ~ digit.rep(min = 1).!
        ~ CharsWhile(c => c != ' ' && c != '\n').? ~ "\n"
    )
      .map((l, r) => l.toInt -> BigInt(r))

  private def parseTotal[$: P] =
    P("Total" ~ ":" ~ digit.rep(min = 1) ~ "\n")

  private def parseComputationTime[$: P] =
    P(
      "Computation" ~ "time" ~ ":" ~ digit
        .rep(min = 1)
        .! ~ ("." ~ digit.rep(min = 1).!).? ~ "s" ~ "\n"
    )
      .map((l, r) => s"$l${r.getOrElse("")}".toDouble)

  private def parseComputationTimeSection[$: P] =
    P(parseTotal ~ parseComputationTime)

  private def parseSizes[$: P] =
    P(parseSize.rep).map(_.toMap)

  private def parseSizeSection[$: P] =
    P(parseITFHeader ~ parseSizes ~ parseFreeHeader ~ parseSizes)

  private def parseSummaryFile[$: P] =
    P(
      Start ~ parseHeader ~ parseSectionSep ~ parseSizeSection
        ~ parseSectionSep ~ parseComputationTimeSection ~ parseSectionSep ~ End
    )

  def parseSummaryFile(
      platformName: String,
      method: Option[Method],
      implm: Option[SolverImplm]
  ): Option[(Map[Int, BigInt], Map[Int, BigInt], Double)] =
    for {
      file <- FileManager.analysisDirectory.locate(
        FileManager.getInterferenceAnalysisSummaryFileName(
          platformName,
          method,
          implm
        )
      )
    } yield {
      val source = Source.fromFile(file)
      parse(
        source.getLines().mkString("", "\n", "\n"),
        parseSummaryFile(using _)
      ) match {
        case Parsed.Success(res, _) =>
          source.close()
          res
        case f: Parsed.Failure =>
          println(f.trace().longAggregateMsg)
          source.close()
          (Map.empty, Map.empty, -1)
      }
    }

  private def digit[$: P] = CharIn("0-9")

  private def parseSemanticsSizeFileHeader[$: P] =
    P("Multi-transaction cardinal" ~ "," ~ "Number" ~ "\n")

  private def parseValues[$: P] =
    P((digit.rep(min = 1).! ~ "," ~ digit.rep(min = 1).!).rep(sep = "\n"))
      .map(_.map((l, r) => l.toInt -> BigInt(r)).toMap)

  private def parseSemanticsSizeFile[$: P] =
    P(Start ~ parseSemanticsSizeFileHeader ~ parseValues ~ End)

  def parseSemanticsSizeFile(platformName: String): Option[Map[Int, BigInt]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getSemanticSizeFileName(platformName)
      )
    } yield {
      val source = Source.fromFile(file)
      parse(
        source.getLines().mkString("\n"),
        parseSemanticsSizeFile(using _)
      ) match {
        case Parsed.Success(res, _) =>
          source.close()
          res
        case f: Parsed.Failure =>
          println(f.trace().longAggregateMsg)
          source.close()
          Map.empty
      }
    }
  }

  private def parseAtomicTransactionId[$: P] =
    P(
      CharPred(x => !Set('|', '<', '>', ' ', '\n', ',').contains(x))
        .rep(min = 1)
        .!
    )

  private def parseTransactionId[$: P] =
    P(parseAtomicTransactionId.!.rep(min = 1, sep = "|"))
      .map(_.mkString("|"))

  private def parseMultiTransaction[$: P] =
    P("<" ~ parseTransactionId.!.rep(min = 2, sep = "||") ~ ">")
      .map(_.sorted)

  private def parseMultiTransactions[$: P] =
    P(Start ~ parseMultiTransaction.rep ~ End)
      .map(_.sorted)

  def parseMultiTransactionFile(source: BufferedSource): Array[Seq[String]] = {
    parse(source.getLines(), parseMultiTransactions(using _)) match {
      case Parsed.Success(res, _) =>
        source.close()
        res.toArray
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Array.empty[Seq[String]]
    }
  }

  private def parseAtomicTransactionTableHeader[$: P] =
    P("AtomicTransactionId" ~ "," ~ "Path" ~ "\n")

  private def parseAtomicTransactionPath[$: P] =
    P(parseAtomicTransactionId ~/ "," ~ parseWord.!.rep(sep = "::") ~ "\n")
      .map((id, path) =>
        AtomicTransactionId(Symbol(id)) -> path.map(s => Symbol(s)).toList
      )

  private def parseAtomicTransactionTable[$: P] =
    P(
      Start ~ parseAtomicTransactionTableHeader ~ parseAtomicTransactionPath.rep ~ End
    )
      .map(_.toMap)

  def parseAtomicTransactionTable(
      source: BufferedSource
  ): Map[AtomicTransactionId, Path[Symbol]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseAtomicTransactionTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseAtomicTransactionTable(
      platformName: String
  ): Option[Map[AtomicTransactionId, Path[Symbol]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getAtomicTransactionTableName(platformName)
      )
    } yield {
      parseAtomicTransactionTable(Source.fromFile(file))
    }
  }

  private def parseTransactionTableHeader[$: P] =
    P("PhysicalTransactionId" ~ "," ~ "AtomicTransactionId(s)" ~ "\n")

  private def parseAtomicTransactionIds[$: P] =
    P(parseTransactionId ~ "," ~ parseAtomicTransactionId.rep(sep = ",") ~ "\n")
      .map((l, aIds) =>
        PhysicalTransactionId(Symbol(l)) -> aIds.toSet.map(id =>
          AtomicTransactionId(Symbol(id))
        )
      )

  private def parseTransactionTable[$: P] =
    P(Start ~ parseTransactionTableHeader ~ parseAtomicTransactionIds.rep ~ End)
      .map(_.toMap)

  def parseTransactionTable(
      source: BufferedSource
  ): Map[PhysicalTransactionId, Set[AtomicTransactionId]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseTransactionTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseTransactionTable(
      platformName: String
  ): Option[Map[PhysicalTransactionId, Set[AtomicTransactionId]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getPhysicalTransactionTableName(platformName)
      )
    } yield {
      parseTransactionTable(Source.fromFile(file))
    }
  }

  private def parseServiceInterfereTableHeader[$: P] =
    P("Service" ~ "," ~ "Service(s)" ~ "\n")

  private def parseServiceInterfereValue[$: P] =
    P(parseWord.! ~/ "," ~ parseWord.!.rep(sep = ",") ~ "\n")
      .map((s, ss) => Symbol(s) -> ss.map(Symbol.apply).toSet)

  private def parseServiceInterfereTable[$: P] =
    P(
      Start ~ parseServiceInterfereTableHeader ~ parseServiceInterfereValue.rep ~ End
    )
      .map(_.toMap)

  def parseServiceInterfereTable(
      source: BufferedSource
  ): Map[Symbol, Set[Symbol]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseServiceInterfereTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseServiceInterfereTable(
      platformName: String
  ): Option[Map[Symbol, Set[Symbol]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getServiceInterfereTableName(platformName)
      )
    } yield {
      parseServiceInterfereTable(Source.fromFile(file))
    }
  }

  private def parseATrInterfereTableHeader[$: P] =
    P("AtomicTransactionId" ~ "," ~ "AtomicTransactionId(s)" ~ "\n")

  private def parseATrInterfereValue[$: P] =
    P(
      parseAtomicTransactionId ~/ "," ~ parseAtomicTransactionId.!.rep(sep =
        ","
      ) ~ "\n"
    )
      .map((s, ss) =>
        AtomicTransactionId(Symbol(s)) -> ss
          .map(x => AtomicTransactionId(Symbol(x)))
          .toSet
      )

  private def parseAtomicTransactionInterfereTable[$: P] =
    P(Start ~ parseATrInterfereTableHeader ~ parseATrInterfereValue.rep ~ End)
      .map(_.toMap)

  def parseAtomicTransactionInterfereTable(
      source: BufferedSource
  ): Map[AtomicTransactionId, Set[AtomicTransactionId]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseAtomicTransactionInterfereTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseAtomicTransactionInterfereTable(
      platformName: String
  ): Option[Map[AtomicTransactionId, Set[AtomicTransactionId]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getAtomicTransactionExclusiveTableName(platformName)
      )
    } yield {
      parseAtomicTransactionInterfereTable(Source.fromFile(file))
    }
  }

  private def parseTrInterfereTableHeader[$: P] =
    P("PhysicalTransactionId" ~ "," ~ "PhysicalTransactionId(s)" ~ "\n")

  private def parseTrInterfereValue[$: P] =
    P(parseTransactionId ~/ "," ~ parseTransactionId.!.rep(sep = ",") ~ "\n")
      .map((s, ss) =>
        PhysicalTransactionId(Symbol(s)) -> ss
          .map(x => PhysicalTransactionId(Symbol(x)))
          .toSet
      )

  private def parseTransactionInterfereTable[$: P] =
    P(Start ~ parseTrInterfereTableHeader ~ parseTrInterfereValue.rep ~ End)
      .map(_.toMap)

  def parseTransactionInterfereTable(
      source: BufferedSource
  ): Map[PhysicalTransactionId, Set[PhysicalTransactionId]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseTransactionInterfereTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseTransactionInterfereTable(
      platformName: String
  ): Option[Map[PhysicalTransactionId, Set[PhysicalTransactionId]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getTransactionExclusiveTableName(platformName)
      )
    } yield {
      parseTransactionInterfereTable(Source.fromFile(file))
    }
  }

  private def parseUserTransactionTableHeader[$: P] =
    P("UserTransactionId" ~ "," ~ "AtomicTransactionId(s)" ~ "\n")

  private def parseUserTransactionValue[$: P] =
    P(
      parseTransactionId ~/ "," ~ parseAtomicTransactionId.rep(sep = ",") ~ "\n"
    )
      .map((s, ss) =>
        UserTransactionId(Symbol(s)) -> ss
          .map(x => AtomicTransactionId(Symbol(x)))
          .toSet
      )

  private def parseUserTransactionTable[$: P] =
    P(
      Start ~ parseUserTransactionTableHeader ~ parseUserTransactionValue.rep ~ End
    )
      .map(_.toMap)

  def parseUserTransactionTable(
      source: BufferedSource
  ): Map[UserTransactionId, Set[AtomicTransactionId]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseUserTransactionTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseUserTransactionTable(
      platformName: String
  ): Option[Map[UserTransactionId, Set[AtomicTransactionId]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getUserTransactionTableName(platformName)
      )
    } yield {
      parseUserTransactionTable(Source.fromFile(file))
    }
  }

  private def parseUserExclusiveTransactionTableHeader[$: P] =
    P("UserTransactionId" ~ "," ~ "UserTransactionId(s)" ~ "\n")

  private def parseUserExclusiveTransactionValue[$: P] =
    P(parseTransactionId ~/ "," ~ parseTransactionId.rep(sep = ",") ~ "\n")
      .map((s, ss) =>
        UserTransactionId(Symbol(s)) -> ss
          .map(x => UserTransactionId(Symbol(x)))
          .toSet
      )

  private def parseUserExclusiveTransactionTable[$: P] =
    P(
      Start ~ parseUserExclusiveTransactionTableHeader ~ parseUserExclusiveTransactionValue.rep ~ End
    )
      .map(_.toMap)

  def parseUserExclusiveTransactionTable(
      source: BufferedSource
  ): Map[UserTransactionId, Set[UserTransactionId]] = {
    parse(
      source.getLines().mkString("", "\n", "\n"),
      parseUserExclusiveTransactionTable(using _)
    ) match {
      case Parsed.Success(res, _) =>
        source.close()
        res
      case f: Parsed.Failure =>
        println(f.trace().longAggregateMsg)
        source.close()
        Map.empty
    }
  }

  def parseUserExclusiveTransactionTable(
      platformName: String
  ): Option[Map[UserTransactionId, Set[UserTransactionId]]] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getUserTransactionExclusiveTableName(platformName)
      )
    } yield {
      parseUserExclusiveTransactionTable(Source.fromFile(file))
    }
  }

  def parseGraphReductionFile(
      platformName: String,
      method: Method,
      implm: SolverImplm
  ): Option[BigDecimal] = {
    for {
      file <- FileManager.exportDirectory.locate(
        FileManager.getGraphReductionFileName(
          platformName,
          Some(method),
          Some(implm)
        )
      )
    } yield {
      val source = Source.fromFile(file)
      val res = source
        .getLines()
        .toSeq(1)
        .toDouble
      source.close()
      BigDecimal(res)
    }
  }
}
