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

import scala.language.higherKinds

trait IsMergeable[C[_,_]] {
  def mergeWith[K,V](first:C[K,V],that:C[K,V], f:(V,V) => V) : C[K,V]
}

trait IsMergeableOps {
  implicit class IsMergeableOps[K,V,C[_,_]](a:C[K,V]){
    def mergeWith(that:C[K,V], f:(V,V) => V)(implicit ev:IsMergeable[C]) : C[K,V] = {
      ev.mergeWith(a,that,f)
    }
  }
}

object IsMergeable {

  def apply[C[_,_]](implicit ev:IsMergeable[C]) : IsMergeable[C] = ev

  implicit def mapIsMergeable:IsMergeable[Map] = {
    new IsMergeable[Map] {
      def mergeWith[K, V](first: Map[K, V], that: Map[K, V], f: (V, V) => V): Map[K, V] = {
        first.foldLeft(that)((acc,kv) =>
          if (acc.contains(kv._1)) acc + (kv._1 -> f(acc(kv._1), kv._2)) else acc + kv
        )
      }
    }
  }
}