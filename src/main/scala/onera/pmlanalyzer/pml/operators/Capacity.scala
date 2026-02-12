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

import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.pml.model.relations.CapacityRelation

import sourcecode.{File, Line}

private[operators] trait Capacity[-L, -R] {
  def apply(l: L, r: R)(using line: Line, file: File): Unit
}

private[pmlanalyzer] object Capacity {

  /** If an element l of type T can be exclusive with another element r of type
   * T, the following operator can be used {{{l exclusiveWith r}}}
   */
  trait Ops {
    extension [L](l: L) {
      def hasCapacity[R](
          r: R
      )(using d: Capacity[L, R], line: Line, file: File): Unit =
        d(l, r)
    }
  }

  given [LH <: Hardware, R](using
      c: Capacity[Service, R],
      pr: Provided[LH, Service]
  ): Capacity[LH, R] with {
    def apply(l: LH, r: R)(using line: Line, file: File): Unit =
      for {
        s <- l.services
      } yield s hasCapacity r
  }

  /**
   * We can generate a proof that a capacity of type R is assignable to a type L
   * If we can find a relation containing syper types of L and R
   */
  given [L, R](using
      dr: CapacityRelation[L, R]
  ): Capacity[L, R] with {
    def apply(l: L, r: R)(using line: Line, file: File): Unit = dr.add(l, r)
  }
}
