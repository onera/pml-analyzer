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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticalityOrdering,
  IsFinite
}

enum Direction(id: Int, name: String) extends BaseEnumeration(id, name) {

  case Degradation extends Direction(3, "degradation")
  case Reparation extends Direction(2, "reparation")
  case Constant extends Direction(1, "constant")
}

object Direction {
  given IsFinite[Direction] with {
    val none: Direction = Constant
    def allWithNone: Seq[Direction] = values.toSeq
    def name(x: Direction): Symbol = Symbol(x.toString)
  }

  given IsCriticalityOrdering[Direction] =
    (x: Direction, y: Direction) => x.id - y.id
}
