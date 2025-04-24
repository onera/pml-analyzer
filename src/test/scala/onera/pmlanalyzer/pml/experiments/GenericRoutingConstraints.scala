package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Target}
import onera.pmlanalyzer.pml.operators.*

trait GenericRoutingConstraints {
  self: GenericPlatform =>

  private val groups: Seq[Group[Cluster]] = groupDSP ++ groupCore

  private val clusters: Seq[Cluster] =
    for {
      g <- groups
      cI <- g.clusters
      cJ <- cI
    } yield cJ

  private val cores: Seq[Initiator] =
    for {
      c <- clusters
      core <- c.cores
    } yield core

  private val group_inputs: Seq[Hardware] = groups.map(_.input_port)
  private val cluster_outputs: Seq[Hardware] = clusters.map(_.output_port)
  private val cluster_inputs: Seq[Hardware] = clusters.map(_.input_port)
  private val platform_to_group_ports: Seq[Hardware] =
    PlatformCrossBar.groupDSPOutputPorts ++ PlatformCrossBar.groupCoreOutputPorts

  // Cores cannot re-enter their own or other clusters and groups
  for {
    core <- cores
    port <- cluster_inputs ++ group_inputs ++ platform_to_group_ports
  } {
    core targeting Target.all blockedBy port
  }

  // DSP use their SRAM directly (instead of the cluster bus)
  for {
    group <- groupDSP
    clusterI <- group.clusters
    clusterJ <- clusterI
    (dsp, sram) <- clusterJ.cores.zip(clusterJ.SRAM)
  } {
    dsp targeting sram useLink dsp to sram
  }

  // DMA cannot enter groups going to configuration- or platform-level targets
  for {
    port <- group_inputs
    target <- Seq(cfg_bus.dma_reg, eth)
  } {
    dma targeting target blockedBy port
  }

  // DMA targeting a SRAM cannot enter other groups or clusters
  for {
    group <- groupDSP
    cluster <- group.clusters.flatten
    sram <- cluster.SRAM
  } {
    for {
      other_group <- groupDSP ++ groupCore
      if other_group != group
    } {
      dma targeting sram blockedBy other_group.input_port
    }

    for {
      other_cluster <- group.clusters.flatten
      if other_cluster != cluster
    } {
      dma targeting sram blockedBy other_cluster.input_port
    }
  }

  // DMA targeting DDR cannot enter groups or clusters
  for {
    ddr <- ddrs
    bank <- ddr.banks
    group <- groupDSP ++ groupCore
  } {
    dma targeting bank blockedBy group.input_port
  }

}
