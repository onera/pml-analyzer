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

package views.dependability.model

import views.dependability.operators.IsFinite

object Fire extends Enumeration {

  val Apply: Value = Value(3,"apply")
  val Wait: Value = Value(2, "wait")
  val No: Value = Value(1,"no")


  implicit val isFinite: IsFinite[Value] = new IsFinite[Value] {
    val none: Value = No
    def allWithNone: Seq[Value] = values.toSeq
    def name(x: Value): Symbol = Symbol(x.toString)
  }

}
