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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.operators._

object CustomTypes {

  type Request[T] = Map[(InitiatorId,TargetId),T]

  object Request {
    def empty[T] = Map.empty[(InitiatorId,TargetId),T]
    def all[T](fm:T) : Request[T] = Map(allOf[InitiatorId].flatMap(sID => allOf[TargetId].map{tId => (sID,tId)}).map(_ -> fm):_*)
    def apply[T](l:((InitiatorId,TargetId),T)*) : Request[T] = l.toMap
  }

  type TargetStatus[T] = Map[TargetId,T]

  object TargetStatus {
    def empty[T] = Map.empty[TargetId,T]
    def all[T](fm:T) : TargetStatus[T] = allOf[TargetId].map{_ -> fm}.toMap
    def apply[T](l:(TargetId,T)*) : TargetStatus[T] = l.toMap
  }

}
