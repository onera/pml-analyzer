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

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.examples.mySys.{
  MyProcInterferenceSpecification,
  MySysInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.operators.*

/** Program entry point to export several version of Keystone
  */
object GenericExport extends App {

  /** Keystone where all transactions are considered
    *
    * @group platform_def
    */
  //    extends GenericPlatform(
  //      name = Symbol("GenericSample"),
  //      nbGroupDSP = 1,
  //      nbGroupCore = 1,
  //      nbClusterGroupDSP =
  //        1, // FIXME This input seems redundant with the DSP Per Group
  //      nbClusterGroupCore =
  //        1, // FIXME This input seems redundant with the Core Per Group
  //      nbClusterDSPPerGroup = 2,
  //      nbClusterCorePerGroup = 2,
  //      nbDSPPerCluster = 2,
  //      nbCorePerCluster = 2,
  //      nbDDRBank = 8,
  //      nbDDRController = 1
  //    )
  object GenericSample
      extends GenericPlatform(
        name = Symbol("GenericSample"),
        nbGroupDSP = 1,
        nbGroupCore = 0,
        nbClusterGroupDSP =
          1, // FIXME This input seems redundant with the DSP Per Group
        nbClusterGroupCore =
          1, // FIXME This input seems redundant with the Core Per Group
        nbClusterDSPPerGroup = 1,
        nbClusterCorePerGroup = 2,
        nbDSPPerCluster = 1,
        nbCorePerCluster = 2,
        nbDDRBank = 8,
        nbDDRController = 1
      )
      with GenericSoftware
      with GenericTransactionLibrary
      with GenericRoutingConstraints
//      with MyProcInterferenceSpecification
//      with MySysInterferenceSpecification

  // Export only HW used by SW (explicit)
  GenericSample.exportRestrictedHWAndSWGraph()

  // Export HW and SW graph whether used or not
  GenericSample.exportHWAndSWGraph()

  // Export Service graph whether used or not and considering that all services are non-exclusive
  GenericSample.exportServiceGraph()

  // Export Service graph considering and SW
  GenericSample.exportRestrictedServiceAndSWGraph()

  // Export Service graph considering that all services are non-exclusive
  GenericSample.exportServiceGraphWithInterfere()

  // Export individually the Service graph of each software
  GenericSample.applications foreach { s =>
    GenericSample.exportRestrictedServiceGraphForSW(s)
  }

  // Export the application allocation table
  GenericSample.exportAllocationTable()

  // Export the data allocation table
  GenericSample.exportDataAllocationTable()

  // Export the target used by software
  GenericSample.exportSWTargetUsageTable()

  // Export the routing constraints
  GenericSample.exportRouteTable()

  // Export the deactivated components
  GenericSample.exportDeactivatedComponents()

  // Export the transactions defined by the user
  GenericSample.exportUserScenarios()

//  GenericSample.exportSemanticsSize()

  //  GenericSample.exportAnalysisGraph()
}
