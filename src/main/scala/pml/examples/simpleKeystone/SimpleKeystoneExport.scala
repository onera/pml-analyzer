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

package pml.examples.simpleKeystone

import pml.exporters.*
import pml.model.utils.Message
import pml.operators.*
import views.interference.examples.simpleKeystone.{SimpleKeystoneApplicativeTableBasedInterferenceSpecification, SimpleKeystonePhysicalTableBasedInterferenceSpecification}

/**
  * Program entry point to export several version of Keystone
  */
object SimpleKeystoneExport extends App {

  /**
    * Keystone where all transactions are considered
    * @group platform_def
    */
  object SimpleKeystoneConfiguredFull extends SimpleKeystonePlatform
    with SimpleKeystoneLibraryConfigurationFull
    with SimpleRoutingConfiguration
    with SimpleKeystonePhysicalTableBasedInterferenceSpecification
    with SimpleKeystoneApplicativeTableBasedInterferenceSpecification

  /**
    * Keystone where L1 cache is not used
    * @group platform_def
    */
  object SimpleKeystoneConfiguredNoL1 extends SimpleKeystonePlatform
    with SimpleKeystoneLibraryConfigurationNoL1
    with SimpleRoutingConfiguration
    with SimpleKeystonePhysicalTableBasedInterferenceSpecification
    with SimpleKeystoneApplicativeTableBasedInterferenceSpecification

  /**
    * Keystone where only transactions for app4, app1 and app21 are scheduled
    * @group platform_def
    */
  object SimpleKeystoneConfiguredPlanApp21 extends SimpleKeystonePlatform
    with SimpleKeystoneLibraryConfigurationPlanApp21
    with SimpleRoutingConfiguration
    with SimpleKeystonePhysicalTableBasedInterferenceSpecification
    with SimpleKeystoneApplicativeTableBasedInterferenceSpecification

  /**
    * Keystone where only transactions for app22, app1 and app3 are scheduled
    * @group platform_def
    */
  object SimpleKeystoneConfiguredPlanApp22 extends SimpleKeystonePlatform
    with SimpleKeystoneLibraryConfigurationPlanApp22
    with SimpleRoutingConfiguration
    with SimpleKeystonePhysicalTableBasedInterferenceSpecification
    with SimpleKeystoneApplicativeTableBasedInterferenceSpecification

  for (p <- Set(SimpleKeystoneConfiguredFull,SimpleKeystoneConfiguredNoL1,SimpleKeystoneConfiguredPlanApp21,SimpleKeystoneConfiguredPlanApp22))  {
    // Export only HW used by SW (explicit)
    p.exportRestrictedHWAndSWGraph()
    // Export HW and SW graph whether used or not
    p.exportHWAndSWGraph()

    // Export individually the Service graph of each software
    p.applications foreach { s => p.exportRestrictedServiceGraphForSW(s) }

    // Export the application allocation table
    p.exportAllocationTable()

    // Export the data allocation table
    p.exportDataAllocationTable()

    // Export the target used by software
    p.exportSWTargetUsageTable()

    // Export the routing constraints
    p.exportRouteTable()

    // Export the deactivated components
    p.exportDeactivatedComponents()

    // Export the transactions defined by the user
    p.exportUserTransactions()
  }

}
