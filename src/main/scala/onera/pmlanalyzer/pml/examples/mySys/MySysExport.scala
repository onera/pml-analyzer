/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * ****************************************************************************
  */

package onera.pmlanalyzer.pml.examples.mySys

import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.examples.mySys.{
  MySysInterferenceSpecification,
  MyProcInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.operators.*

/** Program entry point to export several version of Keystone
  */
object MySysExport extends App {

  /** Keystone where all transactions are considered
    *
    * @group platform_def
    */
  object MySys
      extends MyProcPlatform
      with MySysLibraryConfiguration
      with MyProcRoutingConfiguration
      with MyProcInterferenceSpecification
      with MySysInterferenceSpecification

  // Export only HW used by SW (explicit)
  MySys.exportRestrictedHWAndSWGraph()

  // Export HW and SW graph whether used or not
  MySys.exportHWAndSWGraph()

  // Export Service graph whether used or not and considering that all services are non-exclusive
  MySys.exportServiceGraph()

  // Export Service graph considering and SW
  MySys.exportRestrictedServiceAndSWGraph()

  // Export Service graph considering that all services are non-exclusive
  MySys.exportServiceGraphWithInterfere()

  // Export individually the Service graph of each software
  MySys.applications foreach { s => MySys.exportRestrictedServiceGraphForSW(s) }

  // Export the application allocation table
  MySys.exportAllocationTable()

  // Export the data allocation table
  MySys.exportDataAllocationTable()

  // Export the target used by software
  MySys.exportSWTargetUsageTable()

  // Export the routing constraints
  MySys.exportRouteTable()

  // Export the deactivated components
  MySys.exportDeactivatedComponents()

  // Export the transactions defined by the user
  MySys.exportUserScenarios()

  MySys.exportSemanticsSize()

  //  MySys.exportAnalysisGraph()
}
