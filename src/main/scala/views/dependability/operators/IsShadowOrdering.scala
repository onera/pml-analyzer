/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package views.dependability.operators

trait IsShadowOrdering[T] {
  def containerShadow(init:T, containerState:T) : T
  def corruptingFM(fm:T) : Boolean
  def inputShadow(input:T, containerState : T) : T
}

trait IsShadowOrderingOps {
  implicit class IsShadowingOps[T : IsShadowOrdering](a:T) {
    def containerShadow(containerState:T) : T = IsShadowOrdering[T].containerShadow(a,containerState)
    def isCorruptingFM : Boolean = IsShadowOrdering[T].corruptingFM(a)
    def inputShadow(containerState : T) : T = IsShadowOrdering[T].inputShadow(a,containerState)
  }
}

object IsShadowOrdering{
  def apply[T](implicit ev: IsShadowOrdering[T]) : IsShadowOrdering[T] = ev
}