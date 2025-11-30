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

import onera.pmlanalyzer.pml.model.hardware.{Platform, Target as PMLTarget}
import onera.pmlanalyzer.pml.model.hardware.Target as PMLTarget
import onera.pmlanalyzer.pml.model.hardware.Target as PMLTarget
import onera.pmlanalyzer.pml.model.software.Application as PMLApplication
import onera.pmlanalyzer.pml.model.software.Application as PMLApplication
import onera.pmlanalyzer.pml.model.software.Application as PMLApplication
import onera.pmlanalyzer.views.dependability.model.CustomTypes.TargetStatus

trait DependabilitySpecification {
  self: Platform =>

  type U

  implicit val toTargetId: PMLTarget => TargetId = mkTargetId

  val depSpecificationName: Symbol

  val failureConditions: Set[(PMLApplication, U, Int)]

  def mkTargetId(t: PMLTarget): TargetId

  def softwareStoresDependency(
      p: PMLApplication
  ): (Variable[U], Variable[TargetStatus[U]]) => Expr[TargetStatus[U]]

  def softwareState(
      p: PMLApplication
  ): (Variable[U], Variable[TargetStatus[U]]) => Expr[U]

  val targetIsInputDep: Set[PMLTarget]
}

object DependabilitySpecification {
  type Aux[T] = DependabilitySpecification {
    type U = T
  }
}
