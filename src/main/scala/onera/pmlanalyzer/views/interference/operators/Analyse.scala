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

import monosat.*
import monosat.Logic.*
import net.sf.javabdd.BDD
import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserScenarioId
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Platform}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.views.interference.model.formalisation.*
import onera.pmlanalyzer.views.interference.model.formalisation.ProblemElement.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  InterferenceSpecification
}

import java.io.{File, FileWriter}
import scala.collection.immutable.SortedMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/** Base trait providing proof that an element is analysable with monosat
  * @tparam T
  *   the type of the component (contravariant)
  */
trait Analyse[-T] {
  def computeInterference(
      x: T,
      maxSize: Int,
      ignoreExistingAnalysisFiles: Boolean,
      computeSemantics: Boolean,
      verboseResultFile: Boolean,
      onlySummary: Boolean
  ): Future[Set[File]]

  def printGraph(platform: T): File

  def getSemanticsSize(platform: T, max: Int): Map[Int, BigInt]

  def getGraphSize(platform: T): (BigInt, BigInt)
}

object Analyse {

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
    extension [T <: Platform](self: T) {

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
          onlySummary: Boolean
      )(using ev: Analyse[T]): Future[Set[File]] =
        ev.computeInterference(
          self,
          maxSize,
          ignoreExistingAnalysisFiles,
          computeSemantics,
          verboseResultFile,
          onlySummary
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
          onlySummary: Boolean = false
      )(using ev: Analyse[T]): Set[File] =
        Await.result(
          ev.computeInterference(
            self,
            maxSize,
            ignoreExistingAnalysisFiles,
            computeSemantics,
            verboseResultFile,
            onlySummary
          ),
          timeout
        )

      /** Perform the interference analysis considering that all the initiators
        * can execute a transaction
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
      def computeAllInterference(
          timeout: Duration,
          ignoreExistingAnalysisFiles: Boolean = false,
          computeSemantics: Boolean = true,
          verboseResultFile: Boolean = false,
          onlySummary: Boolean = false
      )(using ev: Analyse[T], p: Provided[T, Hardware]): Set[File] =
        Await.result(
          ev.computeInterference(
            self,
            self.initiators.size,
            ignoreExistingAnalysisFiles,
            computeSemantics,
            verboseResultFile,
            onlySummary
          ),
          timeout
        )

      def getSemanticsSize(ignoreExistingFile: Boolean = false)(using
          ev: Analyse[T],
          p: Provided[T, Hardware]
      ): Map[Int, BigInt] =
        if (!ignoreExistingFile)
          PostProcess
            .parseSemanticsSizeFile(self)
            .getOrElse(ev.getSemanticsSize(self, self.initiators.size))
        else
          ev.getSemanticsSize(self, self.initiators.size)

      def computeSemanticReduction(ignoreExistingFiles: Boolean = false)(using
          ev: Analyse[T],
          p: Provided[T, Hardware]
      ): BigDecimal = {
        Await.result(
          ev.computeInterference(
            self,
            self.initiators.size,
            ignoreExistingAnalysisFiles = ignoreExistingFiles,
            computeSemantics = true,
            verboseResultFile = false,
            onlySummary = true
          ),
          1 minute
        )
        (for {
          (itfResult, freeResult, _) <- PostProcess.parseSummaryFile(self)
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

      private def computeGraphReduction(using ev: Analyse[T]): BigDecimal = {
        val graph = self.fullServiceGraphWithInterfere()
        val systemGraphSize =
          (graph.keySet ++ graph.values.flatten).size + graph
            .flatMap(p => p._2 map { x => Set(p._1, x) })
            .toSet
            .size
        println(s"serviceGraphSize: $systemGraphSize")
        val (nodeSize, edgeSize) = self.getAnalysisGraphSize()
        val graphSize = BigDecimal(nodeSize + edgeSize)
        println(s"interferenceChannelGraphSize: $systemGraphSize")
        if (graphSize != 0) {
          BigDecimal(systemGraphSize) / graphSize
        } else if (BigDecimal(systemGraphSize) != 0)
          BigDecimal(-1)
        else
          BigDecimal(1)

      }

      def computeGraphReduction(
          ignoreExistingFile: Boolean = false
      )(using ev: Analyse[T]): BigDecimal = 
        if (ignoreExistingFile)
          computeGraphReduction
        else {
          PostProcess.parseGraphReductionFile(self)
            .getOrElse(computeGraphReduction)
        }

      def getAnalysisGraphSize()(using ev: Analyse[T]): (BigInt, BigInt) =
        ev.getGraphSize(self)
    }
  }

  /* ------------------------------------------------------------------------------------------------------------------
   * INFERENCE RULES
   * --------------------------------------------------------------------------------------------------------------- */

  /** A platform is analysable
    */
  given Analyse[ConfiguredPlatform] with {

    def getGraphSize(platform: ConfiguredPlatform): (BigInt, BigInt) = {
      val problem =
        computeProblemConstraints(platform, platform.initiators.size)
      val dummySolver = new Solver()
      val graph = problem.serviceGraph.toGraph(dummySolver)
      val result = (BigInt(graph.nodes().size()), BigInt(graph.nEdges()))
      dummySolver.close()
      result
    }

    def printGraph(platform: ConfiguredPlatform): File = {
      val problem =
        computeProblemConstraints(platform, platform.initiators.size)
      val result =
        FileManager.exportDirectory.getFile(s"${platform.name.name}_graph.dot")
      val graphWriter = new FileWriter(result)
      val dummySolver = new Solver()
      graphWriter.write(problem.serviceGraph.toGraph(dummySolver).draw())
      dummySolver.close()
      graphWriter.close()
      result
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
        platform: ConfiguredPlatform,
        maxSize: Int,
        ignoreExistingAnalysisFiles: Boolean,
        computeSemantics: Boolean,
        verboseResultFile: Boolean,
        onlySummary: Boolean
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
                      .getInterferenceAnalysisITFFileName(platform, size)
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
                      .getInterferenceAnalysisFreeFileName(platform, size)
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
                      .getInterferenceAnalysisChannelFileName(platform, size)
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
        FileManager.getInterferenceAnalysisSummaryFileName(platform)
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
              platform.fullName,
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
              platform.fullName,
              "interference analysis"
            )
          )
          Set.empty
        case _ => {
          val generateModelStart = System.currentTimeMillis()
          val problem = computeProblemConstraints(platform, maxSize)
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
            aW.foreach(w => writeFileInfo(w, platform))
          }

          val nbFree = mutable.Map.empty[Int, Int].withDefaultValue(0)
          val nbITF = mutable.Map.empty[Int, Int].withDefaultValue(0)
          val channels = mutable.Map.empty[Int, Map[Channel, Int]]

          val update = (
              isFree: Boolean,
              physical: Set[Set[PhysicalScenarioId]],
              user: Map[Set[PhysicalScenarioId], Set[Set[UserScenarioId]]]
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
              updateChannelNumber(problem, channels, physical, user)
            }
          }

          println(
            Message.successfulModelBuildInfo(
              platform.fullName,
              (System.currentTimeMillis().toFloat - generateModelStart) * 1e-3
            )
          )

          println(
            Message.startingNonExclusiveScenarioEstimationInfo(
              platform.fullName
            )
          )
          val estimateNonExclusiveScenarioStart =
            System.currentTimeMillis().toFloat
          val nonExclusiveScenarios =
            if (computeSemantics)
              Some(
                platform.getSemanticsSize(ignoreExistingFile =
                  ignoreExistingAnalysisFiles
                )
              )
            else None
          println(
            Message.successfulNonExclusiveScenarioEstimationInfo(
              platform.fullName,
              (System
                .currentTimeMillis()
                .toFloat - estimateNonExclusiveScenarioStart) * 1e-3
            )
          )
          for {
            (k, v) <- problem.litToNodeSet
            isFree = v.forall(_.isEmpty)
            physical = problem.decodeModel(Set(k), isFree) if physical.nonEmpty
            userDefined = physical.groupMapReduce(p => p)(
              problem.decodeUserModel
            )(_ ++ _)
          }
            update(isFree, physical, userDefined)

          val assessmentStartDate = System.currentTimeMillis()

          println(
            Message.iterationCompletedInfo(
              1,
              sizes.max,
              (System.currentTimeMillis() - assessmentStartDate) * 1e-3
            )
          )
          for {
            size <- sizes
            map <- nonExclusiveScenarios
          } yield {
            assert(
              nbITF(size) <= map(size),
              s"[ERROR] Interference analysis is unsound, the number of $size-itf is greater thant $size-scenarios"
            )
            assert(
              nbFree(size) <= map(size),
              s"[ERROR] Interference analysis is unsound, the number of $size-free is greater thant $size-scenarios"
            )
          }
          println(
            Message.iterationResultsInfo(
              isFree = false,
              nbITF,
              nonExclusiveScenarios
            )
          )
          println(
            Message.iterationResultsInfo(
              isFree = true,
              nbFree,
              nonExclusiveScenarios
            )
          )

          for (size <- sizes) {
            val iterationStartDate = System.currentTimeMillis()
            val s = problem.instantiate(size)
            val variables =
              problem.groupedScenarios.transform((k, _) => k.toLit(s))
            while (s.solve()) {
              val cube = variables.filter(_._2.value())
              val physical =
                problem.decodeModel(
                  cube.keySet,
                  problem.isFree.toLit(s).value()
                )
              val userDefined = physical
                .groupMapReduce(p => p)(problem.decodeUserModel)(_ ++ _)
              update(problem.isFree.toLit(s).value(), physical, userDefined)
              s.assertTrue(not(monosat.Logic.and(cube.values.toSeq.asJava)))
            }
            s.close()
            println(
              Message.iterationCompletedInfo(
                size,
                sizes.max,
                (System.currentTimeMillis() - iterationStartDate) * 1e-3
              )
            )
            for {
              map <- nonExclusiveScenarios
            } yield {
              if (size == 2)
                assert(
                  nbITF(2) + nbFree(2) == map(2),
                  "[ERROR] Interference analysis is unsound, the sum of 2-itf and 2-free is not equal to 2-scenarios"
                )
              assert(
                nbITF(size) <= map(size),
                s"[ERROR] Interference analysis is unsound, the number of $size-itf is greater thant $size-scenarios"
              )
              assert(
                nbFree(size) <= map(size),
                s"[ERROR] Interference analysis is unsound, the number of $size-free is greater thant $size-scenarios"
              )
            }
            println(
              Message.iterationResultsInfo(
                isFree = false,
                nbITF,
                nonExclusiveScenarios
              )
            )
            println(
              Message.iterationResultsInfo(
                isFree = true,
                nbFree,
                nonExclusiveScenarios
              )
            )
          }
          val computationTime =
            (System.currentTimeMillis() - assessmentStartDate) * 1e-3
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

          writeFileInfo(summaryWriter, platform)
          summaryWriter.write("Computed ITF\n")
          summaryWriter.write(
            Message.printScenarioNumber(nbITF, nonExclusiveScenarios)
          )
          summaryWriter.write("Computed ITF-free\n")
          summaryWriter.write(
            Message.printScenarioNumber(nbFree, nonExclusiveScenarios)
          )
          writeFooter(summaryWriter, computationTime)

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

    private def undirectedEdgeId(l: MNode, r: MNode): EdgeId = Symbol(
      List(l, r).map(_.id.name).sorted.mkString("--")
    )

    private def nodeId(s: Set[Service]): NodeId = Symbol(
      s.toList.map(_.toString).sorted.mkString("<", "$", ">")
    )

    /** Definition of the core problem without cardinality constraints on
      * interference sets
      *
      * @param platform
      *   the platform on which the interference analysis is performed
      * @return
      *   the variables and constraints to be instantiated in a MONOSAT Solver
      */
    private def computeProblemConstraints(
        platform: ConfiguredPlatform,
        maxSize: Int
    ): Problem = {

      // Utilitarian functions
      val addNode: Set[Service] => MNode = immutableHashMapMemo { ss =>
        MNode(nodeId(ss))
      }

      val addLit: LitId => MLit = immutableHashMapMemo { s => MLit(s) }

      // DEFINITION OF VARIABLES

      // retrieving simple transactions to consider from the platform
      val transactions = platform.purifiedTransactions

      val idToScenario = platform.purifiedScenarios

      val scenarioToLit = idToScenario
        .transform((k, _) => MLit(Symbol(k.id.name + "_sn")))

      // association of the simple transaction path to its formatted name
      val initialPathT = idToScenario.to(SortedMap)

      // all the services used by transactions
      val initialServices = initialPathT.values
        .flatMap(s => s.map(transactions))
        .toSet
        .flatten

      // all the services exclusive to a given one
      val initialServicesInterfere = initialServices
        .map(s =>
          s -> initialServices.filter(s2 => platform.finalInterfereWith(s, s2))
        )
        .toMap

      // the actual path will be the service that can be a channel, ie
      // a service that is a service (or exclusive to a service) of a different and non exclusive transaction
      // FIXME If a scenario, the following computation consider that one of the services used by
      // several atomic transactions could be an interference channel that is obviously false
      // and complexify the graph for no reason
      val pathT: Map[PhysicalScenarioId, Set[PhysicalTransaction]] =
        initialPathT.view
          .mapValues(s =>
            s.map(t =>
              transactions(t).filter(s =>
                transactions.keySet.exists(t2 =>
                  t != t2 &&
                    !platform.finalExclusive(t, t2) &&
                    transactions(t2).exists(s2 =>
                      s2 == s || initialServicesInterfere(s2).contains(s)
                    )
                )
              )
            )
          )
          .toMap

      val services = pathT.values.toSet.flatten.flatten

      val servicesExclusive = services
        .map(s =>
          s -> services.filter(s2 => platform.finalInterfereWith(s, s2))
        )
        .toMap

      // the nodes of the service graph are the services grouped by exclusivity pairs
      val serviceToNodes = servicesExclusive.transform((k, v) =>
        if (v.isEmpty)
          Set(addNode(Set(k)))
        else
          v.map(k2 => addNode(Set(k, k2)))
      )

      val nodeToServices = serviceToNodes.keySet
        .flatMap(k => serviceToNodes(k).map(_ -> k))
        .groupMap(_._1)(_._2)

      val reducedNodePath = pathT
        .transform((_, v) => v.map(t => t.map(serviceToNodes)))

      val scenarioToGroupedLit = reducedNodePath
        .groupMap(_._2)(_._1)
        .values
        .flatMap(ss => ss.map(_ -> ss))
        .groupMapReduce(_._1)(x => addLit(groupedScenarioLitId(x._2.toSet).id))(
          (l, _) => l
        )

      val groupedLitToScenarios = scenarioToGroupedLit
        .groupMap(_._2)(_._1)
        .transform((_, v) => v.toSet)

      val groupedLitToNodeSet = scenarioToGroupedLit
        .groupMapReduce(_._2)(kv =>
          reducedNodePath(kv._1).map(_.toSet.flatten)
        )((l, _) => l)

      val addUndirectedEdgeI: ((MNode, MNode)) => MEdge = immutableHashMapMemo { x =>
        MEdge(x._1, x._2, undirectedEdgeId(x._1, x._2))
      }

      val addUndirectedEdge = (lr: Set[Service]) => {
        if(lr.size == 1) {
          for {ns <- serviceToNodes(lr.head).subsets(2).toSet}
            yield addUndirectedEdgeI((ns.head, ns.last))
        }else {
          serviceToNodes(lr.head).flatMap(n1 =>
            serviceToNodes(lr.last).map(n2 => addUndirectedEdgeI((n1, n2)))
          )
        }
      }
        

      // an edge is added to service graph iff one of transaction use it:
      // * the transaction must not be transparent
      // * the edge must not contain a service considered as non impacted
        // FIXME * an edge is added between all nodes sharing a common service
      val edgesToScenarios: Map[MEdge, Set[PhysicalScenarioId]] = pathT.keySet
        .flatMap(s =>
          val pathEdges = for {
            t <- pathT(s) if t.size > 1
            servCouple <- t.sliding(2)
            edge <- addUndirectedEdge(servCouple.toSet)
          } yield edge -> s
          val serviceEdges = {
            for { 
              t <- pathT(s)
              service <- t
              e <- addUndirectedEdge(Set(service))
            } yield e -> s
          }
          pathEdges ++ serviceEdges
        )
        .groupMap(_._1)(_._2)

      val graph = MGraph(nodeToServices.keySet, edgesToScenarios.keySet)

      // DEFINITION OF CONSTRAINTS

      // an edge is enabled iff at least one of the transactions using is selected
      val edgeCst = edgesToScenarios
        .transform((k, trs) =>
          SimpleAssert(
            Equal(
              MEdgeLit(k, graph),
              Or(trs.map(scenarioToGroupedLit).toSeq: _*)
            )
          )
        )

      // there is an interference if the remaining graph is connected,
      // here translated as at least one service used by other transactions is reachable from
      // the initiator service of another one

      val (trivialFreeScenarios, otherScenarios) =
        scenarioToGroupedLit.values.toSet
          .partition(
            groupedLitToNodeSet(_).forall(_.isEmpty)
          ) // All paths of the scenarios are only private ones

      val otherScenariosCouples = otherScenarios
        .subsets(2)
        .map(ss => {
          val (s, sp) = (ss.head, ss.last)
          // find a non private service of the left scenario
          val sHeads = groupedLitToNodeSet(s).filter(_.nonEmpty).map(_.head)
          val spHeads = groupedLitToNodeSet(sp).filter(_.nonEmpty).map(_.head)
          (s -> sHeads, sp -> spHeads)
        })
        .toSet

      val isITF = And(
        (trivialFreeScenarios.map(v => Not(v))
          ++ otherScenariosCouples
            .map(ss => {
              val (vs -> sHeads, vsp -> spLasts) = ss
              Implies(
                And(vs, vsp),
                Or(
                  sHeads
                    .flatMap(head =>
                      spLasts.map(last => Reaches(graph, head, last))
                    )
                    .toSeq: _*
                )
              )
            })).toSeq: _*
      )

      val isFree = And(
        otherScenariosCouples
          .map(ss => {
            val (vs -> sHeads, vsp -> spLasts) = ss
            Implies(
              And(vs, vsp),
              Not(
                Or(
                  sHeads
                    .flatMap(head =>
                      spLasts.map(last => Reaches(graph, head, last))
                    )
                    .toSeq: _*
                )
              )
            )
          })
          .toSeq: _*
      )

      // for each scenario, the scenarios that are exclusive with it
      val exclusiveScenarios = idToScenario
        .transform((k, _) =>
          idToScenario.keySet.filter(kp =>
            k != kp && platform.finalExclusive(k, kp)
          )
        )

      // for each grouped scenario variable v, the other variables containing only scenario that are exclusive with all
      // scenario of v, thus these variables are exclusive

      val onSnPerGrouped = groupedLitToScenarios.transform((k, v) =>
        Implies(k, Or(v.map(scenarioToLit).toSeq: _*))
      )
      val nonExclusiveSn = scenarioToLit.transform((k, v) =>
        exclusiveScenarios(k)
          .map(scenarioToLit)
          .map(sLit => Implies(sLit, Not(v)))
      )

      val nonExclusive =
        (onSnPerGrouped.values ++ nonExclusiveSn.values.flatten)
          .map(SimpleAssert.apply)

      val serviceToScenarioLit = idToScenario.keySet
        .flatMap(k =>
          idToScenario(k).flatMap(tr => transactions(tr).map(_ -> k))
        )
        .groupMap(_._1)(kv => kv._2)
        .transform((_, v) => v)

      Problem(
        platform,
        groupedLitToScenarios,
        groupedLitToNodeSet,
        idToScenario,
        exclusiveScenarios,
        graph,
        isFree,
        isITF,
        Set.empty,
        edgeCst.values.toSet ++ nonExclusive,
        nodeToServices,
        serviceToScenarioLit,
        Some(maxSize)
      )
    }

    private def writeFooter(
        writer: FileWriter,
        computationTime: Double,
        size: Int = -1
    ): Unit =
      writer write
        s"""------------------------------------------
           ${if (size > -1) s"|Total: $size\n" else ""}
           |Computation time: ${computationTime}s
           |-------------------------------------------""".stripMargin

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
        platform: Platform & InterferenceSpecification
    ): Unit =
      writer write
        s"""Platform Name: ${platform.name.name}
           |File:  ${platform.sourceFile}
           |Date: ${java.time.LocalDateTime.now()}
           |------------------------------------------
           |""".stripMargin

    private def updateChannelFile(
        writer: Map[Int, FileWriter],
        channels: mutable.Map[Int, Map[Channel, Int]]
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
        m: Map[Int, Set[Set[UserScenarioId]]]
    ): Unit = {
      for ((k, v) <- m; ss <- v)
        writer(k).write(
          s"${multiTransactionId(ss.map(s => PhysicalScenarioId(s.id)))}\n"
        )
    }

    private def updateNumber(
        nbITF: mutable.Map[Int, Int],
        m: Map[Int, Set[Set[UserScenarioId]]]
    ): Unit = {
      for ((k, v) <- m)
        nbITF(k) = nbITF.getOrElse(k, 0) + v.size
    }

    private def updateChannelNumber(
        problem: Problem,
        channels: mutable.Map[Int, Map[Channel, Int]],
        physical: Set[Set[PhysicalScenarioId]],
        user: Map[Set[PhysicalScenarioId], Set[Set[UserScenarioId]]]
    ): Unit = {
      val channelNb = physical
        .flatMap(p => user(p).map(u => (problem.decodeChannel(p), u)))
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

    /** Compute the number of possible scenario sets for a given platform, this
      * result can be used to estimate the proportion of itf or free scenario
      * sets over all possible sets. It can be used to check that 2-ift + 2-free =
      * 2-non-exclusive (for higher cardinalities, the estimation of k-redundant
      * is needed)
      *
      * @param platform
      *   the studied platform
      * @return
      *   the number of scenario sets per size
      */
    def getSemanticsSize(
        platform: ConfiguredPlatform,
        max: Int
    ): Map[Int, BigInt] = platform match {
      case app: (ApplicativeTableBasedInterferenceSpecification &
            TransactionLibrary) =>
        getSemanticsSizeWithApp(app, max)
      case _ =>
        getSemanticsSizeWithoutApp(platform, max)
    }

    private def getSemanticsSizeWithoutApp(
        platform: ConfiguredPlatform,
        max: Int
    ): Map[Int, BigInt] = {
      val scenario = platform.purifiedScenarios
      val exclusive = platform.finalExclusive(scenario.keySet)
      val factory = new SymbolBDDFactory()
      val bdd =
        getNonExclusiveKBDD(scenario.keySet.toSeq, exclusive, max, factory)

      // for each cardinality, compute the number of satisfying assignments of the BDD encoding scenario sets
      // containing exactly k non exclusive scenarios
      val result = platform match {
        case l: TransactionLibrary =>
          val weightMap = scenario
            .transform((_, v) => l.scenarioUserName(v))
            .map(kv => kv._1.id -> kv._2.size)
            .filter(_._2 >= 1)
          bdd.transform((_, v) => factory.getPathCount(v, weightMap))
        case _ =>
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

      // when a scenario s is selected then at no other scenarios is exclusive with it
      // \bigwedge_{s \in scenarioVar} bdd(s) \Rightarrow not \bigvee_{s' \in exclusive(s)} bdd(s')
      val isExclusive = factory.andBDD(
        exclusive.map(p =>
          symbols(p._1).imp(factory.orBDD(p._2.map(symbols)).not)
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

    private def getSemanticsSizeWithApp(
        platform: ConfiguredLibraryBasedPlatform,
        max: Int
    ): Map[Int, BigInt] = {
      val factory = new SymbolBDDFactory()
      val result = getNonExclusiveKBDD(
        platform.scenarioByUserName.keys.toSeq,
        platform.finalUserScenarioExclusive,
        max,
        factory
      ).transform((_, v) => factory.getPathCount(v))
      factory.dispose()
      result
    }

    /** Compute the number of k-redundant scenario sets for a given platform, it
      * can be used to check that for all size, k-free + k-itf + k-redundant =
      * k-non-exclusive
      *
      * @param platform
      *   the platform to analyse
      * @param free
      *   the interference free scenario sets
      * @param itf
      *   the interference scenario sets
      * @return
      *   the number of k-redundant per size
      */
    @deprecated(
      "poor performance computation of k-redundant cardinal since based on a building out of free and itf" +
        "results that are classically very large"
    )
    def getRedundantCard(
        platform: ConfiguredPlatform,
        free: Set[Set[PhysicalScenarioId]],
        itf: Set[Set[PhysicalScenarioId]]
    ): Map[Int, BigInt] = {
      val idToScenario = platform.purifiedScenarios
      val exclusive = idToScenario.keySet.groupMapReduce(t => t)(t =>
        idToScenario.keySet.filter(platform.finalExclusive(t, _))
      )(_ ++ _)
      val allResults = free ++ itf
      val scenarioToMultiTransaction = allResults
        .flatMap(ss => ss.map(s => s -> ss))
        .groupMap(_._1)(_._2)
      val scenarioVar = idToScenario.keys.toSeq
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
      // if s in scenarioVar is selected then at least one free or itf is selected
      val sSelect: BDD = factory.andBDD(
        scenarioToMultiTransaction.map(p =>
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
      // if a result is selected then all of its scenarios are selected
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
            val exactlyK = factory.mkExactlyK(scenarioVar.map(_.id), k)
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
}
