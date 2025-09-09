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

package onera.pmlanalyzer.pml.examples.simpleT1042

import onera.pmlanalyzer.pml.exporters._
import onera.pmlanalyzer.pml.operators._
import onera.pmlanalyzer.views.interference.examples.simpleT1042.{
  SimpleT1042ApplicativeTableBasedInterferenceSpecification,
  SimpleT1042PhysicalTableBasedInterferenceSpecification
}

object SimpleT1042Export extends App {

  object SimpleT1042ConfiguredFull
      extends SimpleT1042Platform(Symbol("SimpleFull"))
      with SimpleT1042LibraryConfigurationFull
      with SimpleRoutingConfiguration
      with SimpleT1042PhysicalTableBasedInterferenceSpecification
      with SimpleT1042ApplicativeTableBasedInterferenceSpecification

  object SimpleT1042ConfiguredNoL1
      extends SimpleT1042Platform(Symbol("SimpleNoL1"))
      with SimpleT1042LibraryConfigurationNoL1
      with SimpleRoutingConfiguration
      with SimpleT1042PhysicalTableBasedInterferenceSpecification
      with SimpleT1042ApplicativeTableBasedInterferenceSpecification

  object SimpleT1042ConfiguredPlanApp21
      extends SimpleT1042Platform(Symbol("SimplePlanApp21"))
      with SimpleT1042LibraryConfigurationPlanApp21
      with SimpleRoutingConfiguration
      with SimpleT1042PhysicalTableBasedInterferenceSpecification
      with SimpleT1042ApplicativeTableBasedInterferenceSpecification

  object SimpleT1042ConfiguredPlanApp22
      extends SimpleT1042Platform(Symbol("SimplePlanApp22"))
      with SimpleT1042LibraryConfigurationPlanApp22
      with SimpleRoutingConfiguration
      with SimpleT1042PhysicalTableBasedInterferenceSpecification
      with SimpleT1042ApplicativeTableBasedInterferenceSpecification

  for (
    p <- Set(
      SimpleT1042ConfiguredFull,
      SimpleT1042ConfiguredNoL1,
      SimpleT1042ConfiguredPlanApp21,
      SimpleT1042ConfiguredPlanApp22
    )
  ) {
    // Export only general HW dependencies used by SW (explicit)
    p.exportRestrictedHWAndSWGraph()

    // Export individually the Service graph of each software
    p.applications foreach { s => p.exportRestrictedServiceGraphForSW(s) }

    p.exportAllocationTable()
    p.exportDataAllocationTable()
    p.exportSWTargetUsageTable()
    p.exportRouteTable()
    p.exportDeactivatedComponents()
    p.exportUserScenarios()
  }

}
