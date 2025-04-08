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

package onera.pmlanalyzer.views.interference.model.formalisation.Petri

import scala.collection.mutable
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place
import math.Ordering.Implicits.infixOrderingOps
import math.Ordered.orderingToOrdered

object Marking {
  type Marking = mutable.Map[Place, Int]

  val empty: Marking = mutable.Map.empty

  def apply(elem:(Place,Int)*):Marking =
    mutable.Map(elem:_*)

  extension (x: Marking) {
    def >=(y: Marking): Boolean =
      y.keySet.subsetOf(x.keySet) &&
        y.keySet.forall(k => x(k) >= y(k))
  }
}