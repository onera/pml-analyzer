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

package pml.operators

import pml.model.hardware.{Initiator, Platform, Target}
import pml.model.relations.UseRelation
import pml.model.service.{Load, Service, Store}
import pml.model.software.{Application, Data}
import pml.model.utils.Message._
import scalaz.Memo.immutableHashMapMemo
import views.interference.model.specification.InterferenceSpecification.{Path, PhysicalTransaction}
import pml.operators._
import scala.reflect._

/**
 * Base trait for used operator
 *
 * @tparam L the left type
 * @tparam R the right type
 */
trait Used[L, R] {
  def apply(a: L): Set[R]
}

object Used {

  /** ------------------------------------------------------------------------------------------------------------------
   * EXTENSION METHODS
   * --------------------------------------------------------------------------------------------------------------- */

  /**
   * If an element l can use an element r then the following operators can be used
   *
   * l (e.g. a [[pml.model.hardware.Target]]) can provide the hosted [[pml.model.software.Data]]
   * {{{l.hostedData }}}
   * l (e.g. a [[pml.model.hardware.Platform]]) can provide the hosted [[pml.model.software.Application]]
   * {{{l.applications }}}
   * l (e.g. a [[pml.model.hardware.Platform]]) can provide
   * the [[views.interference.model.specification.InterferenceSpecification.PhysicalTransaction]] that can occur
   * {{{l.usedTransactions}}}
   * l (e.g. a [[pml.model.hardware.Platform]]) can provide the multi-path
   * [[views.interference.model.specification.InterferenceSpecification.PhysicalTransaction]] that can occur
   * {{{l.multiPathsTransactions}}}
   *
   * @see usage can be found in [[pml.examples.simpleKeystone.SimpleKeystoneExport]]
   */
  trait Ops {

    /**
     * Extension method class
     */
    extension[L] (self: L) {

      /**
       * PML keyword to access to elements used by self
       *
       * @param ev the proof that self can use elements of type B
       * @tparam B the type of used elements
       * @return the set of used elements
       */
      def used[B]()(using ev: Used[L, B]): Set[B] = ev(self)
    }

    /**
     * Extension method class
     */
    extension (self: Target) {

      /**
       * PML keyword to access to data hosted by self
       *
       * @param ev the proof that self can host data
       * @return
       */
      def hostedData(using ev: Used[Target, Data]): Set[Data] = ev(self)
    }

    /**
     * Extension method class
     */
    extension[L <: Platform] (self: L) {

      /**
       * PML keyword to access to applications hosted by self
       *
       * @return the set of hosted applications
       */
      def applications: Set[Application] = Application.allDirect(self.currentOwner)

      /**
       * PML keyword to access to physical transactions used by self
       *
       * @param ev the proof that self can use transactions
       * @return the set of used physical transactions
       */
      def usedTransactions(using ev: Used[L, PhysicalTransaction]): Set[PhysicalTransaction] = ev(self)

      /**
       * PML keyword to access to multi-path physical transactions used by self
       *
       * @param ev the proof that self can use transactions
       * @return the set of multi path used physical transactions
       */
      def multiPathsTransactions(using ev: Used[L, PhysicalTransaction]): Set[Set[PhysicalTransaction]] = {
        Used.getMultiPaths(usedTransactions).values.toSet
      }
    }

    /**
     * Extension method class
     */
    extension (self: Data) {

      /**
       * PML keyword to access to the targets hosting self
       *
       * @param ev the proof that self can be hosted by targets
       * @return the set of targets
       */
      def hostingTargets(using ev: Used[Data, Target]): Set[Target] = ev(self)
    }

    /**
     * Extension method class
     */
    extension [L <: Application | Initiator](self: L) {

      /**
       * PML keyword to access to the load services used by self
       *
       * @param ev the proof that self can use load services
       * @return the used loads
       */
      def targetLoads(using ev: Used[L, Load]): Set[Load] = ev(self)

      /**
       * PML keyword to access to the store services used by self
       *
       * @param ev the proof that self can use store services
       * @return the used stores
       */
      def targetStores(using ev: Used[L, Store]): Set[Store] = ev(self)

      /**
       * PML keyword to access to the services used by self
       *
       * @param ev the proof that self can use services
       * @return the used services
       */
      def targetService(using ev: Used[L, Service]): Set[Service] = ev(self)

      /**
       * PML keyword to access to the initiator hosting self
       *
       * @param ev the proof that self can be hosted by initiators
       * @return the hosting initiators
       */
      def hostingInitiators(using ev: Used[L, Initiator]): Set[Initiator] = ev(self)
    }
  }

  /** ------------------------------------------------------------------------------------------------------------------
   * INFERENCE RULES
   * --------------------------------------------------------------------------------------------------------------- */

  // basic cases
  given[T, U] (using l: UseRelation[T, U]): Used[T, U] with {
    def apply(a: T): Set[U] = l(a)
  }


  given[T, U] (using l: UseRelation[T, U]): Used[U, T] with {
    def apply(a: U): Set[T] = l.inverse(a)
  }


  // derivations
  given[P <: Platform : Typeable]: Used[P, PhysicalTransaction] with {
    def apply(a: P): Set[PhysicalTransaction] = {
      import a._

      def usedTransactionsBy[U <: Initiator | Application](x: U)(using
                                                                 u:Used[U,Service],
                                                                 r:Restrict[Map[Service, Set[Service]],(U,Service)],
                                                                 typeable: Typeable[U & Initiator]): Set[Path[Service]] = {
        val allTargets = x.targetService

        val serviceGraph = allTargets.groupMapReduce(s => s)(s => r((x, s)))((l, r) => l ++ r)

        val fromServices = x match{
          case app: Application => app.hostingInitiators.flatMap(_.services)
          case ini: Initiator => ini.services
        }

        val paths = serviceGraph.transform((_, graph) => fromServices flatMap { from => pathsIn(from, graph) })

        val result = paths.values.flatten.toSet
        x match {
          case sw: Application =>
            checkTransactions(result, allTargets, Some(sw)).foreach(println)
            checkApplicationAllocation(sw)
          case _:Initiator =>
            checkTransactions(result, allTargets, None).foreach(println)
        }
        result
      }

      val result = a.applications.flatMap(usedTransactionsBy) ++ a.initiators.flatMap(usedTransactionsBy)
      result
    }
  }

  given [AI <: Application | Initiator, S <: Service : Typeable] (using l: Used[AI, Service]): Used[AI, S] with {
    def apply(a: AI): Set[S] = l(a) collect { case s : S => s }
  }

  given [A <:Application, I<:Initiator : Typeable] (using l:Used[Application,Initiator]): Used[A,I] with {
    def apply(a: A): Set[I] = l(a) collect { case s : I => s }
  }

  /** ------------------------------------------------------------------------------------------------------------------
   * UTIL METHODS
   * --------------------------------------------------------------------------------------------------------------- */

  private def checkApplicationAllocation(a: Application)(implicit uSWSrv: Used[Application, Service],
                                                         uAS: Used[Application, Initiator],
                                                         pSB: Provided[Initiator, Service]): Set[String] = {
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

  /**
   * Compute all the path from a given element to the leaf services
   * This methods handle cyclic graphs by simply cutting the loop when traversing it
   *
   * @param from  the initial service
   * @param graph the edges of the graph
   * @tparam A the type of the parent nodes
   * @tparam B the type of the son nodes
   * @return all the possible paths
   */
  private def pathsIn[A, B <: A](from: A, graph: Map[A, Set[B]]): Set[Path[A]] = {

    /**
     * This function value compute the path from a node of the graph to its leaf nodes (first element of the Pair).
     * A set of visited nodes is also provided (second element of the Pair) to cut cycles
     * The result are memoized to avoid multiple computation of the paths
     */
    lazy val _paths: ((A, Set[A])) => Set[Path[A]] = immutableHashMapMemo {
      s =>
        if (s._2.contains(s._1)) {
          println(cyclicGraphWarning)
          Set(Nil)
        }
        else if (s._2.contains(s._1) || !graph.contains(s._1) || graph(s._1).isEmpty)
          Set(Nil)
        else {
          for {
            next <- graph(s._1)
            path <- _paths(next, s._2 + s._1)
          } yield
            next +: path
        }
    }

    //remove empty paths (i.e. from is not connected to anyone in the graph) and add from as path head
    _paths((from, Set.empty)) collect {
      case p if p.nonEmpty => from +: p
    }
  }

  def checkImpossible(s: Set[PhysicalTransaction], target: Set[Service] = Set.empty, a: Option[Application] = None): Set[String] = {
    target
      .filterNot(t => s.exists(_.last == t))
      .map(impossibleRouteWarning(_, a))
  }

  private def getMultiPaths(s: Set[PhysicalTransaction]): Map[(Service, Service), Set[PhysicalTransaction]] =
    s.groupBy(t => (t.head, t.last))
      .filter(_._2.size >= 2)

  def checkMultiPaths(s: Set[PhysicalTransaction]): Set[String] =
    getMultiPaths(s)
      .map(kv => multiPathRouteWarning(kv._1._1, kv._1._2, kv._2)).toSet


  private def checkTransactions(s: Set[PhysicalTransaction], target: Set[Service] = Set.empty, a: Option[Application] = None): Set[String] =
    checkImpossible(s, target, a) ++ checkMultiPaths(s)
}
