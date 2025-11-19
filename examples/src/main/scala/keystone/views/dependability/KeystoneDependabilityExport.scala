package keystone.views.dependability

import onera.pmlanalyzer.views.dependability.exporters.*
import keystone.pml.{KeystonePlatform, RosaceConfiguration}
import keystone.views.dependability.given

object KeystoneDependabilityExport extends App {

  // Creation of the Keystone platform with the rosace application (not configured hor now)
  object Keystone
      extends KeystonePlatform
      with RosaceConfiguration
      with RosaceDependabilitySpecification

  // Generation of the Cecilia file for safety analysis
  println(s"[INFO] Exporting ${Keystone.fullName} to Cecilia")
  PlatformExportOps[OLE, Keystone.type](Keystone).exportAsCeciliaWithFM()
}
