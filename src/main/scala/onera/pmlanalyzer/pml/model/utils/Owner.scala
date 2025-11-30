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

/** Utility class to track hierarchy in the model
  *
  * @param path
  *   the owner's name
  */
private[pmlanalyzer] final case class Owner private (path: List[Symbol]) {
  override def toString: String = path.map(_.name).mkString("_")
  def add(id: Symbol): Owner = Owner(path :+ id)
}

private[pmlanalyzer] object Owner {
  val empty: Owner = Owner(List.empty)

  def apply(s: Symbol): Owner = Owner(List(s))
}
