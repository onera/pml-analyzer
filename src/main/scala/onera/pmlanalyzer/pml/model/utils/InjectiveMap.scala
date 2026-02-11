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

package onera.pmlanalyzer.pml.model.utils

private[pmlanalyzer] final case class InjectiveMap[K, V] private (
    inner: Map[K, V]
) extends Map[K, V] {
  def inverse(): InjectiveMap[V, K] = InjectiveMap(inner.map(_.swap))

  def removed(key: K): InjectiveMap[K, V] = InjectiveMap(inner.removed(key))

  def updated[V1 >: V](key: K, value: V1): InjectiveMap[K, V1] = {
    require(
      inner.forall((k, v) => v != value || k == key),
      "[ERROR] operation breaks injective property"
    )
    InjectiveMap(inner.updated(key, value))
  }

  def get(key: K): Option[V] = inner.get(key)

  def iterator: Iterator[(K, V)] = inner.iterator
}

private[pmlanalyzer] object InjectiveMap {

  def empty[K, V]: InjectiveMap[K, V] = InjectiveMap(Map.empty[K, V])

  def apply[K, V](pairs: Iterable[(K, V)]): InjectiveMap[K, V] = {
    val p = pairs.toSeq
    val nonInjective =
      pairs
        .groupBy(_._2)
        .transform((_, v) => v.map(_._1))
        .filter(_._2.toSet.size >= 2)
        .map((k, v) => s"[ERROR] keys:${v.mkString(",")} -> value: $v")
    require(
      nonInjective.isEmpty,
      s"[ERROR] Map is not injective since in map:\n${nonInjective.mkString("\n")}"
    )
    InjectiveMap(Map(pairs.toSeq: _*))
  }

  def apply[K, V](pairs: (K, V)*): InjectiveMap[K, V] =
    apply(pairs)
}
