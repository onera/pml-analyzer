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

package onera.pmlanalyzer.views.dependability.operators

import onera.pmlanalyzer.allOf
import onera.pmlanalyzer.views.dependability.model.BaseEnumeration

trait IsCriticityOrdering[T] extends Ordering[T]

object IsCriticityOrdering {
  def apply[T](implicit ev: IsCriticityOrdering[T]): IsCriticityOrdering[T] = ev

  trait Ops {
    def min[T: IsCriticityOrdering: IsFinite]: T = allOf[T].min

    def max[T: IsCriticityOrdering: IsFinite]: T = allOf[T].max

    def worst[T: IsCriticityOrdering](l: T*): T = l.max

    def best[T: IsCriticityOrdering](l: T*): T = l.min

    implicit class CriticityOrderOps[T: IsCriticityOrdering](a: T)
        extends Ordered[T] {
      def compare(that: T): Int = IsCriticityOrdering[T].compare(a, that)
    }
  }

  given [T <: BaseEnumeration]: IsCriticityOrdering[T] with {
    def compare(x: T, y: T): Int = x.id - y.id
  }

}
