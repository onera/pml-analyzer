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

package onera

/**
 * The following package contains all types that
 * an user can import to build its model
 */
package object pmlanalyzer
    extends pml.exporters.All
    with pml.operators.All
    with views.dependability.exporters.All
    with views.dependability.operators.All
    with views.interference.exporters.All
    with views.interference.operators.All
    with views.patterns.exporters.All {

  val PMLNodeBuilder: onera.pmlanalyzer.pml.model.PMLNodeBuilder.type =
    onera.pmlanalyzer.pml.model.PMLNodeBuilder

  type Hardware = onera.pmlanalyzer.pml.model.hardware.Hardware
  type Target = onera.pmlanalyzer.pml.model.hardware.Target
  val Target: onera.pmlanalyzer.pml.model.hardware.Target.type =
    onera.pmlanalyzer.pml.model.hardware.Target
  type Initiator = onera.pmlanalyzer.pml.model.hardware.Initiator
  val Initiator: onera.pmlanalyzer.pml.model.hardware.Initiator.type =
    onera.pmlanalyzer.pml.model.hardware.Initiator
  type SimpleTransporter =
    onera.pmlanalyzer.pml.model.hardware.SimpleTransporter
  val SimpleTransporter
      : onera.pmlanalyzer.pml.model.hardware.SimpleTransporter.type =
    onera.pmlanalyzer.pml.model.hardware.SimpleTransporter
  type Virtualizer = onera.pmlanalyzer.pml.model.hardware.Virtualizer
  val Virtualizer: onera.pmlanalyzer.pml.model.hardware.Virtualizer.type =
    onera.pmlanalyzer.pml.model.hardware.Virtualizer
  type Composite = onera.pmlanalyzer.pml.model.hardware.Composite
  val Composite: onera.pmlanalyzer.pml.model.hardware.Composite.type =
    onera.pmlanalyzer.pml.model.hardware.Composite
  type Platform = onera.pmlanalyzer.pml.model.hardware.Platform
  val Platform: onera.pmlanalyzer.pml.model.hardware.Platform.type =
    onera.pmlanalyzer.pml.model.hardware.Platform

  type Transaction = onera.pmlanalyzer.pml.model.configuration.Transaction
  val Transaction: onera.pmlanalyzer.pml.model.configuration.Transaction.type =
    onera.pmlanalyzer.pml.model.configuration.Transaction
  type UsedTransaction =
    onera.pmlanalyzer.pml.model.configuration.UsedTransaction
  val UsedTransaction
      : onera.pmlanalyzer.pml.model.configuration.UsedTransaction.type =
    onera.pmlanalyzer.pml.model.configuration.UsedTransaction
  type TransactionLibrary =
    onera.pmlanalyzer.pml.model.configuration.TransactionLibrary

  type Service = onera.pmlanalyzer.pml.model.service.Service
  type Load = onera.pmlanalyzer.pml.model.service.Load
  val Load: onera.pmlanalyzer.pml.model.service.Load.type =
    onera.pmlanalyzer.pml.model.service.Load
  type Store = onera.pmlanalyzer.pml.model.service.Store
  val Store: onera.pmlanalyzer.pml.model.service.Store.type =
    onera.pmlanalyzer.pml.model.service.Store
  type ArtificialService = onera.pmlanalyzer.pml.model.service.ArtificialService
  val ArtificialService
      : onera.pmlanalyzer.pml.model.service.ArtificialService.type =
    onera.pmlanalyzer.pml.model.service.ArtificialService

  type Application = onera.pmlanalyzer.pml.model.software.Application
  val Application: onera.pmlanalyzer.pml.model.software.Application.type =
    onera.pmlanalyzer.pml.model.software.Application
  type Data = onera.pmlanalyzer.pml.model.software.Data
  val Data: onera.pmlanalyzer.pml.model.software.Data.type =
    onera.pmlanalyzer.pml.model.software.Data

  type Context = onera.pmlanalyzer.pml.model.utils.Context
  type ReflexiveInfo = onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
  val FileManager: onera.pmlanalyzer.pml.exporters.FileManager.type =
    onera.pmlanalyzer.pml.exporters.FileManager

  type InterferenceSpecification =
    onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
  val InterferenceSpecification
      : onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.type =
    onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
  type TableBasedInterferenceSpecification =
    onera.pmlanalyzer.views.interference.model.specification.TableBasedInterferenceSpecification
  type PhysicalTableBasedInterferenceSpecification =
    onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
  type ApplicativeTableBasedInterferenceSpecification =
    onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification

  type Method =
    onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
  val Method
      : onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.type =
    onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
  type SolverImplm =
    onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
  val SolverImplm
      : onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.type =
    onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm

  type BaseEnumeration =
    onera.pmlanalyzer.views.dependability.model.BaseEnumeration
  type IsFinite[T] = onera.pmlanalyzer.views.dependability.operators.IsFinite[T]
  type IsShadowOrdering[T] =
    onera.pmlanalyzer.views.dependability.operators.IsShadowOrdering[T]
  type IsCriticalityOrdering[T] =
    onera.pmlanalyzer.views.dependability.operators.IsCriticalityOrdering[T]
  type DependabilitySpecification =
    onera.pmlanalyzer.views.dependability.model.DependabilitySpecification
  type Request[T] =
    onera.pmlanalyzer.views.dependability.model.CustomTypes.Request[T]
  type TargetStatus[T] =
    onera.pmlanalyzer.views.dependability.model.CustomTypes.TargetStatus[T]
  type Expr[T] = onera.pmlanalyzer.views.dependability.model.Expr[T]
  type TargetId = onera.pmlanalyzer.views.dependability.model.TargetId
  val TargetId: onera.pmlanalyzer.views.dependability.model.TargetId.type =
    onera.pmlanalyzer.views.dependability.model.TargetId
  type Variable[T] = onera.pmlanalyzer.views.dependability.model.Variable[T]
  type Worst[T] = onera.pmlanalyzer.views.dependability.model.Worst[T]
  val Worst: onera.pmlanalyzer.views.dependability.model.Worst.type =
    onera.pmlanalyzer.views.dependability.model.Worst

  type Backing = onera.pmlanalyzer.views.patterns.model.Backing
  type Claim = onera.pmlanalyzer.views.patterns.model.Claim
  type Given = onera.pmlanalyzer.views.patterns.model.Given
  type Defeater = onera.pmlanalyzer.views.patterns.model.Defeater
  type FinalEvidence = onera.pmlanalyzer.views.patterns.model.FinalEvidence
  type Strategy = onera.pmlanalyzer.views.patterns.model.Strategy
}
