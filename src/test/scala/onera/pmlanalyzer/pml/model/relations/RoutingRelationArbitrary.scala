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

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.{Provided, Used}
import org.scalacheck.{Arbitrary, Gen}

trait RoutingRelationArbitrary {
  self: Platform =>

  def toHardwareRouting(
      m: Map[(Initiator, Service, Service), Set[Service]]
  ): Map[(Initiator, Target, Hardware), Set[Hardware]] = {
    (
      for {
        ((ini, tgt, on), next) <- m.toSeq
        tgtH <- tgt.targetOwner
        onH <- on.hardwareOwner
        nextH = next.flatMap(_.hardwareOwner)
      } yield (ini, tgtH, onH) -> nextH
    ).groupMapReduce(_._1)(_._2)(_ ++ _)
  }
  def toServiceRouting(
      m: Map[(Initiator, Target, Hardware), Set[Hardware]]
  ): Map[(Initiator, Service, Service), Set[Service]] =
    (for {
      ((ini, to, on), next) <- m.toSeq
      toL <- to.loads
      onL <- on.loads
      nextL = next.loads
      toS <- to.stores
      onS <- on.stores
      nextS = next.stores
      (newTo, newOn, newNext) <- List((toL, onL, nextL), (toS, onS, nextS))
    } yield {
      (ini, newTo, newOn) -> newNext.toSet[Service]
    }).groupMapReduce(_._1)(_._2)(_ ++ _)

  /**
   * @note useLink override previous routing constraints, consider adding a warning if a routing constraint is overriden?
   * @param m the specification of the routing
   * @param undo add constraints if true, remove them if false
   */
  def applyAllRoute(
      m: Map[(Initiator, Target, Hardware), Set[Hardware]],
      undo: Boolean
  ): Unit = {
    if (undo) {
      val s = toServiceRouting(m)
      for { k <- s.keySet }
        context.InitiatorRouting.remove(k)
    } else {
      for {
        ((ini, tgt, on), next) <- m
      } {
        if (next.isEmpty)
          ini targeting tgt blockedBy on
        else if (next.size == 1)
          ini targeting tgt useLink on to next.head
        else {
          val doNotUse = on.linked -- next
          for { n <- doNotUse }
            ini targeting tgt cannotUseLink on to n
        }
      }
    }
  }

  given (using
      used: Used[Initiator, Service],
      pT: Provided[Target, Service],
      conf: ArbitraryConfiguration
  ): Arbitrary[Map[(Initiator, Target, Hardware), Set[Hardware]]] =
    Arbitrary(
      {
        for {
          electedValues <- Gen.someOf(
            for {
              i <- All[Initiator]
              if context.PLLinkableToPL.edges.contains(i)
              tS <- used(i)
              tH <- tS.targetOwner
              if context.PLLinkableToPL.edges.contains(
                tH
              ) || context.PLLinkableToPL.inverseEdges.contains(tH)
              lH <- Endomorphism.closure(
                i,
                context.PLLinkableToPL.edges
              ) - i - tH
              rH <- lH.linked.map(x => Some(x)) + None
            } yield (i, tH, lH) -> rH
          )
        } yield {
          val map =
            electedValues
              .groupMapReduce(_._1)((_, v) =>
                v match {
                  case Some(x) => Set(x)
                  case None    => Set.empty
                }
              )(_ ++ _)
              .filter((k, v) => k._3.linked.size > v.size)
          if (conf.showArbitraryInfo)
            println(
              s"[INFO] generated routing relation with ${map.values.map(_.size).sum} values"
            )
          if (map.size > conf.maxRoutingConstraint)
            map
              .drop(electedValues.size - conf.maxRoutingConstraint)
          else
            map
        }
      }
    )
}
