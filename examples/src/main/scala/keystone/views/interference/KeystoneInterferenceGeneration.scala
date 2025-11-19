package keystone.views.interference

import keystone.pml.{
  KeystonePlatform,
  KeystoneRoutingConstraints,
  RosaceConfiguration
}
import onera.pmlanalyzer.views.interference.operators.*
import scala.concurrent.duration.*
import scala.language.postfixOps

object KeystoneInterferenceGeneration extends App {

  object KeystoneWithRosace
      extends KeystonePlatform
      with RosaceConfiguration
      with KeystoneRoutingConstraints
      with RosaceInterferenceSpecification

  KeystoneWithRosace.computeAllInterference(2 hours)
}
