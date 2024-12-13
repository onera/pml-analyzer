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

import onera.pmlanalyzer.pml.examples.mySys.MyProcPlatform
import onera.pmlanalyzer.pml.examples.simpleKeystone.SimpleKeystonePlatform
import onera.pmlanalyzer.pml.examples.simpleT1042.SimpleT1042Platform
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Platform}
import onera.pmlanalyzer.pml.model.relations.{
  AuthorizeRelation,
  LinkRelation,
  RoutingRelation,
  UseRelation
}
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification

import scala.collection.mutable
import scala.collection.mutable.HashMap as MHashMap

/** Base trait for restrict operator used to restrict the connection graph of
  * elements L to the elements R that are used
  *
  * @tparam L
  *   the left type
  * @tparam R
  *   the right type
  */
trait Restrict[L, R] {

  private val _memo =
    MHashMap
      .empty[(Service, Initiator, Service, Service), (Boolean, Set[String])]

  def usedForTgt[U <: Service](tgt: U, ini: Initiator, from: U, to: U)(implicit
      lU: Linked[U, U],
      r: RoutingRelation[(Initiator, Service, Service), Service]
  ): (Boolean, Set[String]) =
    _memo.getOrElseUpdate(
      (tgt, ini, from, to),
      usedForTgt(tgt, ini, from, to, Seq.empty[(U, U)])
    )

  /** Reachability test function taking into account the routing function
   *
   * @param tgt
   * the final service to reach
   * @param ini
   * the initiator requesting to reach the final service
   * @param from
   * the left hand-side of the considered edge in the graph
   * @param to
   * the right hand-side of the considered edge in the graph
   * @param visited
   * the visited edges
   * @param lU
   * the proof that services are connected
   * @param r
   * the routing relation
   * @tparam U
   * the type of service considered
   * @return
   * true iff the link from-to is used by the initiator to reach the target
   */
  private def usedForTgt[U <: Service](
      tgt: U,
      ini: Initiator,
      from: U,
      to: U,
      visited: Seq[(U, U)]
  )(implicit
      lU: Linked[U, U],
      r: RoutingRelation[(Initiator, Service, Service), Service]
  ): (Boolean, Set[String]) = {
    if (to == tgt)
      (true, Set.empty)
    else if (visited.contains((from, to))) {
      (
        false,
        Set(
          Message.cycleWarning(visited.head._1 +: visited.map(_._2), ini, tgt)
        )
      )
    } else {
      val successors =
        r.get((ini, tgt, to)) match
          case Some(routed) => lU(to).filter(routed.contains)
          case None =>
            lU(to)

      successors.foldLeft((false, Set.empty[String]))((acc, succ) =>
        if (acc._1)
          acc
        else {
          val (isUsedNext, warnings) =
            usedForTgt(tgt, ini, to, succ, visited :+ (from, to))
          (isUsedNext, warnings ++ acc._2)
        }
      )
    }
  }

  def apply(b: R): L
}

object Restrict {

  def apply[L, R](using ev: Restrict[L, R]): Restrict[L, R] = ev

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** @note
    *   restrict operators is an advanced feature and should not be necessary
    *   for models
    */
  trait Ops {

    extension (self: Application) {
      def serviceGraph(using
          ev: Restrict[Map[Service, Set[Service]], Application]
      ): Map[Service, Set[Service]] = ev(self)
      def hardwareGraph(using
          ev: Restrict[Map[Hardware, Set[Hardware]], Application]
      ): Map[Hardware, Set[Hardware]] = ev(self)
    }

    /** Extension method class
     *
     * the element on which keyword can be used
      */
    extension (self: Platform) {

      /** PML keyword to access to the service graph of an application
        *
        * @param s
        *   the application
        * @return
        *   its service graph
        */
      def serviceGraphOf(s: Application): Map[Service, Set[Service]] = {
        import self._
        val ev = implicitly[
          Restrict[(Map[Service, Set[Service]], Set[String]), Application]
        ]
        ev(s)._1
      }

      /** PML keyword to access to the service graph of the full platform
        *
        * @return
        *   its hardware graph
        */
      def serviceGraph(): Map[Service, Set[Service]] = {
        self.applications
          .flatMap {
            self.serviceGraphOf
          }
          .groupMapReduce(_._1)(_._2)(_ ++ _)
      }

      def serviceGraphWithInterfere(): Map[Set[Service], Set[Set[Service]]] =
        self match {
          case spec: InterferenceSpecification =>
            val services = self.services
            val initialGraph = serviceGraph()
            val exclusiveServices = services
              .map(s =>
                s -> services.filter(s2 => spec.finalInterfereWith(s, s2))
              )
              .toMap
            val newNodes = services.flatMap(s =>
              if (exclusiveServices(s).isEmpty) Set(Set(s))
              else exclusiveServices(s).map(s2 => Set(s, s2))
            )
            newNodes
              .map(n =>
                n -> (for {
                  n2 <- newNodes
                  if n != n2
                  if n.intersect(n2).nonEmpty || n.exists(s =>
                    n2.exists(s2 =>
                      initialGraph.contains(s) && initialGraph(s).contains(s2)
                    )
                  )
                } yield {
                  n2
                })
              )
              .toMap
          case _ =>
            serviceGraph().groupMapReduce(p => Set(p._1))(p =>
              p._2.map(s => Set(s))
            )(_ ++ _)
        }

      def fullServiceGraphWithInterfere()
          : Map[Set[Service], Set[Set[Service]]] =
        self match {
          case spec: InterferenceSpecification =>
            val services = self.services
            val initialGraph = self.ServiceLinkableToService.edges
            val exclusiveServices = services
              .map(s =>
                s -> services.filter(s2 => spec.finalInterfereWith(s, s2))
              )
              .toMap
            val newNodes = services.flatMap(s =>
              if (exclusiveServices(s).isEmpty) Set(Set(s))
              else exclusiveServices(s).map(s2 => Set(s, s2))
            )
            newNodes
              .map(n =>
                n -> (for {
                  n2 <- newNodes
                  if n != n2
                  if n.intersect(n2).nonEmpty || n.exists(s =>
                    n2.exists(s2 =>
                      initialGraph.contains(s) && initialGraph(s).contains(s2)
                    )
                  )
                } yield {
                  n2
                })
              )
              .toMap
          case _ =>
            self.ServiceLinkableToService.edges.groupMapReduce(p => Set(p._1))(
              p => p._2.map(s => Set(s))
            )(_ ++ _)
        }

      /** PML keyword to access to the hardware graph of the considered element
        *
        * @return
        *   its hardware graph
        */
      def hardwareGraph(): Map[Hardware, Set[Hardware]] =
        self.applications
          .flatMap {
            self.hardwareGraphOf
          }
          .groupMapReduce(_._1)(_._2)(_ ++ _)

      /** PML keyword to access to hardware graph used by an application
        *
        * @param s
        *   the application
        * @return
        *   its hardware graph
        */
      def hardwareGraphOf(s: Application): Map[Hardware, Set[Hardware]] = {
        import self._
        val ev = implicitly[
          Restrict[(Map[Hardware, Set[Hardware]], Set[String]), Application]
        ]
        ev(s)._1
      }

      /** PML keyword to access to the service graph used by an application to
        * access a target
        *
        * @param s
        *   the application
        * @param tgt
        *   the target service
        * @return
        *   its service graph
        */
      def serviceGraphOf(
          s: Application,
          tgt: Service
      ): Map[Service, Set[Service]] = {
        import self._
        val ev = implicitly[
          Restrict[
            (Map[Service, Set[Service]], Set[String]),
            (Application, Service)
          ]
        ]
        ev((s, tgt))._1
      }

      /** PML keyword to access to the hardware graph used by an application to
        * access a target
        *
        * @param s
        *   the application
        * @param tgt
        *   the target service
        * @return
        *   its service graph
        */
      def hardwareGraphOf(
          s: Application,
          tgt: Service
      ): Map[Hardware, Set[Hardware]] = {
        import self._
        val ev = implicitly[
          Restrict[
            (Map[Hardware, Set[Hardware]], Set[String]),
            (Application, Service)
          ]
        ]
        ev((s, tgt))._1
      }

    }

  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * ---------------------------------------------------------------------------------------------------------------
    */

  /** A restricted can be obtained from an endomorphism over services
    *
    * @param lS
    *   the proof that the endomorphism exists
    * @param uL
    *   the proof that an application uses loads
    * @param uS
    *   the proof that an application uses stores
    * @param uSI
    *   the proof that an application uses initiators
    * @param aR
    *   the proof that an authorize relation exists
    * @param r
    *   the proof that a routing relation exists
    * @param pB
    *   the proof that an initiator provide services
    * @return
    *   an object building service graph for applications
    */
  given (using
      lS: LinkRelation[Service],
      uL: Used[Application, Load],
      uS: Used[Application, Store],
      uSI: Used[Application, Initiator],
      aR: AuthorizeRelation[Application, Service],
      r: RoutingRelation[(Initiator, Service, Service), Service],
      pB: Provided[Initiator, Service]
        ): Restrict[(Map[Service, Set[Service]], Set[String]), Application] with {

    /** Check if the application uses some route between an initial service and
      * a target service
      *
      * @param a
      *   the application
      * @param from
      *   the initial service
      * @param to
      *   the target service
      * @param u
      *   the proof that application uses services of type U
      * @param lU
      *   the proof that services of type U can be linked
      * @tparam U
      *   the type of the service
      * @return
      *   if the application uses some route between from and to
      */
    def useBySW[U <: Service](a: Application, from: U, to: U)(implicit
        u: Used[Application, U],
        lU: Linked[U, U]
    ): (Boolean, Set[String]) = {
      u(a).foldLeft((false, Set.empty[String]))((acc, tgt) => {
        if (acc._1 || !aR(a).contains(tgt))
          acc
        else {
          a.hostingInitiators.foldLeft((false, acc._2))((acc2, ini) => {
            if (acc2._1)
              acc2
            else {
              val (isUsed, isUsedWarnings) = usedForTgt(tgt, ini, from, to)
              (isUsed, isUsedWarnings ++ acc2._2)
            }
          })
        }
      })
    }

    def used(
              a: Application,
              from: Service,
              to: Service
            ): (Boolean, Set[String]) =
      (from, to) match {
        case (fromL: Load, toL: Load)   => useBySW(a, fromL, toL)
        case (fromS: Store, toS: Store) => useBySW(a, fromS, toS)
        case _ => (false, Set.empty)
      }

    /** Provide the service graph of an application
      *
      * @param b
      *   the application
      * @return
      *   its service graph
      */
    def apply(b: Application): (Map[Service, Set[Service]], Set[String]) = {
      val warnings = mutable.Set.empty[String]
      val graph = lS.edges collect { case (from, linked) =>
        from -> {
          linked filter { to => {
              val (isUsed, cycleWarnings) = used(b, from, to)
              if (isUsed)
                warnings ++= cycleWarnings
              isUsed
            }
          }
        }
      } filter {
        _._2.nonEmpty
      }
      (graph, warnings.toSet)
    }
  }

  given [T](using
      lP: LinkRelation[Hardware],
      pS: Provided[Hardware, Service],
            restrict: Restrict[(Map[Service, Set[Service]], Set[String]), T]
           ): Restrict[(Map[Hardware, Set[Hardware]], Set[String]), T] with {
    def apply(b: T): (Map[Hardware, Set[Hardware]], Set[String]) = {
      val restricted = restrict(b)
      // shortcut non-owner services, if k -> v and k is not owned by any HW then push v to all predecessors of k
      val nonOwnedServices = restricted._1.keySet.collect {
        case b: Service if b.hardwareOwner.isEmpty => b
      }
      val shortcut = nonOwnedServices.foldLeft(restricted._1)((acc, toRemove) =>
        {
          for {
            toAdd <- acc.get(toRemove)
            in = acc.keySet.filter(k => acc(k).contains(toRemove))
          } yield {
            acc
              .removed(toRemove)
              .transform((k, v) => if (in.contains(k)) v ++ toAdd else v)
          }
        } getOrElse acc
      )
      val usedPLLinks = lP.edges
        .transform((k, v) =>
          v.filter(t =>
            shortcut.exists(ks =>
              k.services.contains(ks._1) && t.services.intersect(ks._2).nonEmpty
            )
          )
        )
        .filter {
          _._2.nonEmpty
        }
      val lostBasicEdges = shortcut
        .collect({ case (k: Service, v) => k -> v })
        .transform((si, v) =>
          v.filterNot(st =>
            usedPLLinks.exists(kvPL =>
              kvPL._1.services
                .contains(si) && kvPL._2.flatMap(_.services).contains(st)
            )
          )
        )
        .filter(_._2.nonEmpty)
        .toSeq
        .flatMap(kv =>
          kv._2.flatMap(t =>
            t.hardwareOwner.flatMap(tPL => kv._1.hardwareOwner.map(_ -> tPL))
          )
        )
      // WARNING if a link si -> st exists where si and st have owner then a physical link is added
      val completedPLLinks =
        lostBasicEdges.foldLeft(usedPLLinks)((acc, toAdd) =>
          {
            for { v <- acc.get(toAdd._1) } yield acc.updated(
              toAdd._1,
              v + toAdd._2
            )
          }.getOrElse(acc + (toAdd._1 -> Set(toAdd._2)))
        )
      (completedPLLinks, restricted._2)
    }
  }

  given [T <: Application | Initiator](using
      lS: LinkRelation[Service],
      uSI: Used[Application, Initiator],
      aR: AuthorizeRelation[Application, Service],
                                       r: RoutingRelation[(Initiator, Service, Service), Service]
                                      ): Restrict[(Map[Service, Set[Service]], Set[String]), (T, Service)] with {

    def used(
              a: T,
              tgt: Service,
              from: Service,
              to: Service
            ): (Boolean, Set[String]) = {
      val authorized = a match {
        case app: Application => aR(app).contains(tgt);
        case _                => true
      }
      val hostingInitiators = a match {
        case app: Application => app.hostingInitiators
        case i: Initiator     => Set(i)
      }
      if (!authorized)
        (false, Set.empty)
      else {
        (tgt, to, from) match {
          case (tgtL: Load, toL: Load, fromL: Load) =>
            hostingInitiators.foldLeft((false, Set.empty[String]))(
              (acc, ini) => {
                if (acc._1)
                  acc
                else {
                  val (isUsedRes, isUsedWarnings) =
                    usedForTgt(tgtL, ini, fromL, toL)
                  (isUsedRes, acc._2 ++ isUsedWarnings)
                }
              }
            )
          case (tgtS: Store, toS: Store, fromS: Store) =>
            hostingInitiators.foldLeft((false, Set.empty[String]))(
              (acc, ini) => {
                if (acc._1)
                  acc
                else {
                  val (isUsedRes, isUsedWarnings) =
                    usedForTgt(tgtS, ini, fromS, toS)
                  (isUsedRes, acc._2 ++ isUsedWarnings)
                }
              }
            )
          case _ => (false, Set.empty)
        }
      }
    }

    def apply(b: (T, Service)): (Map[Service, Set[Service]], Set[String]) = {
      val warnings = mutable.Set.empty[String]
      val graph = lS.edges collect { case (from, linked) =>
        from -> {
          linked filter { to => {
              val (isUsed, cycleWarning) = used(b._1, b._2, from, to)
              if (isUsed)
                warnings ++= cycleWarning
              isUsed
            }
          }
        }
      } filter {
        _._2.nonEmpty
      }
      (graph, warnings.toSet)
    }
  }
}
