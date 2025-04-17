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

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.Used
import org.scalacheck.{Arbitrary, Gen}

trait RoutingRelationArbitrary {
  self: Platform =>

  def toServiceRouting(
      m: Map[(Initiator, Target, Hardware), Hardware]
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
    }).toMap

  def applyAllRoute(
      m: Map[(Initiator, Target, Hardware), Hardware],
      undo: Boolean
  ): Unit = {
    for {
      ((ini, tgt, on), next) <- m
    } {
      if (!undo)
        ini targeting tgt useLink on to next
      else {
        for {
          sTgt <- tgt.services
          sOn <- on.services
        }
          context.InitiatorRouting.remove((ini, sTgt, sOn))
      }
    }
  }

  given (using
      allI: All[Initiator],
      used: Used[Initiator, Service],
      pT: Provided[Target, Service],
      conf: ArbitraryConfiguration
  ): Arbitrary[Map[(Initiator, Target, Hardware), Hardware]] =
    Arbitrary(
      {
        for {
          electedValues <- Gen.someOf(
            for {
              i <- allI()
              if context.PLLinkableToPL.edges.contains(i)
              tS <- used(i)
              tH <- tS.targetOwner
              if context.PLLinkableToPL.edges.contains(
                tH
              ) || context.PLLinkableToPL.inverseEdges.contains(tH)
              lH <- Endomorphism.closure(i, context.PLLinkableToPL.edges)
              rH <- context.PLLinkableToPL(lH)
            } yield (i, tH, lH) -> rH
          )
        } yield {
          if (electedValues.size > conf.maxRoutingConstraint)
            electedValues
              .drop(electedValues.size - conf.maxRoutingConstraint)
              .toMap
          else
            electedValues.toMap
        }
      }
    )
}
