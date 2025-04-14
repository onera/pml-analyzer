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

import onera.pmlanalyzer.pml.model.hardware.{Platform, Target}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.utils.All
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}

import scala.annotation.targetName

trait UseRelationArbitrary {
  self: Platform =>

  @targetName("given_KV_Use")
  given [K,V](using allK:All[K], allV:All[V], r:Use[K,V]) :Arbitrary[Map[K, Set[V]]] = Arbitrary(
      if(allK().nonEmpty && allV().nonEmpty)
        for {
          m <- Gen.mapOf(Gen.zip(
            Gen.oneOf(allK()),
            Gen.atLeastOne(allV())
              .map(_.toSet)
          ))
        } yield m
      else
        Map.empty
  )

  given [K] (using allI:All[K], allT:All[Target], r:Use[K,Service]): Arbitrary[Map[K, Set[Service]]] = Arbitrary({
    val targetService =
      for {
        t <- allT()
        s <- t.services
      } yield s

    if(targetService.nonEmpty && allI().nonEmpty)
      for {
        m <- Gen.mapOf(
          Gen.zip(
            Gen.oneOf(allI()),
            Gen.atLeastOne(targetService)
              .map(_.toSet)
          )
        )
      } yield m
    else
      Map.empty
  })
}
