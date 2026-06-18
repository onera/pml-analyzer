/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.pml.model.SourceCodeTraceable
import onera.pmlanalyzer.pml.model.relations.Relation.Change
import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import sourcecode.{File, Line, Name}

import scala.collection.mutable

/** Function between two finite sets Warning each one of the set contains the
  * empty set value thus R(a) = \emptyset not imply that a \notin R
  *
  * @param iniValues
  *   initial values of the relation
  * @tparam L
  *   type of the left set
  * @tparam R
  *   type of the right set
  */
abstract class Function[L, R](iniValues: Map[L, R])(using n: Name) {

  protected val modifications: mutable.ArrayBuffer[Change[L, R]] =
    mutable.ArrayBuffer.empty

  val name: String = n.value

  val _values: mutable.HashMap[L, R] = mutable.HashMap(
    iniValues.toSeq: _*
  )
  val _inverse: mutable.HashMap[R, mutable.Set[L]] =
    mutable.HashMap(
      _values.groupMapReduce(_._2)((k, _) => mutable.Set(k))(_ ++ _).toSeq: _*
    )

  /**
   * Get all modifications linked to a given edge
   *
   * @param l the left element
   * @param r the right element
   * @return all modification in order of execution
   */
  def getModificationsFor(l: L, r: R): Seq[Change[L, R]] =
    modifications.filter(c => c.l == l && c.r == r).toSeq

  /** Provide the relation a map of edges
    *
    * @return
    *   the map of edges
    */
  def edges: Map[L, R] = _values.toMap

  /** Add the element b to a
    *
    * @param a
    *   the input element
    * @param b
    *   the new element
    */
  def add(a: L, b: R)(using line: Line, file: File): Unit = {
    _values(a) = b
    _inverse.getOrElseUpdate(b, mutable.Set.empty[L]) += a
    modifications += Change(a, Some(b), isAdd = true, line, file)
  }

  /** Remove a from the relation WARNING: this is different from removing all
    * elements in relation with a
    *
    * @param a
    *   the input element
    */
  def remove(a: L)(using line: Line, file: File): Unit = {
    for {
      sa <- _inverse.values
      if sa.contains(a)
    } {
      sa -= a
    }
    modifications += Change(a, Some(_values(a)), isAdd = false, line, file)
    _values.remove(a)
  }

  /** Provide the elements in relation with a WARNING the function returns an
    * empty set either if a is not in the relation or if no elements are
    * associated to a
    *
    * @param a
    *   the input element
    * @return
    *   the set of related elements
    */
  def apply(a: L): R = _values(a)

  /** Provide the elements in relation with a id a is in the relation
    *
    * @param a
    *   the input element
    * @return
    *   the optional set of related elements
    */
  def get(a: L): Option[R] = for { b <- _values.get(a) } yield b

  /** Provide the elements a in relation with b
    *
    * @param b
    *   the output element
    * @return
    *   the set of related inputs
    */
  def inverse(b: R): Set[L] = _inverse.getOrElse(b, Set.empty).toSet

  /** Provide the set of all output elements
    *
    * @return
    *   the set of output elements
    */
  def targetSet: Set[R] = _values.values.toSet

  /** Provide the set of all input elements
    *
    * @return
    *   the set of input elements
    */
  def domain: Set[L] = _values.keys.toSet

}
