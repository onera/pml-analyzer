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

package onera.pmlanalyzer.pml.model.utils

import org.scalacheck.Gen

import scala.collection.immutable.{AbstractSet, SortedSet}

object GenExtension {

  extension (x: Gen.type) {
    def mapForAllK[K, V](keys: Set[K], gen: => Gen[V]): Gen[Map[K, V]] =
      if (keys.isEmpty)
        Map.empty
      else
        for {
          kv <- gen.map(keys.head -> _)
          m <- mapForAllK(keys - kv._1, gen)
        } yield m + kv

    def nonEmptySubSetOfN[V](n: Int, s: Set[V]): Gen[Set[V]] = {
      assert(s.size >= n)
      for {
        size <- x.choose(1, n)
        r <- x.pick(size, s)
      } yield r.toSet
    }
  }
}
