package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait HbusCl2C2B8RoutingConstraints {
  self: HbusCl2C2B8Platform =>

  private val dma_targets: Seq[Target] = Seq(
    rosace.ddr.BK0,
    rosace.ddr.BK1,
    rosace.ddr.BK2,
    rosace.ddr.BK3,
    rosace.ddr.BK4,
    rosace.ddr.BK5,
    rosace.ddr.BK6,
    rosace.ddr.BK7,
    rosace.eth,
  )

  private val cluster_inputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.input_port,
    rosace.cg0.cl1.input_port,
    rosace.cg0.input_port,
  )

  private val cluster_outputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.cl1.output_port,
    rosace.cg0.output_port,
  )

  private val cores: Seq[Initiator] = Seq(
    rosace.cg0.cl0.C0,
    rosace.cg0.cl0.C1,
    rosace.cg0.cl1.C0,
    rosace.cg0.cl1.C1,
  )

  private val srams: Seq[Target] = Seq(
  )

  for {
    i <- cores
    target <- Target.all
    in_port <- cluster_inputs
  } {
    i targeting target blockedBy in_port
  }

  for {
    i <- Seq(rosace.dma)
    target <- dma_targets
    in_port <- cluster_inputs
  } {
    i targeting target blockedBy in_port
  }

  for {
    i <- Seq(rosace.dma)
    target <- srams
    out_port <- cluster_outputs
  } {
    i targeting target blockedBy out_port
  }

}
