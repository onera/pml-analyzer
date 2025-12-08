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

private[pmlanalyzer] trait IsCriticalityOrdering[T] extends Ordering[T]

private[pmlanalyzer] object IsCriticalityOrdering {
  def apply[T](implicit
      ev: IsCriticalityOrdering[T]
  ): IsCriticalityOrdering[T] = ev

  trait Ops {
    def min[T: IsCriticalityOrdering: IsFinite]: T = allOf[T].min

    def max[T: IsCriticalityOrdering: IsFinite]: T = allOf[T].max

    def worst[T: IsCriticalityOrdering](l: T*): T = l.max

    def best[T: IsCriticalityOrdering](l: T*): T = l.min

    implicit class CriticityOrderOps[T: IsCriticalityOrdering](a: T)
        extends Ordered[T] {
      def compare(that: T): Int = IsCriticalityOrdering[T].compare(a, that)
    }
  }

  given [T <: BaseEnumeration]: IsCriticalityOrdering[T] with {
    def compare(x: T, y: T): Int = x.id - y.id
  }

}
