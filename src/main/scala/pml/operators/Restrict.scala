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

import pml.model.hardware.{Hardware, Initiator, Platform}
import pml.model.relations.{AuthorizeRelation, LinkRelation, RoutingRelation, UseRelation}
import pml.model.service.{Load, Service, Store}
import pml.model.software.Application
import pml.model.utils.Message

import scala.collection.mutable.HashMap as MHashMap

/**
  * Base trait for restrict operator used to restrict the connection graph
  * of elements L to the elements R that are used
  *
  * @tparam L the left type
  * @tparam R the right type
  */
trait Restrict[L, R] {

  private val _memo = MHashMap.empty[(Service, Initiator, Service, Service), Boolean]

  private val _memoRoute = MHashMap.empty[(Service, Initiator, Service, Service), Boolean]

  def usedForTgt[U <: Service](tgt: U, ini: Initiator, from: U, to: U)(implicit
                                                                       lU: Linked[U, U],
                                                                       p: Provided[Initiator, Service],
                                                                       r: RoutingRelation[(Initiator, Service, Service), Service]): Boolean =
    _memo.getOrElseUpdate((tgt, ini, from, to), usedForTgt(tgt, ini, from, to, Seq.empty[(U, U)]))

  /**
    * The service x is used to reach target from the initiator (knowing a set of visited links) either if
    * x is the target service or it exists a service connected to x that is used to reach the target
    *
    * @param tgt       the target service
    * @param initiator the initiator of the request
    * @param x         the actual service
    * @param visited   the set of visited tgt-x services visited
    * @param lU        the link relation between services
    * @param p         the provide relation of an initiator
    * @param r         the routing relation of the platform
    * @tparam U the type of the service
    * @return true is the service is used to reach tgt
    */
  private def usedNext[U <: Service](tgt: U, initiator: Initiator, x: U, visited: Seq[(U, U)])(implicit
                                                                                               lU: Linked[U, U],
                                                                                               p: Provided[Initiator, Service],
                                                                                               r: RoutingRelation[(Initiator, Service, Service), Service]): Boolean =
    x == tgt ||
      lU(x).exists(u => usedForTgt(tgt, initiator, x, u, visited))

  /**
    * A service "to" can be accessed from another service "on" to reach a target service "tgt"
    * from a given initiator "ini" if "on" either
    * if "on" has no predecessor and is the initiator services
    * or it exists a predecessor of "on" that is routed
    * Moreover if routing restriction applied (r.get((ini, tgt, on)) is defined) then "to" must be a viable option
    *
    * @param ini
    * @param tgt
    * @param on
    * @param to
    * @param visited
    * @param lU
    * @param p
    * @param r
    * @tparam U
    * @return
    */
  private def isRouted[U <: Service](ini: Initiator, tgt: U, on: U, to: U, visited: Seq[(U, U)])(implicit
                                                                                                 lU: Linked[U, U],
                                                                                                 p: Provided[Initiator, Service],
                                                                                                 r: RoutingRelation[(Initiator, Service, Service), Service]): Boolean =
    _memoRoute.getOrElseUpdate(
      (tgt, ini, on, to),
      {
        if (visited.contains(on, to)) {
          println(Message.cycleWarning(visited, ini, tgt))
          false
        } else {
          val pred = lU.inverse(on)
          r.get((ini, tgt, on)) match {
            case None =>
              (pred.isEmpty && ini.services.contains(on)) || (pred exists { x =>
                isRouted(ini, tgt, x, on, visited :+ ((on, to)))
              })
            case Some(s) =>
              s.contains(to) && ((pred.isEmpty && ini.services.contains(on)) || (pred exists { x =>
                isRouted(ini, tgt, x, on, visited :+ ((on, to)))
              }))
          }
        }
      })

  //visited is added to cut cycles
  private def usedForTgt[U <: Service](tgt: U, ini: Initiator, from: U, to: U, visited: Seq[(U, U)])(implicit
                                                                                                     lU: Linked[U, U],
                                                                                                     p: Provided[Initiator, Service],
                                                                                                     r: RoutingRelation[(Initiator, Service, Service), Service]): Boolean = {
    if (visited.contains((from, to))) {
      println(Message.cycleWarning(visited, ini, tgt))
      false
    } else {
      isRouted(ini, tgt, from, to, visited) && usedNext(tgt, ini, to, visited :+ ((from, to)))
    }
  }

  def apply(b: R): L
}

object Restrict {

  def apply[L,R](using ev:Restrict[L,R]):Restrict[L,R] = ev

  /** ------------------------------------------------------------------------------------------------------------------
    * EXTENSION METHODS
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * @note restrict operators is an advanced feature and should not be necessary for models
    */
  trait Ops {

    extension (self:Application){
      def serviceGraph(using ev:Restrict[Map[Service, Set[Service]], Application]): Map[Service, Set[Service]] = ev(self)
      def hardwareGraph(using ev:Restrict[Map[Hardware, Set[Hardware]], Application]): Map[Hardware, Set[Hardware]] = ev(self)
    }

    /**
      * Extension method class
      *
      * @param self the element on which keyword can be used
      */
    extension (self: Platform) {

      /**
        * PML keyword to access to the service graph of an application
        *
        * @param s the application
        * @return its service graph
        */
      def serviceGraphOf(s: Application): Map[Service, Set[Service]] = {
        import self._
        val ev = implicitly[Restrict[Map[Service, Set[Service]], Application]]
        ev(s)
      }

      /**
        * PML keyword to access to the hardware graph of the considered element
        *
        * @return its hardware graph
        */
      def hardwareGraph(): Map[Hardware, Set[Hardware]] =
        self.applications
          .flatMap {
            self.hardwareGraphOf
          }
          .groupMapReduce(_._1)(_._2)(_ ++ _)

      /**
        * PML keyword to access to hardware graph used by an application
        *
        * @param s the application
        * @return its hardware graph
        */
      def hardwareGraphOf(s: Application): Map[Hardware, Set[Hardware]] = {
        import self._
        val ev = implicitly[Restrict[Map[Hardware, Set[Hardware]], Application]]
        ev(s)
      }

      /**
        * PML keyword to access to the service graph used by an application to access a target
        *
        * @param s   the application
        * @param tgt the target service
        * @return its service graph
        */
      def serviceGraphOf(s: Application, tgt: Service): Map[Service, Set[Service]] = {
        import self._
        val ev = implicitly[Restrict[Map[Service, Set[Service]], (Application, Service)]]
        ev((s, tgt))
      }

      /**
        * PML keyword to access to the hardware graph used by an application to access a target
        *
        * @param s   the application
        * @param tgt the target service
        * @return its service graph
        */
      def hardwareGraphOf(s: Application, tgt: Service): Map[Hardware, Set[Hardware]] = {
        import self._
        val ev = implicitly[Restrict[Map[Hardware, Set[Hardware]], (Application, Service)]]
        ev((s, tgt))
      }

    }

  }

  /** ------------------------------------------------------------------------------------------------------------------
    * INFERENCE RULES
    * --------------------------------------------------------------------------------------------------------------- */

  /**
    * A restricted can be obtained from an endomorphism over services
    *
    * @param lS  the proof that the endomorphism exists
    * @param uL  the proof that an application uses loads
    * @param uS  the proof that an application uses stores
    * @param uSI the proof that an application uses initiators
    * @param aR  the proof that an authorize relation exists
    * @param r   the proof that a routing relation exists
    * @param pB  the proof that an initiator provide services
    * @return an object building service graph for applications
    */
  given (using
         lS: LinkRelation[Service],
         uL: Used[Application, Load],
         uS: Used[Application, Store],
         uSI: Used[Application, Initiator],
         aR: AuthorizeRelation[Application, Service],
         r: RoutingRelation[(Initiator, Service, Service), Service],
         pB: Provided[Initiator, Service]): Restrict[Map[Service, Set[Service]], Application] with {


    /**
      * Check if the application uses some route between an initial service and a target service
      *
      * @param a    the application
      * @param from the initial service
      * @param to   the target service
      * @param u    the proof that application uses services of type U
      * @param lU   the proof that services of type U can be linked
      * @tparam U the type of the service
      * @return if the application uses some route between from and to
      */
    def useBySW[U <: Service](a: Application, from: U, to: U)(implicit u: Used[Application, U],
                                                              lU: Linked[U, U]): Boolean = {
      u(a).exists(tgt => aR(a).contains(tgt) &&
        a.hostingInitiators.exists(ini => usedForTgt(tgt, ini, from, to)))
    }

    def used(a: Application, from: Service, to: Service): Boolean = (from,to) match {
      case (fromL: Load, toL:Load) => useBySW(a, fromL, toL)
      case (fromS: Store, toS: Store) => useBySW(a, fromS, toS)
      case _ => false
    }

    /**
      * Provide the service graph of an application
      *
      * @param b the application
      * @return its service graph
      */
    def apply(b: Application): Map[Service, Set[Service]] = {
      lS.edges collect {
        case (from, linked) => from -> {
          linked filter {
            used(b, from, _)
          }
        }
      } filter {
        _._2.nonEmpty
      }
    }
  }

  given[T] (using
            lP: LinkRelation[Hardware],
            pS: Provided[Hardware, Service],
            restrict: Restrict[Map[Service, Set[Service]], T]): Restrict[Map[Hardware, Set[Hardware]], T] with {
    def apply(b: T): Map[Hardware, Set[Hardware]] = {
      val restricted = restrict(b)
      //shortcut non-owner services, if k -> v and k is not owned by any HW then push v to all predecessors of k
      val nonOwnedServices = restricted.keySet.collect { case b: Service if b.hardwareOwner.isEmpty => b }
      val shortcut = nonOwnedServices.foldLeft(restricted)((acc, toRemove) => {
        for {
          toAdd <- acc.get(toRemove)
          in = acc.keySet.filter(k => acc(k).contains(toRemove))
        } yield {
          acc.removed(toRemove).transform((k, v) => if (in.contains(k)) v ++ toAdd else v)
        }
      } getOrElse acc)
      val usedPLLinks = lP.edges.transform((k, v) =>
        v.filter(t =>
          shortcut.exists(ks =>
            k.services.contains(ks._1) && t.services.intersect(ks._2).nonEmpty
          )
        )
      ).filter {
        _._2.nonEmpty
      }
      val lostBasicEdges = shortcut.collect({ case (k: Service, v) => k -> v }).transform(
        (si, v) => v.filterNot(st =>
          usedPLLinks.exists(kvPL =>
            kvPL._1.services.contains(si) && kvPL._2.flatMap(_.services).contains(st)))).filter(_._2.nonEmpty)
        .toSeq.flatMap(kv => kv._2.flatMap(t => t.hardwareOwner.flatMap(tPL => kv._1.hardwareOwner.map(_ -> tPL))))
      //WARNING if a link si -> st exists where si and st have owner then a physical link is added
      val completedPLLinks = lostBasicEdges.foldLeft(usedPLLinks)((acc, toAdd) => {
        for {v <- acc.get(toAdd._1)} yield acc.updated(toAdd._1, v + toAdd._2)
      }.getOrElse(acc + (toAdd._1 -> Set(toAdd._2))))
      completedPLLinks
    }
  }

  given[T <: Application | Initiator] (using
         lS: LinkRelation[Service],
         uSI: Used[Application, Initiator],
         aR: AuthorizeRelation[Application, Service],
         r: RoutingRelation[(Initiator, Service, Service), Service],
         pB: Provided[Initiator, Service]): Restrict[Map[Service, Set[Service]], (T, Service)] with {

    def used(a: T, tgt: Service, from: Service, to: Service): Boolean = {
      val authorized = a match {
        case app: Application => aR(app).contains(tgt);
        case _ => true
      }
      val hostingInitiators = a match {
        case app:Application => app.hostingInitiators
        case i:Initiator => Set(i)
      }
      authorized && ((tgt,to,from) match {
        case (tgtL: Load,toL:Load,fromL:Load) =>
          hostingInitiators.exists(ini => usedForTgt(tgtL, ini, fromL, toL))
        case (tgtS: Store, toS:Store, fromS:Store) =>
          hostingInitiators.exists(ini => usedForTgt(tgtS, ini, fromS, toS))
        case _ => false
      })
    }


    def apply(b: (T, Service)): Map[Service, Set[Service]] = {
      lS.edges collect {
        case (from, linked) => from -> {
          linked filter {
            used(b._1, b._2, from, _)
          }
        }
      } filter {
        _._2.nonEmpty
      }
    }
  }
}
