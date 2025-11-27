package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.Transporter
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification

trait GenericApplicationInterferenceSpecification
    extends ApplicativeTableBasedInterferenceSpecification {
  self: GenericPlatform with GenericTransactionLibrary with GenericSoftware =>

  private val groups: Seq[Group[Cluster]] = groupDSP ++ groupCore
  private val clusters: Seq[Cluster] =
    for {
      g <- groups
      cI <- g.clusters
      cJ <- cI
    } yield cJ

  private val pf_crossbar_ports: Seq[Transporter] =
    PlatformCrossBar.groupDSPInputPorts ++
      PlatformCrossBar.groupDSPOutputPorts ++
      PlatformCrossBar.groupCoreInputPorts ++
      PlatformCrossBar.groupCoreOutputPorts ++
      PlatformCrossBar.ddrOutputPorts :+
      PlatformCrossBar.dma_input_port :+
      PlatformCrossBar.config_bus_output_port :+
      PlatformCrossBar.eth_output_port

  private val core_applications: Seq[Application] =
    (coreApplications ++ dspApplications).flatten.flatten.flatten

  /**
   * Cores only affect group-level buses.
   * FiXME Cores from the same cluster will interfere at the group and L2 levels
   */
  // Cores do not affect cluster-level transporters
  for {
    application <- core_applications
    c <- clusters
    port <- Seq(c.bus, c.input_port, c.output_port)
  } {
    application notInterfereWith port
  }
  // Cores do not affect platform-level transporters
  for {
    application <- core_applications
    port <- pf_crossbar_ports
  } {
    application notInterfereWith port
  }

  /**
   * Cores do not affect the memory controllers
   */
  for {
    application <- core_applications
    ddr <- ddrs
  } {
    application notInterfereWith ddr.ddr_ctrl
    application notInterfereWith ddr.input_port
  }

}
