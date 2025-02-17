package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait HbusClXCYBYRoutingConstraints {
  self: HbusClXCYBYPlatform =>

  private val dma_targets: Seq[Target] = ddr.banks :+ eth

  private val cluster_inputs: Seq[Hardware] =
    cg0.input_port +: cg0.clusters.map(_.input_port)

  private val cluster_outputs: Seq[Hardware] =
    cg0.output_port +: cg0.clusters.map(_.output_port)

  private val cores: Seq[Initiator] =
    for {
      cluster <- cg0.clusters
      core <- cluster.cores
    } yield
      core

  for {
    i <- cores
    target <- Target.all
    in_port <- cluster_inputs
  } {
    i targeting target blockedBy in_port
  }

  for {
    target <- dma_targets
    in_port <- cluster_inputs
  } {
    dma targeting target blockedBy in_port
  }

}
