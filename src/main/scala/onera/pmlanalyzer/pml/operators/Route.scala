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
import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.RoutingRelation
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.utils.Message.uselessRoutingConstraintWarning
import onera.pmlanalyzer.pml.model.utils.{Context, Owner}
import sourcecode.{File, Line}

/** Extension methods
  */
private[pmlanalyzer] object Route {

  /** Any hardware can route or forbid the transactions passing through him. A
    * routing constraint can be specified as follows.
    *
    * If all transactions from an [[pml.model.hardware.Initiator]] (denoted
    * initiator) to a [[pml.model.hardware.Target]] (denoted target) are routed
    * by an [[pml.model.hardware.Hardware]] (denoted router) to one of its
    * successor (denoted next) then
    * {{{initiator targeting target useLink router to next}}} If the router
    * forbids a specific route then
    * {{{initiator targeting target cannotUseLink router to next}}} If the
    * router forbids all routes then
    * {{{initiator targeting target isBlockedBy router}}} If this constraint is
    * true for all target, then the targeting keyword can be omitted in any
    * previous construct {{{initiator useLink router to next}}}
    * @note
    *   the constraint is fruitful iff a service of the router is linked to a
    *   service of next
    * @see
    *   route usage can be found in
    *   [[pml.examples.simpleKeystone.SimpleRoutingConfiguration]]
    */
  trait Ops {

    extension (self: Initiator) {

      /** PML keyword to specify the target used by the initiator for which a
        * routing constraint applies
        * @param target
        *   a target
        * @return
        *   the partial routing constraint where the initiator target couple is
        *   specified
        */
      def targeting(target: Target): SimpleRouteIdentifyRouter =
        SimpleRouteIdentifyRouter(self, Seq(target))

      /** PML keyword to specify the set of targets used by the initiator for
        * which a routing constraint applies
        * @param target
        *   a set of targets
        * @return
        *   the partial routing constraint where the initiator target couples
        *   are specified
        */
      def targeting(target: Iterable[Target]): SimpleRouteIdentifyRouter =
        SimpleRouteIdentifyRouter(self, target)

      /** PML keyword to specify the hardware routing the transactions
        * @param router
        *   an hardware
        * @return
        *   a partial routing constraint where the initiator, the targets and
        *   the router are specified
        */
      def useLink(router: Hardware)(using
          context: Context,
          owner: Owner
      ): SimpleRouterIdentifyNext =
        SimpleRouterIdentifyNext(self, Target.all, router, forbid = false)

      /** PML keyword to specify the router blocking the route
        * @param router
        *   an hardware
        * @return
        *   a partial routing constraint where the initiator, the targets and
        *   the router are specified
        */
      def cannotUseLink(router: Hardware)(using
          context: Context,
          owner: Owner
      ): SimpleRouterIdentifyNext =
        SimpleRouterIdentifyNext(self, Target.all, router, forbid = true)

    }

    /** Partial routing constraint where the initiator target couples are
      * specified
      * @param a
      *   the initiator
      * @param targets
      *   the set of targets
      */
    final case class SimpleRouteIdentifyRouter(
        a: Initiator,
        targets: Iterable[Target]
    ) {

      /** PML keyword to specify the hardware routing the transactions
        * @param router
        *   an hardware
        * @return
        *   a partial routing constraint where the initiator, the targets and
        *   the router are specified
        */
      def useLink(router: Hardware): SimpleRouterIdentifyNext =
        SimpleRouterIdentifyNext(a, targets, router, forbid = false)

      /** PML keyword to specify the router blocking the route
        * @param router
        *   an hardware
        * @return
        *   a partial routing constraint where the initiator, the targets and
        *   the router are specified
        */
      def cannotUseLink(router: Hardware): SimpleRouterIdentifyNext =
        SimpleRouterIdentifyNext(a, targets, router, forbid = true)

      /** PML keyword to specify the router blocking all routes
        * @param router
        *   an hardware
        */
      def blockedBy(router: Hardware)(using
          p: Provided[Hardware, Service],
          l: Linked[Service, Service],
          r: RoutingRelation[(Initiator, Service, Service), Service],
          line: Line,
          file: File
      ): Unit = {
        for {
          t <- targets
          tL <- t.loads
          rL <- router.loads
        } yield {
          remove(tL, rL, l(rL))
        }

        for {
          t <- targets
          tS <- t.stores
          rS <- router.stores
        } yield {
          remove(tS, rS, l(rS))
        }
      }

      private def remove[T <: Service](t: T, on: T, next: Set[T])(using
          l: Linked[T, T],
          r: RoutingRelation[(Initiator, Service, Service), Service],
          line: Line,
          file: File
      ): Unit =
        r.get((a, t, on)) match {
          case Some(_) =>
            r.remove((a, t, on), next)
          case None =>
            r.add((a, t, on), l(on) -- next)
        }
    }

    /** Partial routing constraint where the initiator target couples, the
      * router and the type of routing constraint are specified
      * @param a
      *   the initiator
      * @param targets
      *   the set of targets
      * @param router
      *   the router
      * @param forbid
      *   if it is a blocking or routing constraint
      */
    final case class SimpleRouterIdentifyNext(
        a: Initiator,
        targets: Iterable[Target],
        router: Hardware,
        forbid: Boolean
    ) {

      /** PML keyword to specify the link from [[router]] that is routed or
        * blocked
        * @param next
        *   the component linked to [[router]]
        * @param p
        *   the proof that hardware provide services
        * @param l
        *   the proof that services can be linked
        * @param r
        *   the proof that a routing relation exists
        */
      def to(next: Hardware)(using
          p: Provided[Hardware, Service],
          l: Linked[Service, Service],
          r: RoutingRelation[(Initiator, Service, Service), Service],
          line: Line,
          file: File
      ): Unit = {
        if (
          !next.services
            .exists(s => router.services.exists(s2 => l(s2).contains(s)))
        )
          println(uselessRoutingConstraintWarning(router, next))
        else {
          for {
            t <- targets
            tL <- t.loads
            rL <- router.loads
          } yield {
            update(tL, rL, next.loads)
          }

          for {
            t <- targets
            tS <- t.stores
            rS <- router.stores
          } yield {
            update(tS, rS, next.stores)
          }
        }
      }

      private def update[T <: Service](t: T, on: T, next: Set[T])(using
          l: Linked[T, T],
          r: RoutingRelation[(Initiator, Service, Service), Service],
          line: Line,
          file: File
      ): Unit = {
        r.get((a, t, on)) match {
          case Some(_) if forbid =>
            r.remove((a, t, on), next)
          case None if forbid =>
            r.add((a, t, on), l(on) -- next)
          case _ =>
            r.remove((a, t, on))
            r.add((a, t, on), next)
        }
      }
    }
  }
}
