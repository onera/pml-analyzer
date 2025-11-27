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

import onera.pmlanalyzer.pml.model.hardware.{
  Hardware,
  Initiator,
  Platform,
  Target
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.GenExtension.*
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}

import scala.annotation.targetName

trait UseRelationArbitrary {
  self: Platform =>

  def applyAllUses[K, V](use: Map[K, Set[V]], undo: Boolean)(using
      u: Use[K, V],
      r: UseRelation[K, V]
  ): Unit = {
    for {
      (i, ss) <- use
      s <- ss
    } {
      if (!undo)
        u(i, s)
      else
        r.remove(i, s)
    }
  }

  @targetName("given_direct_use")
  given [K, V <: Hardware](using
      allK: All[K],
      allV: All[V],
      conf: ArbitraryConfiguration,
      r: Use[K, V]
  ): Arbitrary[Map[K, Set[V]]] = Arbitrary(
    if (All[K].nonEmpty && All[V].nonEmpty)
      if (conf.forceTotalHosting) {
        Gen.mapForAllK(
          All[K],
          Gen.oneOf(All[V]).map(x => Set(x))
        )
      } else
        for {
          m <- Gen.mapOf(
            Gen.zip(
              Gen.oneOf(All[K]),
              Gen
                .atLeastOne(All[V])
                .map(_.toSet)
            )
          )
        } yield {
          if (conf.showArbitraryInfo)
            println(
              s"[INFO] generated use relation with ${m.values.map(_.size).sum} values"
            )
          m
        }
    else
      Map.empty
  )

  given [K](using
      allI: All[K],
      conf: ArbitraryConfiguration,
      r: Use[K, Service]
  ): Arbitrary[Map[K, Set[Service]]] = Arbitrary({
    val targetService =
      for {
        t <- All[Target]
        s <- t.services
      } yield s

    if (targetService.nonEmpty && All[K].nonEmpty)
      if (conf.forceTotalHosting)
        Gen.mapForAllK(
          All[K],
          Gen.oneOf(targetService).map(x => Set(x))
        )
      else
        for {
          m <- Gen.mapOf(
            Gen.zip(
              Gen.oneOf(All[K]),
              Gen
                .atLeastOne(targetService)
                .map(_.toSet)
            )
          )
        } yield {
          if (conf.showArbitraryInfo)
            println(
              s"[INFO] generated use relation with ${m.values.map(_.size).sum} values"
            )
          m
        }
    else
      Map.empty
  })
}
