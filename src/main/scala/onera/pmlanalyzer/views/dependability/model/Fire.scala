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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.operators.IsFinite

enum Fire(i: Int, name: String) extends BaseEnumeration(i, name) {
  case Apply extends Fire(3, "apply")
  case Wait extends Fire(2, "wait")
  case No extends Fire(1, "no")
}
object Fire {

  implicit val isFinite: IsFinite[Fire] = new IsFinite[Fire] {
    val none: Fire = No
    def allWithNone: Seq[Fire] = Fire.values.toSeq
    def name(x: Fire): Symbol = Symbol(x.toString)
  }

}
