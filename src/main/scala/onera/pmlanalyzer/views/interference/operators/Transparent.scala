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

package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.views.interference.model.relations.TransparentSet
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalAtomicTransactionId
import sourcecode.{File, Line}

private[operators] trait Transparent[T] {
  def apply(x: T)(using line: Line, file: File): Unit
}

object Transparent {

  /** If an element x of type T is transparent then the operator can be used as
    * follows {{{x.isTransparent}}}
    */
  trait Ops {
    extension [T](x: T) {

      /** The element x is discarded for interference analysis
        * @param ev
        *   proof that T can be discarded
        */
      def isTransparent(using
          ev: Transparent[T],
          line: Line,
          file: File
      ): Unit = ev(x)
    }
  }

  given [T](using ev: TransparentSet[T]): Transparent[T] with {
    def apply(x: T)(using line: Line, file: File): Unit = ev.value += x
  }

  given [U](using
      transformation: Transform[U, Option[PhysicalAtomicTransactionId]],
      ev: Transparent[PhysicalAtomicTransactionId]
  ): Transparent[U] with {
    def apply(x: U)(using line: Line, file: File): Unit = for {
      id <- transformation(x)
    } ev(id)
  }

  given [V](using
      transformation: Transform[V, Set[PhysicalAtomicTransactionId]],
      ev: Transparent[PhysicalAtomicTransactionId]
  ): Transparent[V] with {
    def apply(x: V)(using line: Line, file: File): Unit = for {
      id <- transformation(x)
    } ev(id)
  }

}
