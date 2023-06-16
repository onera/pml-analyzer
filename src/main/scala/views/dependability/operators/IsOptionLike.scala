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

trait IsOptionLike[T] {
  def isDefined(x:T):Boolean = x == none
  val none : T
  def toOption(x:T) : Option[T] = {
    if (x == none) {
      None
    }else {
      Some(x)
    }
  }
}

trait IsOptionLikeOps {
  implicit class IsOptionLikeOps[T] (x:T)(implicit ev:IsOptionLike[T]) {
    def isDefined: Boolean = ev.isDefined(x)
    val none : T = ev.none
    def toOption : Option[T] = ev.toOption(x)
  }

  def none[T](implicit ev:IsOptionLike[T]) : T = ev.none
}
