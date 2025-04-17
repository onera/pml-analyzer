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

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.LinkRelationArbitrary.removeNonReachableFrom
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}

trait LinkRelationArbitrary {
  self: Platform =>

  def linkToServiceMap(
      m: Map[Hardware, Set[Hardware]]
  ): Map[Service, Set[Service]] = {
    val r = for {
      (k, v) <- m.toSeq
      kL <- k.loads
      kS <- k.stores
      x <- v
      xL <- x.loads
      xS <- x.stores
      (nK, nV) <- List(kL -> xL, kS -> xS)
    } yield nK -> nV
    r.groupMap(_._1)(_._2).view.mapValues(_.toSet).toMap
  }

  def applyAllLinks(m: Map[Hardware, Set[Hardware]], undo: Boolean): Unit =
    for {
      (l, v) <- m
    } {
      l match
        case i: Initiator =>
          for { r <- v }
            r match
              case tr: Transporter =>
                if (!undo)
                  i link tr
                else
                  i unlink tr
              case tg: Target =>
                if (!undo)
                  i link tg
                else
                  i unlink tg
              case _ =>
        case tr: Transporter =>
          for { r <- v }
            r match
              case rTr: Transporter =>
                if (!undo)
                  tr link rTr
                else
                  tr unlink rTr
              case tg: Target =>
                if (!undo)
                  tr link tg
                else
                  tr unlink tg
              case _ =>
        case _ =>
    }

  given [S](using
      arb: Arbitrary[Map[Hardware, Set[Hardware]]],
      p: Provided[Hardware, S]
  ): Arbitrary[Map[S, Set[S]]] =
    Arbitrary(
      for {
        link <- arb.arbitrary
      } yield {
        for {
          (k, v) <- link
          newK <- k.provided
          newV = v.flatMap(_.provided)
        } yield newK -> newV
      }
    )

  given (using
      allI: All[Initiator],
      allTr: All[Transporter],
      allTg: All[Target],
      conf: ArbitraryConfiguration
  ): Arbitrary[Map[Hardware, Set[Hardware]]] = Arbitrary(
    if (allI().nonEmpty && allTr().nonEmpty && allTg().nonEmpty)
      for {
        map <- Gen.mapOf(
          Gen.zip(
            Gen.oneOf(allTr() ++ allI()),
            Gen
              .listOfN(
                List(conf.maxLinkPerComponent, (allTr() ++ allTg()).size).min,
                Gen.oneOf(allTr() ++ allTg())
              )
              .map(_.toSet)
          )
        )
      } yield {
        val r = map
          .transform((k, v) => v - k)
          .filter(_._2.nonEmpty)
        if (conf.removeUnreachableLink) {
          removeNonReachableFrom(allI().toSet[Hardware], r)
        } else
          r
      }
    else
      Map.empty
  )
}

object LinkRelationArbitrary {

  def removeNonReachableFrom[A](
      from: Set[A],
      in: Map[A, Set[A]]
  ): Map[A, Set[A]] = {
    val reachableFrom: Set[A] =
      for {
        f <- from
        r <- Endomorphism.closure(f, in)
      } yield r

    for {
      (k, v) <- in
      if reachableFrom.contains(k)
    } yield k -> v
  }
}
