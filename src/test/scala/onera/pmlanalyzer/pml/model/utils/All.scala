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

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeMap}

import scala.reflect.Typeable

trait All[T] {
  def apply(): Set[T]
}

object All {

  given [T <: PMLNode](using map: PMLNodeMap[T]): All[T] with {
    def apply(): Set[T] = map.map.values.toSet
  }

  given (using st: All[SimpleTransporter], v: All[Virtualizer]): All[
    Transporter
  ] with {
    def apply(): Set[Transporter] = All[SimpleTransporter] ++ All[Virtualizer]
  }

  given (using t: All[Transporter], i: All[Initiator], tg: All[Target]): All[
    Hardware
  ] with {
    def apply(): Set[Hardware] =
      All[Transporter] ++ All[Initiator] ++ All[Target]
  }

  given (using st: All[Application], v: All[Data]): All[Software] with {
    def apply(): Set[Software] = All[Application] ++ All[Data]
  }

  given (using st: All[Load], v: All[Store], a: All[ArtificialService]): All[
    Service
  ] with {
    def apply(): Set[Service] =
      All[Load] ++ All[Store] ++ All[ArtificialService]
  }

  def apply[T](using ev: All[T]): Set[T] = ev()
}
