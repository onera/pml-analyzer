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

import scalaz.Memo.immutableHashMapMemo
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.LinkRelationArbitrary.removeNonReachableFrom
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import org.scalacheck.{Arbitrary, Gen}
import onera.pmlanalyzer.pml.operators.*

trait LinkRelationArbitrary {
  self: Platform =>

  def toServiceMap(
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

  def applyAll(m: Map[Hardware, Set[Hardware]], link: Boolean): Unit =
    for {
      (l, v) <- m
    } {
      l match
        case i: Initiator =>
          for { r <- v }
            r match
              case tr: Transporter =>
                if (link)
                  i link tr
                else
                  i unlink tr
              case tg: Target =>
                if (link)
                  i link tg
                else
                  i unlink tg
              case _ =>
        case tr: Transporter =>
          for { r <- v }
            r match
              case rTr: Transporter =>
                if (link)
                  tr link rTr
                else
                  tr unlink rTr
              case tg: Target =>
                if (link)
                  tr link tg
                else
                  tr unlink tg
              case _ =>
        case _ =>
    }

  given (using
      allI: All[Initiator],
      allTr: All[Transporter],
      allTg: All[Target],
      conf: ArbitraryConfiguration
  ): Arbitrary[Map[Hardware, Set[Hardware]]] = Arbitrary(
    if (allI().nonEmpty && allTr().nonEmpty && allTg().nonEmpty)
      for {
        iSet <- Gen
          .listOfN(
            List(conf.maxInitiatorSetLink, allI().size).min,
            Gen.oneOf(allI())
          )
          .map(_.toSet)
          .suchThat(_.nonEmpty)
        trSet <- Gen
          .listOfN(
            List(conf.maxTransporterSetLink, allTr().size).min,
            Gen.oneOf(allTr())
          )
          .map(_.toSet)
          .suchThat(_.nonEmpty)
        tgSet <- Gen
          .listOfN(
            List(conf.maxTargetSetLink, allTg().size).min,
            Gen.oneOf(allTg())
          )
          .map(_.toSet)
          .suchThat(_.nonEmpty)
        map <- Gen.mapOf(
          Gen.zip(
            Gen.oneOf(trSet ++ iSet),
            Gen
              .atLeastOne(trSet ++ tgSet)
              .map(_.toSet)
          )
        )
      } yield {
        val r = map
          .transform((k, v) => v - k)
          .filter(_._2.nonEmpty)
        if(conf.removeUnreachableLink) {
          removeNonReachableFrom(allI().toSet[Hardware],r)
        } else
          r
      }
    else
      Map.empty
  )
}

object LinkRelationArbitrary {

  def closure[A](a: A, m: Map[A, Set[A]]): Set[A] = {
    lazy val rec: ((A, Set[A])) => Set[A] = immutableHashMapMemo { s =>
      if (s._2.contains(s._1))
        Set(s._1)
      else
        m.getOrElse(s._1, Set.empty).flatMap(rec(_, s._2 + s._1)) + s._1
    }
    rec(a, Set.empty)
  }

  def removeNonReachableFrom[A](from: Set[A], in: Map[A, Set[A]]): Map[A, Set[A]] = {
    val reachableFrom: Set[A] =
      for {
        f <- from
        r <- closure(f, in)
      } yield
        r

    for {
      (k, v) <- in
      if reachableFrom.contains(k)
    } yield
      k -> v
  }
}
