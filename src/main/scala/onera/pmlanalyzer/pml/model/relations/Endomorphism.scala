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

import scalaz.Memo.immutableHashMapMemo
import sourcecode.{File, Line, Name}

/** Refinement for endomorphisms (relation on the same type A)
  *
  * @param iniValues
  *   initial values of the relation
  * @tparam A
  *   the elements type
  */
abstract class Endomorphism[A](iniValues: Map[A, Set[A]])(using n: Name)
    extends Relation[A, A](iniValues: Map[A, Set[A]]) {

  /** Remove an element from both the input and output set
    *
    * @param a
    *   the element to remove
    */
  override def remove(a: A)(using line: Line, file: File): Unit = {
    super.remove(a)
    _inverse.remove(a)
  }

  /** Provide the reflexive and transitive closure of a by the endomorphism
    *
    * @param a
    *   the input element
    * @return
    *   the set of all elements indirectly related to a
    */
  def closure(a: A): Set[A] = {
    lazy val rec: ((A, Set[A])) => Set[A] = immutableHashMapMemo { s =>
      if (s._2.contains(s._1))
        Set(s._1)
      else
        apply(s._1).flatMap(rec(_, s._2 + s._1)) + s._1
    }
    rec(a, Set.empty)
  }

  /** Provide the reflexive and transitive inverse closure of a by the
    * endomorphism
    *
    * @param a
    *   the input element
    * @return
    *   the set of all elements that indirectly relate to a
    */
  def inverseClosure(a: A): Set[A] = {
    lazy val rec: ((A, Set[A])) => Set[A] = immutableHashMapMemo { s =>
      if (s._2.contains(s._1))
        Set(s._1)
      else
        inverse(s._1).flatMap(rec(_, s._2 + s._1)) + s._1
    }
    rec(a, Set.empty)
  }
}
