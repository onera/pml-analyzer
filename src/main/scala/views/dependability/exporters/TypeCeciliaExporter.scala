/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package views.dependability.exporters

import views.dependability.operators.{IsFinite, _}

trait TypeCeciliaExporter {
  def typeModel[T: IsFinite]: EnumeratedType = {
    EnumeratedType(nameOf[T], allWithNone.map(fm => fm.name).toList, PhylogFolder.phylogFMTypeFolder)
  }

  def typeModel[K: IsFinite, V: IsFinite]: RecordType = {
    val tyypeMode = typeModel[V]
    RecordType(Symbol(s"Map${nameOf[K].name}To${nameOf[V].name}"), PhylogFolder.phylogCustomTypeFolder, allOf[K].map(t => Flow(t.name, tyypeMode, In)).toList)
  }

  def boolMapTypeModel[K: IsFinite]: RecordType = {
    RecordType(Symbol(s"Map${nameOf[K].name}ToBool"), PhylogFolder.phylogCustomTypeFolder, allOf[K].map(t => Flow(t.name, CeciliaBoolean, In)).toList)
  }
}
