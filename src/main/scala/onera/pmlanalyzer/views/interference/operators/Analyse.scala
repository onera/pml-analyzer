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

import com.github.javabdd.BDD
import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.model.formalisation.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.Default
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  InterferenceSpecification
}

import java.io.{File, FileWriter}
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

/** Base trait providing proof that an element is analysable with monosat
  * @tparam T
  *   the type of the component (contravariant)
  */
private[pmlanalyzer] sealed trait Analyse[-T] {
  def computeInterference(
      x: T,
      maxSize: Int,
      ignoreExistingAnalysisFiles: Boolean,
      computeSemantics: Boolean,
      verboseResultFile: Boolean,
      onlySummary: Boolean,
      implm: SolverImplm,
      method: Method
  ): Future[Set[File]]

  def printGraph(x: T, implm: SolverImplm, method: Method): File

  def getSemanticsSize(x: T, max: Int): Map[Int, BigInt]

  def getGraphSize(
      x: T,
      implm: SolverImplm,
      method: Method
  ): (BigInt, BigInt)

  def getName(x: T): String

  def getMaxSize(x: T): Int
}

private[pmlanalyzer] object Analyse {

  type ConfiguredPlatform = Platform & InterferenceSpecification

  type ConfiguredLibraryBasedPlatform = ConfiguredPlatform &
    ApplicativeTableBasedInterferenceSpecification & TransactionLibrary

  /* ------------------------------------------------------------------------------------------------------------------
   * EXTENSION METHODS
   * --------------------------------------------------------------------------------------------------------------- */

  /** If an element x of type T can be analysed then
    *
    * To provide a computation future (computation is not executed yet) of all
    * interference up to maxSize {{{x.computeKInterference(maxSize)}}} To
    * actually perform the interference calculus up to maxSize
    * {{{x.computeKInterference(maxSize, duration)}}}
    */
  trait Ops {

    /** Extension method
      */
    extension [T](self: T) {

      /** Provide a computation future (computation is not executed yet) of all
        * interference up to maxsize
        * @param maxSize
        *   the maximal number of initiator that can execute a transaction, it
        *   should be less or equal to the total number of initiators in the
        *   platform.
        * @param ignoreExistingAnalysisFiles
        *   do the analysis only even if result files for the considered
        *   platform can be found in the analysis directory
        * @param verboseResultFile
        *   add extra information on analysis files
        * @param ev
        *   the proof that the component is analysable
        * @return
        *   the computation future
        */
      def computeKInterference(
          maxSize: Int,
          ignoreExistingAnalysisFiles: Boolean,
          computeSemantics: Boolean,
          verboseResultFile: Boolean,
          onlySummary: Boolean,
          implm: SolverImplm,
          method: Method
      )(using ev: Analyse[T]): Future[Set[File]] =
        ev.computeInterference(
          self,
          maxSize,
          ignoreExistingAnalysisFiles,
          computeSemantics,
          verboseResultFile,
          onlySummary,
          implm,
          method
        )

      /** Perform the interference analysis
        * @param maxSize
        *   the maximal number of initiator that can execute a transaction, it
        *   should be less or equal to the total number of initiators in the
        *   platform.
        * @param timeout
        *   the maximal duration that is allowed to perform the interference
        *   computation.
        * @param ignoreExistingAnalysisFiles
        *   do the analysis only even if result files for the considered
        *   platform can be found in the analysis directory (false by default)
        * @param verboseResultFile
        *   add extra information on analysis files (false by default)
        * @param ev
        *   the proof that the component is analysable
        * @return
        *   the computation future
        */
      def computeKInterference(
          maxSize: Int,
          timeout: Duration,
          ignoreExistingAnalysisFiles: Boolean = false,
          computeSemantics: Boolean = true,
          verboseResultFile: Boolean = false,
          onlySummary: Boolean = false,
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using ev: Analyse[T]): Set[File] =
        Await.result(
          ev.computeInterference(
            self,
            maxSize,
            ignoreExistingAnalysisFiles,
            computeSemantics,
            verboseResultFile,
            onlySummary,
            implm,
            method
          ),
          timeout
        )

      def getAnalysisGraphSize(implm: SolverImplm, method: Method)(using
          ev: Analyse[T]
      ): (BigInt, BigInt) =
        ev.getGraphSize(self, implm, method)

      /** Perform the interference analysis considering that all the initiators
       * can execute a transaction
       *
       * @param timeout
       * the maximal duration that is allowed to perform the interference
       * computation.
       * @param ignoreExistingAnalysisFiles
       * do the analysis only even if result files for the considered
       * platform can be found in the analysis directory (false by default)
       * @param verboseResultFile
       * add extra information on analysis files (false by default)
       * @param ev
       * the proof that the component is analysable
       * @return
       * the computation future
       */
      def computeAllInterference(
          timeout: Duration,
          ignoreExistingAnalysisFiles: Boolean = false,
          computeSemantics: Boolean = true,
          verboseResultFile: Boolean = false,
          onlySummary: Boolean = false,
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using ev: Analyse[T]): Set[File] =
        Await.result(
          ev.computeInterference(
            self,
            ev.getMaxSize(self),
            ignoreExistingAnalysisFiles,
            computeSemantics,
            verboseResultFile,
            onlySummary,
            implm,
            method
          ),
          timeout
        )

      def getSemanticsSize(ignoreExistingFile: Boolean = false)(using
          ev: Analyse[T]
      ): Map[Int, BigInt] =
        if (!ignoreExistingFile)
          PostProcess
            .parseSemanticsSizeFile(ev.getName(self))
            .getOrElse(ev.getSemanticsSize(self, ev.getMaxSize(self)))
        else
          ev.getSemanticsSize(self, ev.getMaxSize(self))

      def computeSemanticReduction(
          implm: SolverImplm,
          method: Method,
          ignoreExistingFiles: Boolean = false
      )(using
          ev: Analyse[T]
      ): BigDecimal = {
        Await.result(
          ev.computeInterference(
            self,
            ev.getMaxSize(self),
            ignoreExistingAnalysisFiles = ignoreExistingFiles,
            computeSemantics = true,
            verboseResultFile = false,
            onlySummary = true,
            implm = implm,
            method
          ),
          1 minute
        )
        (for {
          (itfResult, freeResult, _) <- PostProcess.parseSummaryFile(
            ev.getName(self),
            Some(method),
            Some(implm)
          )
        } yield {
          val nonRed =
            itfResult.filter(_._1 >= 3).values.sum
              + freeResult.filter(_._1 >= 3).values.sum
          val semantics = self
            .getSemanticsSize(ignoreExistingFiles)
            .filter(_._1 >= 3)
            .values
            .sum
          if (nonRed != 0) {
            BigDecimal(semantics) / BigDecimal(nonRed)
          } else if (semantics != 0)
            BigDecimal(-1)
          else
            BigDecimal(1)
        }) getOrElse BigDecimal(-1)
      }

      def getName(using ev: Analyse[T]): String =
        ev.getName(self)

      def getMaxSize(using ev: Analyse[T]): Int =
        ev.getMaxSize(self)
    }

    extension [T <: ConfiguredPlatform](self: T) {
      private def computeGraphReduction(
          implm: SolverImplm,
          method: Method
      )(using ev: Analyse[T]): BigDecimal = {
        val graph = self.fullServiceGraphWithInterfere()
        val systemGraphSize =
          (graph.keySet ++ graph.values.flatten).size + graph
            .flatMap(p => p._2 map { x => Set(p._1, x) })
            .toSet
            .size
        val (nodeSize, edgeSize) = self.getAnalysisGraphSize(implm, method)
        val graphSize = BigDecimal(nodeSize + edgeSize)
        if (graphSize != 0) {
          BigDecimal(systemGraphSize) / graphSize
        } else if (BigDecimal(systemGraphSize) != 0)
          BigDecimal(-1)
        else
          BigDecimal(1)

      }

      def computeGraphReduction(
          implm: SolverImplm,
          method: Method,
          ignoreExistingFile: Boolean = false
      )(using ev: Analyse[T]): BigDecimal =
        if (ignoreExistingFile)
          computeGraphReduction(implm, method)
        else {
          PostProcess
            .parseGraphReductionFile(ev.getName(self), method, implm)
            .getOrElse(computeGraphReduction(implm, method))
        }

      def computeTopologicalInterferenceSystem(
          maxSize: Int
      )(using ev: Analyse[T]): TopologicalInterferenceSystem = {
        Analyse.computeSystem(self, maxSize)
      }
    }
  }

  /* ------------------------------------------------------------------------------------------------------------------
   * INFERENCE RULES
   * --------------------------------------------------------------------------------------------------------------- */

  given (using ev: Analyse[TopologicalInterferenceSystem]): Analyse[
    ConfiguredPlatform
  ] with {

    def getName(x: ConfiguredPlatform): String = x.fullName

    def getMaxSize(x: ConfiguredPlatform): Int = x.initiators.size

    def computeInterference(
        x: ConfiguredPlatform,
        maxSize: Int,
        ignoreExistingAnalysisFiles: Boolean,
        computeSemantics: Boolean,
        verboseResultFile: Boolean,
        onlySummary: Boolean,
        implm: SolverImplm,
        method: Method
    ): Future[Set[File]] = ev.computeInterference(
      computeSystem(x, maxSize),
      maxSize,
      ignoreExistingAnalysisFiles,
      computeSemantics,
      verboseResultFile,
      onlySummary,
      implm,
      method
    )

    def printGraph(
        x: ConfiguredPlatform,
        implm: SolverImplm,
        method: Method
    ): File = ev.printGraph(
      computeSystem(x, x.initiators.size),
      implm,
      method
    )

    def getSemanticsSize(
        x: ConfiguredPlatform,
        max: Int
    ): Map[Int, BigInt] = ev.getSemanticsSize(
      computeSystem(x, max),
      max
    )

    def getGraphSize(
        x: ConfiguredPlatform,
        implm: SolverImplm,
        method: Method
    ): (BigInt, BigInt) = ev.getGraphSize(
      computeSystem(x, x.initiators.size),
      implm,
      method
    )
  }

  /** A platform is analysable
    */
  given Analyse[TopologicalInterferenceSystem] with {

    def getName(x: TopologicalInterferenceSystem): String =
      x.name

    def getMaxSize(x: TopologicalInterferenceSystem): Int =
      x.maxSize

    def getGraphSize(
        x: TopologicalInterferenceSystem,
        implm: SolverImplm,
        method: Method
    ): (BigInt, BigInt) = {
      val problem =
        computeProblem(x, method)
      val graph = problem.graph
      val result = (BigInt(graph.nodes.size), BigInt(graph.edges.size))
      result
    }

    def printGraph(
        x: TopologicalInterferenceSystem,
        implm: SolverImplm,
        method: Method
    ): File = {
      val problem =
        computeProblem(x, method)
      val result =
        FileManager.exportDirectory.getFile(
          s"${x.name}_${method}_method_${implm}_solver_graph.dot"
        )
      val emptySolver = Solver(implm)
      problem.graph.exportGraph(emptySolver, result)
      emptySolver.close()
      result
    }

    private def solve(
        calculusProblem: InterferenceCalculusProblem with Decoder,
        size: Int,
        isFree: Boolean,
        implm: SolverImplm,
        update: (
            Boolean,
            Set[Set[PhysicalTransactionId]],
            Map[Set[PhysicalTransactionId], Set[Set[UserTransactionId]]]
        ) => Unit
    ): Unit = {
      val instantiatedProblem = calculusProblem.instantiate(size, isFree, implm)
      val results =
        instantiatedProblem.enumerateSolution(calculusProblem.variables)
      for { r <- results } {
        val physical = calculusProblem.decodeModel(
          r.collect({ case l: MLit => l }),
          isFree,
          implm
        )
        val userDefined = physical
          .groupMapReduce(p => p)(calculusProblem.decodeUserModel)(_ ++ _)
        update(isFree, physical, userDefined)
      }
    }

    /** Sequential version of MONOSAT-based interference computation The
      * parallelization is impossible, MONOSAT is tied to a native library so
      * cannot ensure that parallel calls are safe (TOO BAD)
      *
      * @param platform
      *   the platform for which the interference are computed, must contained
      *   the extra information for the interference calculus
      * @return
      *   the files containing the results of the interference calculus
      */
    def computeInterference(
        platform: TopologicalInterferenceSystem,
        maxSize: Int,
        ignoreExistingAnalysisFiles: Boolean,
        computeSemantics: Boolean,
        verboseResultFile: Boolean,
        onlySummary: Boolean,
        implm: SolverImplm,
        method: Method
    ): Future[Set[File]] = Future {
      val sizes = 2 to maxSize
      val interferenceFiles =
        if (onlySummary)
          None
        else
          Some(
            sizes
              .map(size =>
                size -> FileManager.analysisDirectory
                  .getFile(
                    FileManager
                      .getInterferenceAnalysisITFFileName(
                        platform.name,
                        size,
                        Some(method),
                        Some(implm)
                      )
                  )
              )
              .toMap
          )

      val freeFiles =
        if (onlySummary)
          None
        else
          Some(
            sizes
              .map(size =>
                size -> FileManager.analysisDirectory
                  .getFile(
                    FileManager
                      .getInterferenceAnalysisFreeFileName(
                        platform.name,
                        size,
                        Some(method),
                        Some(implm)
                      )
                  )
              )
              .toMap
          )
      val channelFiles =
        if (onlySummary)
          None
        else
          Some(
            sizes
              .map(size =>
                size -> FileManager.analysisDirectory
                  .getFile(
                    FileManager
                      .getInterferenceAnalysisChannelFileName(
                        platform.name,
                        size,
                        Some(method),
                        Some(implm)
                      )
                  )
              )
              .toMap
          )
      val files =
        for {
          iF <- interferenceFiles
          fF <- freeFiles
          cF <- channelFiles
        } yield (iF.values ++ fF.values ++ cF.values).toSet

      val summaryFile = FileManager.analysisDirectory.getFile(
        FileManager.getInterferenceAnalysisSummaryFileName(
          platform.name,
          Some(method),
          Some(implm)
        )
      )

      files match
        case Some(vF)
            if !ignoreExistingAnalysisFiles
              && vF.forall(f =>
                FileManager.analysisDirectory.locate(f.getName).isDefined
              ) =>
          println(
            Message.analysisResultFoundInfo(
              FileManager.analysisDirectory.name,
              platform.name,
              "interference analysis"
            )
          )
          vF
        case None
            if !ignoreExistingAnalysisFiles
              && FileManager.analysisDirectory
                .locate(summaryFile.getName)
                .isDefined =>
          println(
            Message.analysisResultFoundInfo(
              FileManager.analysisDirectory.name,
              platform.name,
              "interference analysis"
            )
          )
          Set.empty
        case _ => {
          val generateModelStart = System.currentTimeMillis() millis
          val calculusProblem = computeProblem(platform, method)
          val summaryWriter = new FileWriter(summaryFile)
          val interferenceWriters =
            for { iF <- interferenceFiles } yield iF.transform((_, v) =>
              new FileWriter(v)
            )
          val freeWriters =
            for { fF <- freeFiles } yield fF.transform((_, v) =>
              new FileWriter(v)
            )
          val channelWriters =
            for { cF <- channelFiles } yield cF.transform((_, v) =>
              new FileWriter(v)
            )
          val allWriters =
            for {
              iW <- interferenceWriters
              fW <- freeWriters
              cW <- channelWriters
            } yield iW.values ++ fW.values ++ cW.values

          for {
            iW <- interferenceWriters
            fW <- freeWriters
            cW <- channelWriters
            aW <- allWriters
            if verboseResultFile
          } {
            cW.foreach(kv => writeChannelInfo(kv._2, kv._1))
            fW.foreach(kv => writeFreeInfo(kv._2, kv._1))
            iW.foreach(kv => writeITFInfo(kv._2, kv._1))
            aW.foreach(w =>
              writeFileInfo(
                w,
                platform.name,
                platform.sourceFile,
                implm,
                method
              )
            )
          }

          val nbFree = mutable.Map.empty[Int, BigInt].withDefaultValue(0)
          val nbITF = mutable.Map.empty[Int, BigInt].withDefaultValue(0)
          val channels = mutable.Map.empty[Int, Map[Set[Symbol], Int]]

          val update = (
              isFree: Boolean,
              physical: Set[Set[PhysicalTransactionId]],
              user: Map[Set[PhysicalTransactionId], Set[Set[UserTransactionId]]]
          ) => {
            val userBySize =
              user.values.flatten.groupBy(_.size).transform((_, v) => v.toSet)
            if (isFree) {
              updateNumber(nbFree, userBySize)
              for { fW <- freeWriters }
                updateResultFile(fW, userBySize)
            } else {
              updateNumber(nbITF, userBySize)
              for { iW <- interferenceWriters }
                updateResultFile(iW, userBySize)
              updateChannelNumber(calculusProblem, channels, physical, user)
            }
          }

          println(
            Message.successfulModelBuildInfo(
              platform.name,
              ((System
                .currentTimeMillis() millis) - generateModelStart).toSeconds
            )
          )

          println(
            Message.startingNonExclusiveTransactionEstimationInfo(
              platform.name
            )
          )
          val estimateNonExclusiveMultiTransactionsStart =
            System.currentTimeMillis() millis
          val nonExclusiveMultiTransactions =
            if (computeSemantics)
              Some(
                platform.getSemanticsSize(ignoreExistingFile =
                  ignoreExistingAnalysisFiles
                )
              )
            else None
          println(
            Message.successfulNonExclusiveMultiTransactionEstimationInfo(
              platform.name,
              ((System
                .currentTimeMillis() millis) - estimateNonExclusiveMultiTransactionsStart).toSeconds
            )
          )
          for {
            (isFree, physical, userDefined) <- calculusProblem
              .decodeTrivialSolutions(implm)
          } {
            update(isFree, physical, userDefined)
          }

          val assessmentStartDate = System.currentTimeMillis() millis

          println(
            Message.iterationCompletedInfo(
              1,
              sizes.max,
              ((System
                .currentTimeMillis() millis) - assessmentStartDate).toSeconds
            )
          )
          for {
            size <- sizes
            map <- nonExclusiveMultiTransactions
          } yield {
            assert(
              nbITF(size) <= map(size),
              s"[ERROR] Interference analysis is unsound, the number of $size-itf is greater thant $size-multi-transactions"
            )
            assert(
              nbFree(size) <= map(size),
              s"[ERROR] Interference analysis is unsound, the number of $size-free is greater thant $size-multi-transactions"
            )
          }
          println(
            Message.iterationResultsInfo(
              isFree = false,
              nbITF,
              nonExclusiveMultiTransactions
            )
          )
          println(
            Message.iterationResultsInfo(
              isFree = true,
              nbFree,
              nonExclusiveMultiTransactions
            )
          )

          for (size <- sizes) {
            val iterationStartDate = (System.currentTimeMillis() millis)
            solve(calculusProblem, size, isFree = false, implm, update)
            solve(calculusProblem, size, isFree = true, implm, update)
            println(
              Message.iterationCompletedInfo(
                size,
                sizes.max,
                ((System
                  .currentTimeMillis() millis) - iterationStartDate).toSeconds
              )
            )
            for {
              map <- nonExclusiveMultiTransactions
            } yield {
              if (size == 2)
                assert(
                  nbITF(2) + nbFree(2) == map(2),
                  "[ERROR] Interference analysis is unsound, the sum of 2-itf and 2-free is not equal to 2-multi-transactions"
                )
              assert(
                nbITF(size) <= map(size),
                s"[ERROR] Interference analysis is unsound, the number of $size-itf is greater thant $size-multi-transactions"
              )
              assert(
                nbFree(size) <= map(size),
                s"[ERROR] Interference analysis is unsound, the number of $size-free is greater thant $size-multi-transactions"
              )
            }
            println(
              Message.iterationResultsInfo(
                isFree = false,
                nbITF,
                nonExclusiveMultiTransactions
              )
            )
            println(
              Message.iterationResultsInfo(
                isFree = true,
                nbFree,
                nonExclusiveMultiTransactions
              )
            )
          }
          val computationTime =
            ((System
              .currentTimeMillis() millis) - assessmentStartDate).toSeconds
          for { cW <- channelWriters }
            updateChannelFile(cW, channels)

          if (verboseResultFile) {
            for {
              iW <- interferenceWriters
              (i, w) <- iW
            } {
              writeFooter(w, computationTime, nbITF.getOrElse(i, 0))
            }
            for {
              fW <- freeWriters
              (i, w) <- fW
            } {
              writeFooter(w, computationTime, nbFree.getOrElse(i, 0))
            }
          }

          writeFileInfo(
            summaryWriter,
            platform.name,
            platform.sourceFile,
            implm,
            method
          )
          summaryWriter.write("Computed ITF\n")
          summaryWriter.write(
            Message.printMultiTransactionNumber(
              nbITF,
              nonExclusiveMultiTransactions
            )
          )
          summaryWriter.write("Computed ITF-free\n")
          summaryWriter.write(
            Message.printMultiTransactionNumber(
              nbFree,
              nonExclusiveMultiTransactions
            )
          )
          writeFooter(
            summaryWriter,
            computationTime,
            nbFree.values.sum + nbITF.values.sum
          )

          summaryWriter.flush()
          summaryWriter.close()

          for {
            aW <- allWriters
            w <- aW
          } {
            w.flush()
            w.close()
          }
          println(
            Message.analysisCompletedInfo(
              "Interference analysis",
              computationTime
            )
          )
          files.getOrElse(Set.empty)
        }
    }

    /** Definition of the core problem without cardinality constraints on
      * interference sets
      *
      * @param platform
      *   the platform on which the interference analysis is performed
      * @return
      *   the variables and constraints to be instantiated in a MONOSAT Solver
      */
    private def computeProblem(
        system: TopologicalInterferenceSystem,
        method: Method
    ): InterferenceCalculusProblem with Decoder = {

      method match {
        case Method.GroupedLitBased =>
          GroupedLitInterferenceCalculusProblem(system)
        case Method.Default =>
          DefaultInterferenceCalculusProblem(system)
      }
    }

    private def writeFooter(
        writer: FileWriter,
        computationTime: Long,
        size: BigInt
    ): Unit =
      writer write
        s"""------------------------------------------
           |Total: $size
           |Computation time: ${computationTime}s
           |-------------------------------------------
           |""".stripMargin

    private def writeChannelInfo(writer: FileWriter, size: Int): Unit =
      writer write
        s"""------------------------------------------
           |Interference channels for $size-ary interferences
           |""".stripMargin

    private def writeITFInfo(writer: FileWriter, size: Int): Unit =
      writer write
        s"""------------------------------------------
           |$size-ary interference transactions
           |""".stripMargin

    private def writeFreeInfo(writer: FileWriter, size: Int): Unit =
      writer write
        s"""------------------------------------------
           |$size-ary interference free transactions
           |""".stripMargin

    private def writeFileInfo(
        writer: FileWriter,
        platformName: String,
        sourceFile: String,
        implm: SolverImplm,
        method: Method
    ): Unit =
      writer write
        s"""Platform Name: $platformName
           |Computation Method: $method
           |Solver: $implm
           |File:  $sourceFile
           |Date: ${java.time.LocalDateTime.now()}
           |------------------------------------------
           |""".stripMargin

    private def updateChannelFile(
        writer: Map[Int, FileWriter],
        channels: mutable.Map[Int, Map[Set[Symbol], Int]]
    ): Unit = {
      for ((k, v) <- channels)
        writer(k)
          .write(
            v.map(c => s"${channelId(c._1)}: ${c._2} interferences")
              .toList
              .sorted
              .mkString("\n")
          )
    }

    private def updateResultFile(
        writer: Map[Int, FileWriter],
        m: Map[Int, Set[Set[UserTransactionId]]]
    ): Unit = {
      for ((k, v) <- m; ss <- v)
        writer(k).write(
          s"${multiTransactionId(ss.map(s => PhysicalTransactionId(s.id)))}\n"
        )
    }

    private def updateNumber(
        nbITF: mutable.Map[Int, BigInt],
        m: Map[Int, Set[Set[UserTransactionId]]]
    ): Unit = {
      for ((k, v) <- m)
        nbITF(k) = nbITF.getOrElse(k, BigInt(0)) + v.size
    }

    private def updateChannelNumber(
        calculusProblem: InterferenceCalculusProblem with Decoder,
        channels: mutable.Map[Int, Map[Set[Symbol], Int]],
        physical: Set[Set[PhysicalTransactionId]],
        user: Map[Set[PhysicalTransactionId], Set[Set[UserTransactionId]]]
    ): Unit = {
      val channelNb = physical
        .flatMap(p => user(p).map(u => (calculusProblem.decodeChannel(p), u)))
        .groupBy(_._2.size)
        .transform((_, v) => v.groupMap(_._1)(_._2).transform((_, v) => v.size))
      for ((k, v) <- channelNb)
        channels.get(k) match {
          case Some(map) =>
            channels(k) =
              (map.toSeq ++ v.toSeq).groupMapReduce(_._1)(_._2)(_ + _)
          case None =>
            channels(k) = v
        }
    }

    /** Compute the number of possible multi-transactions for a given platform, this
      * result can be used to estimate the proportion of itf or free multi-transactions
      * over all possible sets. It can be used to check that 2-ift + 2-free =
      * 2-non-exclusive (for higher cardinalities, the estimation of k-redundant
      * is needed)
      *
      * @param x
      *   the studied platform
      * @return
      *   the number of multi-transactions per size
      */
    def getSemanticsSize(
        x: TopologicalInterferenceSystem,
        max: Int
    ): Map[Int, BigInt] = {
      val transactions = x.idToTransaction
      val exclusive = x.exclusiveWithTr
      val factory = new SymbolBDDFactory()
      val bdd =
        getNonExclusiveKBDD(transactions.keySet.toSeq, exclusive, max, factory)

      // for each cardinality, compute the number of satisfying assignments of the BDD encoding transactions sets
      // containing exactly k non-exclusive transactions
      val result = x.transactionUserNameOpt match {
        case Some(transactionUserName) =>
          val weightMap = transactions
            .transform((_, v) => transactionUserName(v))
            .map(kv => kv._1.id -> kv._2.size)
            .filter(_._2 >= 1)
          bdd.transform((_, v) => factory.getPathCount(v, weightMap))
        case None =>
          bdd.transform((_, v) => factory.getPathCount(v))
      }
      factory.dispose()
      result
    }

    private def getNonExclusiveKBDD[T](
        values: Seq[T],
        exclusive: Map[T, Set[T]],
        max: Int,
        factory: SymbolBDDFactory
    ): Map[Int, BDD] = {
      val symbols =
        values.map(x => x -> factory.getVar(Symbol(x.toString))).toMap

      // when a transaction s is selected then other transactions that are exclusive with it are not selected
      // \bigwedge_{s \in transactionVar} bdd(s) \Rightarrow not \bigvee_{s' \in exclusive(s)} bdd(s')
      val isExclusive = factory.andBDD(
        exclusive.map(p =>
          symbols(p._1).imp(factory.orBDD((p._2 - p._1).map(symbols)).not)
        )
      )

      (2 to max)
        .map(k =>
          k -> factory
            .mkExactlyK(values.map(x => Symbol(x.toString)), k)
            .and(isExclusive)
        )
        .toMap
    }

    /** Compute the number of k-redundant multi-transactions for a given platform, it
      * can be used to check that for all size, k-free + k-itf + k-redundant =
      * k-non-exclusive
      *
      * @param platform
      *   the platform to analyse
      * @param free
      *   the interference free multi-transactions
      * @param itf
      *   the interference multi-transactions
      * @return
      *   the number of k-redundant per size
      */
    @deprecated(
      "poor performance computation of k-redundant cardinal since based on a building out of free and itf" +
        "results that are classically very large"
    )
    def getRedundantCard(
        platform: ConfiguredPlatform,
        free: Set[Set[PhysicalTransactionId]],
        itf: Set[Set[PhysicalTransactionId]]
    ): Map[Int, BigInt] = {
      val idToTransaction = platform.purifiedTransactions
      val exclusive = idToTransaction.keySet.groupMapReduce(t => t)(t =>
        idToTransaction.keySet.filter(t2 => t2 != t && platform.finalExclusive(t, t2))
      )(_ ++ _)
      val allResults = free ++ itf
      val transactionToMultiTransaction = allResults
        .flatMap(ss => ss.map(s => s -> ss))
        .groupMap(_._1)(_._2)
      val transactionVar = idToTransaction.keys.toSeq
      val nonEmptyChannelResults = platform
        .channelNonEmpty(allResults)
      val factory = new SymbolBDDFactory()

      val isNonExclusive = exclusive.foldLeft(factory.one())((acc, p) =>
        acc.and(
          factory
            .getVar(p._1.id)
            .imp(
              p._2
                .foldLeft(factory.zero())((orAcc, s) =>
                  orAcc.or(factory.getVar(s.id))
                )
                .not()
            )
        )
      )
      // if s in transactionVar is selected then at least one free or itf is selected
      val sSelect: BDD = factory.andBDD(
        transactionToMultiTransaction.map(p =>
          factory
            .getVar(p._1.id)
            .imp(
              factory.orBDD(
                p._2.map(ss =>
                  factory
                    .getVar(InterferenceSpecification.multiTransactionId(ss).id)
                )
              )
            )
        )
      )
      // if a result is selected then all of its transactions are selected
      val resultSelect: BDD = factory.andBDD(
        allResults.map(ss =>
          factory
            .getVar(InterferenceSpecification.multiTransactionId(ss).id)
            .imp(factory.andBDD(ss.map(s => factory.getVar(s.id))))
        )
      )
      // if an itf is selected then all itfs owning an interference channel with the itf must be discarded
      // i.e. all the itfs sharing a common service with itf or using an exclusive service with itf
      val emptyChannel = factory.andBDD(
        nonEmptyChannelResults.map(p =>
          factory
            .getVar(InterferenceSpecification.multiTransactionId(p._1).id)
            .imp(
              factory
                .orBDD(
                  p._2.map(ss =>
                    factory.getVar(
                      InterferenceSpecification.multiTransactionId(ss).id
                    )
                  )
                )
                .not()
            )
        )
      )
      // the selection must contains at least one interference
      val atLeastOneITF: BDD = factory.orBDD(
        itf
          .map(InterferenceSpecification.multiTransactionId)
          .map(s => factory.getVar(s.id))
      )
      val staticCst = isNonExclusive
        .and(sSelect)
        .and(resultSelect)
        .and(atLeastOneITF)
        .and(emptyChannel)
      (2 to platform.initiators.size)
        .map(k =>
          k -> {
            val exactlyK = factory.mkExactlyK(transactionVar.map(_.id), k)
            // the selected itf or free must have a cardinality strictly lower than k
            val restrictedITFAndFree: BDD = factory.andBDD(
              allResults
                .filter(ss => ss.size >= k)
                .map(InterferenceSpecification.multiTransactionId)
                .map(ss => factory.getVar(ss.id).not)
            )
            factory.getPathCount(
              exactlyK
                .and(restrictedITFAndFree)
                .and(staticCst)
            )
          }
        )
        .toMap
    }
  }

  private def computeSystem(
      platform: ConfiguredPlatform,
      maxSize: Int
  ): TopologicalInterferenceSystem = {
    val exclusiveWithATr: Map[AtomicTransactionId, Set[AtomicTransactionId]] =
      platform.relationToMap(
        platform.purifiedAtomicTransactions.keySet,
        (l, r) => platform.finalExclusive(l, r)
      )
    val exclusiveWithTr
        : Map[PhysicalTransactionId, Set[PhysicalTransactionId]] =
      platform.relationToMap(
        platform.purifiedTransactions.keySet,
        (l, r) => platform.finalExclusive(l, r)
      )
    val interfereWith: Map[Service, Set[Service]] =
      platform.relationToMap(
        platform.services,
        (l, r) => l != r && platform.finalInterfereWith(l, r)
      )

    val finalUserTransactionExclusiveOpt =
      platform match {
        case appSpec: ApplicativeTableBasedInterferenceSpecification =>
          Some(appSpec.finalUserTransactionExclusive)
        case _ => None
      }

    val transactionUserNameOpt =
      platform match {
        case lib: TransactionLibrary =>
          Some(lib.transactionUserName)
        case _ => None
      }
    TopologicalInterferenceSystem(
      platform.purifiedAtomicTransactions.transform((_, v) => v.map(_.name)),
      platform.purifiedTransactions,
      exclusiveWithATr,
      exclusiveWithTr,
      interfereWith.map((k, v) => k.name -> v.map(_.name)),
      maxSize,
      finalUserTransactionExclusiveOpt,
      transactionUserNameOpt,
      platform.fullName,
      platform.sourceFile
    )
  }
}
