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

package keystone.pml

import keystone.pml.{
  KeystonePlatform,
  KeystoneRoutingConstraints,
  RosaceConfiguration
}
import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.operators.*

object KeystoneExport extends App {

  // Creation of the Keystone platform without configuration
  object Keystone extends KeystonePlatform

  // Creation of the Keystone platform configured with Rosace
  object KeystoneWithRosace
      extends KeystonePlatform
      with RosaceConfiguration
      with KeystoneRoutingConstraints

  // Export non-configured SW and HW dependencies
  Keystone.exportHWAndSWGraph()

  // Export non-configured Service dependencies
  Keystone.exportServiceGraph()

  // Export only general HW dependencies used by SW (explicit)
  KeystoneWithRosace.exportRestrictedHWAndSWGraph()

  // Export only general Service dependencies used by SW (explicit)
  KeystoneWithRosace.exportRestrictedServiceAndSWGraph()

  // Export individually the Service graph of each software
  KeystoneWithRosace.applications foreach {
    KeystoneWithRosace.exportRestrictedServiceGraphForSW
  }

  KeystoneWithRosace.exportAllocationTable()
  KeystoneWithRosace.exportDataAllocationTable()
  KeystoneWithRosace.exportSWTargetUsageTable()
  KeystoneWithRosace.exportRouteTable()
  KeystoneWithRosace.exportDeactivatedComponents()
  KeystoneWithRosace.exportPhysicalTransactions()
  KeystoneWithRosace.exportUserTransactions()
}
