package onera.pmlanalyzer.pml.model.instances.keystone

import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.operators.*

object KeystoneExport extends App {

  // Creation of the Keystone platform without configuration
  object Keystone extends KeystonePlatform

  // Creation of the Keystone platform configured with Rosace
  object KeystoneWithRosace extends KeystonePlatform
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
