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

final case class ArbitraryConfiguration(
    maxTargetLoad: Int = 2,
    maxTargetStore: Int = 2,
    maxVirtualizerLoad: Int = 2,
    maxVirtualizerStore: Int = 2,
    maxSimpleTransporterLoad: Int = 2,
    maxSimpleTransporterStore: Int = 2,
    maxInitiatorLoad: Int = 2,
    maxInitiatorStore: Int = 2,
    maxCompositePerContainer: Int = 2,
    maxCompositeLayers: Int = 3,
    maxInitiatorInContainer: Int = 3,
    maxTargetInContainer: Int = 3,
    maxTransporterInContainer: Int = 6,
    maxApplication: Int = 20,
    maxData: Int = 20,
    maxTransaction: Int = 100,
    discardImpossibleTransactions: Boolean = true,
    discardMultiPathTransactions: Boolean = true,
    maxLinkPerComponent: Int = 3,
    forceTotalHosting: Boolean = false,
    removeUnreachableLink: Boolean = true,
    maxRoutingConstraint: Int = 50,
    showArbitraryInfo: Boolean = false
)

object ArbitraryConfiguration {
  implicit val default: ArbitraryConfiguration = ArbitraryConfiguration()
}
