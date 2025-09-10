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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.PMLNodeMap
import onera.pmlanalyzer.pml.model.hardware.{Initiator, Platform, Target}
import onera.pmlanalyzer.pml.model.relations.UseRelation
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.model.utils.Message.*
import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{AtomicTransaction, AtomicTransactionId, Path}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.operators.DelayedTransform.TransactionParam

import scala.collection.mutable
import scala.reflect.*

/** Base trait for used operator
  *
  * @tparam L
  *   the left type
  * @tparam R
  *   the right type
  */
trait Used[L, R] {
  def apply(a: L): Set[R]
}

object Used {

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** If an element l can use an element r then the following operators can be
    * used
    *
    * l (e.g. a [[pml.model.hardware.Target]]) can provide the hosted
    * [[pml.model.software.Data]] {{{l.hostedData}}} l (e.g. a
    * [[pml.model.hardware.Platform]]) can provide the hosted
    * [[pml.model.software.Application]] {{{l.applications}}} l (e.g. a
    * [[pml.model.hardware.Platform]]) can provide the
    * [[views.interference.model.specification.InterferenceSpecification.PhysicalTransaction]]
    * that can occur {{{l.usedTransactions}}} l (e.g. a
    * [[pml.model.hardware.Platform]]) can provide the multi-path
    * [[views.interference.model.specification.InterferenceSpecification.PhysicalTransaction]]
    * that can occur {{{l.multiPathsTransactions}}}
    *
    * @see
    *   usage can be found in
    *   [[pml.examples.simpleKeystone.SimpleKeystoneExport]]
    */
  trait Ops {

    /** Extension method class
      */
    extension [L](self: L) {

      /** PML keyword to access to elements used by self
        *
        * @param ev
        *   the proof that self can use elements of type B
        * @tparam B
        *   the type of used elements
        * @return
        *   the set of used elements
        */
      def used[B](using ev: Used[L, B]): Set[B] = ev(self)
    }

    /** Extension method class
      */
    extension (self: Target) {

      /** PML keyword to access to data hosted by self
        *
        * @param ev
        *   the proof that self can host data
        * @return
        */
      def hostedData(using ev: Used[Target, Data]): Set[Data] = ev(self)
    }

    /** Extension method class
      */
    extension [L <: Platform](self: L) {

      /** PML keyword to access to applications hosted by self
        *
        * @return
        *   the set of hosted applications
        */
      def applications: Set[Application] = {
        import self.*
        Application.allDirect(self.currentOwner)
      }

      /** PML keyword to access to physical transactions used by self
        *
        * @param ev
        *   the proof that self can use transactions
        * @return
        *   the set of used physical transactions
        */
      def issuedAtomicTransactions(using
          ev: Used[L, AtomicTransaction]
      ): Set[AtomicTransaction] = ev(self)

      /** PML keyword to access to multi-path physical transactions used by self
        *
        * @param ev
        *   the proof that self can use transactions
        * @return
        *   the set of multi path used physical transactions
        */
      def multiPathsTransactions(using
          ev: Used[L, AtomicTransaction]
      ): Set[Set[AtomicTransaction]] = {
        Used.getMultiPaths(issuedAtomicTransactions).values.toSet
      }
    }

    /** Extension method class
      */
    extension (self: Data) {

      /** PML keyword to access to the targets hosting self
        *
        * @param ev
        *   the proof that self can be hosted by targets
        * @return
        *   the set of targets
        */
      def hostingTargets(using ev: Used[Data, Target]): Set[Target] = ev(self)
    }

    /** Extension method class
      */
    extension [L <: Application | Initiator](self: L) {

      /** PML keyword to access to the load services used by self
        *
        * @param ev
        *   the proof that self can use load services
        * @return
        *   the used loads
        */
      def targetLoads(using ev: Used[L, Load]): Set[Load] = ev(self)

      /** PML keyword to access to the store services used by self
        *
        * @param ev
        *   the proof that self can use store services
        * @return
        *   the used stores
        */
      def targetStores(using ev: Used[L, Store]): Set[Store] = ev(self)

      /** PML keyword to access to the services used by self
        *
        * @param ev
        *   the proof that self can use services
        * @return
        *   the used services
        */
      def targetService(using ev: Used[L, Service]): Set[Service] = ev(self)

      /** PML keyword to access to the initiator hosting self
        *
        * @param ev
        *   the proof that self can be hosted by initiators
        * @return
        *   the hosting initiators
        */
      def hostingInitiators(using ev: Used[L, Initiator]): Set[Initiator] = ev(
        self
      )

      /** PML keyword to access to the applications hosted by self
       *
       * @param ev
       * the proof that self can be hosted by initiators
       * @return
       * the hosted applications
       */
      def hostedApplications(using ev: Used[L, Application]): Set[Application] =
        ev(
          self
        )
    }
    extension[T] (y: => T) {
      
      def toTransactionParam (using ev: DelayedTransform[T, TransactionParam]): TransactionParam = 
        ev(y)
    }

    extension [T](x: T) {

      /** Method that should be provided by sub-classes to access to the path
       *
       * @return
       * the set of service paths
       */
      def paths(using
          ev: Transform[T, Set[AtomicTransaction]]
      ): Set[AtomicTransaction] =
        ev(x)

      /** Check if the target is in the possible targets of the transaction
       *
       * @param t
       * target to find
       * @return
       * true if the target is contained
       */
      def useTarget(
          t: Target
      )(using
          ev: Transform[T, Set[AtomicTransaction]],
          p: Provided[Target, Service]
      ): Boolean =
        usedTargets.contains(t)

      /** Provide the targets of the transaction
       *
       * @return
       * the set of targets
       */
      def usedTargets(using
          ev: Transform[T, Set[AtomicTransaction]],
          p: Provided[Target, Service]
      ): Set[Target] =
        paths.filter(_.size >= 2).flatMap(t => t.last.targetOwner)

      /** Provide the initiators fo a transaction
       *
       * @return
       * the set of initiators
       */
      def usedInitiators(using
          ev: Transform[T, Set[AtomicTransaction]],
          p: Provided[Initiator, Service]
      ): Set[Initiator] =
        paths.filter(_.nonEmpty).flatMap(t => t.head.initiatorOwner)

      /** Check is the initiator is in the possible initiators of the transaction
       *
       * @param ini
       * initiator to find
       * @return
       * true if the initiator is contained
       */
      def useInitiator(
          ini: Initiator
      )(using
          ev: Transform[T, Set[AtomicTransaction]],
          p: Provided[Initiator, Service]
      ): Boolean =
        usedInitiators.contains(ini)
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  // basic cases
  given [T, U](using l: UseRelation[T, U]): Used[T, U] with {
    def apply(a: T): Set[U] = l(a)
  }

  given [T, U](using l: UseRelation[T, U]): Used[U, T] with {
    def apply(a: U): Set[T] = l.inverse(a)
  }

  // derivations
  given [P <: Platform: Typeable]: Used[P, AtomicTransaction] with {
    def apply(a: P): Set[AtomicTransaction] = {
      import a._

      val (appPaths, appWarnings) = a.applications.map(usedTransactionsBy).unzip
      val (iniPaths, iniWarnings) = a.initiators.map(usedTransactionsBy).unzip

      (appWarnings.flatten ++ iniWarnings.flatten).toSeq.sorted.foreach(println)

      appPaths.flatten ++ iniPaths.flatten
    }

    def usedTransactionsBy[U <: Initiator | Application](x: U)(using
        uA: Used[Application, Initiator],
        uAI: Used[Application, Service],
        uS: Used[U, Service],
        uI: Used[U, Initiator],
        p: Provided[Initiator, Service],
        r: Restrict[(Map[Service, Set[Service]], Set[String]), (U, Service)],
        typeable: Typeable[U & Initiator]
    ): (Set[Path[Service]], Set[String]) = {

      // get all target services for x
      val allTargets = x.targetService

      val warningRestrict = mutable.Set.empty[String]
      // get the service graph from x services to reach each target service.
      val serviceGraph =
        allTargets.groupMapReduce(s => s)(s => {
          val (graph, warnings) = r((x, s))
          warningRestrict ++= warnings
          graph
        })((l, r) => l ++ r)

      // compute the services of x
      val fromServices = x.hostingInitiators.flatMap(_.services)

      // compute the paths within each graph from the services of x
      val (paths, warningPaths) = (for {
        iniS <- fromServices
        graph <- serviceGraph.values if graph.contains(iniS)
      } yield {
        pathsIn(iniS, graph)
      }).unzip

      val result = paths.flatten

      x match {
        case sw: Application =>
          checkTransactions(result, allTargets, Some(sw)).toSeq.sorted
            .foreach(println)
          checkApplicationAllocation(sw)
        case _: Initiator =>
          checkTransactions(result, allTargets, None).toSeq.sorted
            .foreach(println)
      }
      (result, warningPaths.flatten ++ warningRestrict)
    }
  }

  given [AI <: Application | Initiator, S <: Service: Typeable](using
      l: Used[AI, Service]
  ): Used[AI, S] with {
    def apply(a: AI): Set[S] = l(a) collect { case s: S => s }
  }

  given [A <: Application, I <: Initiator: Typeable](using
      l: Used[Application, Initiator]
  ): Used[A, I] with {
    def apply(a: A): Set[I] = l(a) collect { case s: I => s }
  }

  given [AI <: Application | Initiator, I <: Initiator: Typeable](using
      l: Used[Application, Initiator]
  ): Used[AI, I] with {
    def apply(a: AI): Set[I] =
      a match
        case i: I           => Set(i)
        case a: Application => l(a) collect { case s: I => s }
        case _              => Set.empty
  }

  /** ------------------------------------------------------------------------------------------------------------------
    * UTIL METHODS
    * ---------------------------------------------------------------------------------------------------------------
    */

  private def checkApplicationAllocation(a: Application)(implicit
      uSWSrv: Used[Application, Service],
      uAS: Used[Application, Initiator],
      pSB: Provided[Initiator, Service]
  ): Set[String] = {
    val noTarget =
      if (a.targetService.isEmpty)
        Set(applicationNotUsingServicesWarning(a))
      else Set.empty
    val noExecutor = a match {
      case application: Application if application.hostingInitiators.isEmpty =>
        Set(applicationNotAllocatedWarning(application))
      case _ =>
        Set.empty
    }
    val noBasics = a.hostingInitiators
      .filter(s => s.services.isEmpty)
      .map(noServiceInitiatorWarning(a, _))

    noTarget ++ noBasics ++ noExecutor
  }

  /** Compute all the path from a given element to the leaf services This
    * methods handle cyclic graphs by simply cutting the loop when traversing it
    *
    * @param from
    *   the initial service
    * @param graph
    *   the edges of the graph
    * @tparam A
    *   the type of the parent nodes
    * @tparam B
    *   the type of the son nodes
    * @return
    *   all the possible paths
    */
  private def pathsIn[A, B <: A](
      from: A,
      graph: Map[A, Set[B]]
  ): (Set[Path[A]], Set[String]) = {

    /** This function value compute the path from a node of the graph to its
      * leaf nodes (first element of the Pair). A set of visited nodes is also
      * provided (second element of the Pair) to cut cycles The result are
      * memoized to avoid multiple computation of the paths
      */
    def _paths(
        current: A,
        path: Path[A],
        visited: Set[A]
    ): (Set[Path[A]], Set[String]) = {
      if (visited.contains(current)) {
        (Set.empty, Set(cyclicGraphWarning))
      } else if (!graph.contains(current) || graph(current).isEmpty)
        (Set(path :+ current), Set.empty)
      else {
        val (paths, warnings) = graph(current)
          .map(next => _paths(next, path :+ current, visited + current))
          .unzip
        (
          paths.flatten filter {
            _.nonEmpty
          },
          warnings.flatten
        )
      }
    }

    _paths(from, Nil, Set.empty)
  }

  def checkImpossible(
      s: Set[AtomicTransaction],
      target: Set[Service] = Set.empty,
      a: Option[Application] = None
  ): Set[String] = {
    target
      .filterNot(t => s.exists(_.last == t))
      .map(impossibleRouteWarning(_, a))
  }

  private def getMultiPaths(
      s: Set[AtomicTransaction]
  ): Map[(Service, Service), Set[AtomicTransaction]] =
    s.groupBy(t => (t.head, t.last))
      .filter(_._2.size >= 2)

  def checkMultiPaths(s: Set[AtomicTransaction]): Set[String] =
    getMultiPaths(s)
      .map(kv => multiPathRouteWarning(kv._1._1, kv._1._2, kv._2))
      .toSet

  private def checkTransactions(
      s: Set[AtomicTransaction],
      target: Set[Service] = Set.empty,
      a: Option[Application] = None
  ): Set[String] =
    checkImpossible(s, target, a) ++ checkMultiPaths(s)
}
