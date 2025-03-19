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
import sourcecode.{File, Line, Name}

import scala.collection.mutable
import scala.collection.mutable.{HashMap as MHashMap, Seq as MSeq, Set as MSet}

/** Relation between two finite sets Warning each one of the set contains the
  * empty set value thus R(a) = \emptyset not imply that a \notin R
  *
  * @param iniValues
  *   initial values of the relation
  * @tparam L
  *   type of the left set
  * @tparam R
  *   type of the right set
  */
abstract class Relation[L, R](iniValues: Map[L, Set[R]])(using n: Name) {

  private val modifications: mutable.ArrayBuffer[Change[L, R]] =
    mutable.ArrayBuffer.empty

  val name: String = n.value

  val _values: MHashMap[L, MSet[R]] = MHashMap(
    iniValues.map(p => p._1 -> MSet(p._2.toSeq: _*)).toSeq: _*
  )
  val _inverse: MHashMap[R, MSet[L]] =
    _values.flatMap(p => p._2.map(b => b -> MSet(p._1)))

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
  def edges: Map[L, Set[R]] = (_values.view mapValues {
    _.toSet
  }).toMap

  /** Add the element b to a
    *
    * @param a
    *   the input element
    * @param b
    *   the new element
    */
  def add(a: L, b: R)(using line: Line, file: File): Unit = {
    _values.getOrElseUpdate(a, MSet.empty[R]) += b
    _inverse.getOrElseUpdate(b, MSet.empty[L]) += a
    modifications += Change(a, b, isAdd = true, line, file)
  }

  /** Add a collection of b elements to a Warning if the b is empty then all the
    * element linked to a are removed (STRANGE)
    *
    * @param a
    *   the input element
    * @param b
    *   the collection of new elements
    */
  def add(a: L, b: Iterable[R])(using line: Line, file: File): Unit =
    if (b.nonEmpty)
      b.foreach(add(a, _))
    else
      _values.getOrElseUpdate(a, MSet.empty[R]).clear()

  /** Remove the element b from the relation with a
    *
    * @param a
    *   the input element
    * @param b
    *   the removed element
    */
  def remove(a: L, b: R)(using line: Line, file: File): Unit =
    for (sb <- _values.get(a); sa <- _inverse.get(b)) yield {
      sb -= b
      sa -= a
      modifications += Change(a, b, isAdd = false, line, file)
    }

  /** Remove the elements of the collection b from the relation with a
    *
    * @param a
    *   the input element
    * @param b
    *   the removed elements
    */
  def remove(a: L, b: Iterable[R])(using line: Line, file: File): Unit =
    b.foreach(remove(a, _))

  /** Remove a from the relation WARNING: this is different from removing all
    * elements in relation with a
    *
    * @param a
    *   the input element
    */
  def remove(a: L)(using line: Line, file: File): Unit =
    apply(a).foreach(remove(a, _))

  /** Provide the elements in relation with a WARNING the function returns an
    * empty set either if a is not in the relation or if no elements are
    * associated to a
    *
    * @param a
    *   the input element
    * @return
    *   the set of related elements
    */
  def apply(a: L): Set[R] = _values.getOrElse(a, Set.empty).toSet

  /** Provide the elements in relation with a id a is in the relation
    *
    * @param a
    *   the input element
    * @return
    *   the optional set of related elements
    */
  def get(a: L): Option[Set[R]] = for { b <- _values.get(a) } yield b.toSet

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
  def targetSet: Set[R] = _values.values.flatten.toSet

  /** Provide the set of all input elements
    *
    * @return
    *   the set of input elements
    */
  def domain: Set[L] = _values.keys.toSet

}

object Relation {

  final case class Change[L, R](
                                 l: L,
                                 r: R,
                                 isAdd: Boolean,
                                 line: Line,
                                 file: File
                               )(using name: Name)
    extends SourceCodeTraceable {

    /**
     * Line in source code where node has been instantiated
     */
    val lineInFile: Int = line.value

    /**
     * Source file in which node has been instantiated
     */
    val sourceFile: String =
      file.value.split('.').init.mkString(java.io.File.separator)

    override def toString: String = s"$sourceFile:$lineInFile ${
      if (isAdd) "adding" else "removing"
    } $l -> $r ${if (isAdd) "to" else "from"} ${name.value}"
  }

  /** Trait gathering all relation instances
    */
  trait Instances
      extends LinkRelation.Instances
      with UseRelation.Instances
      with ProvideRelation.Instances
      with AuthorizeRelation.Instances
      with RoutingRelation.Instances
}
