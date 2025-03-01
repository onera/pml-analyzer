package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Target}
import onera.pmlanalyzer.pml.operators.*

trait GenericRoutingConstraints {
  self: GenericPlatform =>

  private val groups: Seq[Group[? >: ClusterCore & ClusterDSP <: Cluster]] = (groupDSP ++ groupCore)

  private val clusters: Seq[Cluster] = groups.flatMap(_.clusters).flatten

  private val cores: Seq[Initiator] = clusters.flatMap(_.cores)

  private val group_inputs: Seq[Hardware] = groups.map(_.input_port)
  private val cluster_outputs: Seq[Hardware] = clusters.map(_.output_port)
  private val cluster_inputs: Seq[Hardware] = clusters.map(_.input_port)

  // Cores cannot re-enter their own or other clusters and groups
  for {
    core <- cores
    target <- Target.all
    port <- cluster_inputs ++ group_inputs
  } {
    core targeting target blockedBy port
  }

  // DSP use their SRAM directly (instead of the cluster bus)
  for {
    group <- groupDSP
    cluster <- group.clusters.flatten
    dsp <- cluster.cores

    target <- cluster.SRAM
  } {
    dsp targeting target useLink dsp to target
  }


}
