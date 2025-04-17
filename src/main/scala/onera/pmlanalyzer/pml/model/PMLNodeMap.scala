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

package onera.pmlanalyzer.pml.model

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*

import scala.collection.mutable

final case class PMLNodeMap[T <: PMLNode] private (map: mutable.Map[Symbol, T])

object PMLNodeMap {

  given (using i: Instances): PMLNodeMap[Application] = i.memoApplication

  given (using i: Instances): PMLNodeMap[Data] = i.memoData

  given (using i: Instances): PMLNodeMap[Load] = i.memoLoad

  given (using i: Instances): PMLNodeMap[Store] = i.memoStore

  given (using i: Instances): PMLNodeMap[ArtificialService] =
    i.memoArtificialService

  given (using i: Instances): PMLNodeMap[Target] = i.memoTarget

  given (using i: Instances): PMLNodeMap[Initiator] = i.memoInitiator

  given (using i: Instances): PMLNodeMap[SimpleTransporter] =
    i.memoSimpleTransporter

  given (using i: Instances): PMLNodeMap[Virtualizer] = i.memoVirtualizer

  given (using i: Instances): PMLNodeMap[Composite] = i.memoComposite

  given (using i: Instances): PMLNodeMap[Transaction] = i.memoTransaction
  given (using i: Instances): PMLNodeMap[UsedTransaction] =
    i.memoUsedTransaction
  given (using i: Instances): PMLNodeMap[Scenario] = i.memoScenario
  given (using i: Instances): PMLNodeMap[UsedScenario] = i.memoUsedScenario

  trait Instances {
    val memoApplication: PMLNodeMap[Application]
    val memoData: PMLNodeMap[Data]
    val memoLoad: PMLNodeMap[Load]
    val memoStore: PMLNodeMap[Store]
    val memoArtificialService: PMLNodeMap[ArtificialService]
    val memoTarget: PMLNodeMap[Target]
    val memoInitiator: PMLNodeMap[Initiator]
    val memoSimpleTransporter: PMLNodeMap[SimpleTransporter]
    val memoVirtualizer: PMLNodeMap[Virtualizer]
    val memoComposite: PMLNodeMap[Composite]
    val memoTransaction: PMLNodeMap[Transaction]
    val memoUsedTransaction: PMLNodeMap[UsedTransaction]
    val memoScenario: PMLNodeMap[Scenario]
    val memoUsedScenario: PMLNodeMap[UsedScenario]
  }

  trait EmptyInstances extends Instances {
    val memoApplication: PMLNodeMap[Application] = PMLNodeMap(mutable.Map.empty)
    val memoData: PMLNodeMap[Data] = PMLNodeMap(mutable.Map.empty)
    val memoLoad: PMLNodeMap[Load] = PMLNodeMap(mutable.Map.empty)
    val memoStore: PMLNodeMap[Store] = PMLNodeMap(mutable.Map.empty)
    val memoArtificialService: PMLNodeMap[ArtificialService] = PMLNodeMap(
      mutable.Map.empty
    )
    val memoTarget: PMLNodeMap[Target] = PMLNodeMap(mutable.Map.empty)
    val memoInitiator: PMLNodeMap[Initiator] = PMLNodeMap(mutable.Map.empty)
    val memoSimpleTransporter: PMLNodeMap[SimpleTransporter] = PMLNodeMap(
      mutable.Map.empty
    )
    val memoVirtualizer: PMLNodeMap[Virtualizer] = PMLNodeMap(mutable.Map.empty)
    val memoComposite: PMLNodeMap[Composite] = PMLNodeMap(mutable.Map.empty)
    val memoTransaction: PMLNodeMap[Transaction] = PMLNodeMap(mutable.Map.empty)
    val memoUsedTransaction: PMLNodeMap[UsedTransaction] = PMLNodeMap(
      mutable.Map.empty
    )
    val memoScenario: PMLNodeMap[Scenario] = PMLNodeMap(mutable.Map.empty)
    val memoUsedScenario: PMLNodeMap[UsedScenario] = PMLNodeMap(
      mutable.Map.empty
    )
  }

}
