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

package onera.pmlanalyzer.pml.model

import onera.pmlanalyzer.pml.model.hardware.Composite
import onera.pmlanalyzer.pml.model.utils.{Message, Owner}

import scala.collection.mutable.HashMap as MHashMap

/** Trait for pml node builder (usually companion object) that must adopt an
  * h-consign like object handling
  * @tparam T
  *   the concrete type of built object
  */
private[pmlanalyzer] trait PMLNodeBuilder[T <: PMLNode] private[pml] {

  def get(name: Symbol)(using _memo: PMLNodeMap[T]): Option[T] =
    _memo.map.get(name)

  def add(v: T)(using _memo: PMLNodeMap[T]): Unit = {
    for { l <- _memo.map.get(v.name) } {
      println(
        Message.errorMultipleInstantiation(
          s"$l in ${l.sourceFile} at line ${l.lineInFile}",
          s"${v.sourceFile} at line ${v.lineInFile}"
        )
      )
    }
    _memo.map.addOne((v.name, v))
  }

  def getOrElseUpdate(name: Symbol, v: => T)(using _memo: PMLNodeMap[T]): T = {
    for { l <- _memo.map.get(name) } {
      println(
        Message.errorMultipleInstantiation(
          s"$l in ${l.sourceFile} at line ${l.lineInFile}",
          s"${v.sourceFile} at line ${v.lineInFile}"
        )
      )
    }
    _memo.map.getOrElseUpdate(name, v)
  }

  /** Provide all the object of the current type created for the platform,
    * including the ones created in composite components
    * @group embedFunctions
    * @param owner
    *   the name of the platform owning the objects
    * @return
    *   set of created objects
    */
  def all(implicit
      owner: Owner,
      _memo: PMLNodeMap[T],
      memoC: PMLNodeMap[Composite]
  ): Set[T] = {
    allDirect ++ Composite.allDirect.flatMap(c => all(c.currentOwner))
  }

  /** Provide all the object of the current type created for the platform,
    * without the ones created in composite components
    * @group embedFunctions
    * @param owner
    *   the name of the platform owning the objects
    * @return
    *   set of created objects
    */
  def allDirect(implicit owner: Owner, _memo: PMLNodeMap[T]): Set[T] =
    for {
      v <- _memo.map.values.toSet
      if v.owner == owner
    } yield v
}

private[pmlanalyzer] object PMLNodeBuilder {

  /** Formatting of object name
   *
   * @param name
   * the name of the object
   * @param owner
   * the name of its owner
   * @return
   * the formatted name
   * @note
   * this method should not be used in models
   * @group utilFun
   */
  final def formatName(name: Symbol, owner: Owner): Symbol = Symbol(
    s"${owner}_${name.name}"
  )

}
