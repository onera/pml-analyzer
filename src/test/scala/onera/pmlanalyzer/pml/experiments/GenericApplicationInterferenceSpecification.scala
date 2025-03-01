package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.Transporter
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.operators.*

trait GenericApplicationInterferenceSpecification
  extends ApplicativeTableBasedInterferenceSpecification {
  self: GenericPlatform
    with GenericTransactionLibrary
    with GenericSoftware =>

  private val groups: Seq[Group[? >: ClusterCore & ClusterDSP <: Cluster]] = (groupDSP ++ groupCore)
  private val clusters: Seq[Cluster] = groups.flatMap(_.clusters).flatten

  private val pf_crossbar_ports: Seq[Transporter] = (
    PlatformCrossBar.groupDSPInputPorts ++
      PlatformCrossBar.groupDSPOutputPorts ++
      PlatformCrossBar.groupCoreInputPorts ++
      PlatformCrossBar.groupCoreOutputPorts ++
      PlatformCrossBar.ddrOutputPorts :+
      PlatformCrossBar.dma_input_port :+
      PlatformCrossBar.config_bus_output_port :+
      PlatformCrossBar.eth_output_port
  )

  private val core_applications: Seq[Application] = (coreApplications ++ dspApplications).flatten.flatten.flatten

  /**
   * Cores only affect group-level buses.
   * FiXME Cores from the same cluster will interfere at the group and L2 levels
   */
  // Cores do not affect cluster-level transporters
  for {
    application <- core_applications
    ports <- clusters.flatMap(c => Seq(c.bus, c.input_port, c.output_port))
  }  {
    application notInterfereWith ports.loads
    application notInterfereWith ports.stores
  }
  // Cores do not affect platform-level transporters
  for {
    application <- (coreApplications ++ dspApplications).flatten.flatten.flatten
    ports <- pf_crossbar_ports
  }  {
    application notInterfereWith ports.loads
    application notInterfereWith ports.stores
  }

  /**
   * Cores do not affect the memory controllers
   */
  for {
    application <- core_applications
    ddr <- ddrs
  } {
    application notInterfereWith ddr.ddr_ctrl.loads
    application notInterfereWith ddr.ddr_ctrl.stores
    application notInterfereWith ddr.input_port.loads
    application notInterfereWith ddr.input_port.stores
  }



}
