package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait NocC8S4G2B8RoutingConstraints {
  self: NocC8S4G2B8Platform =>

  private val dma_targets: Seq[Target] = Seq(
    rosace.ddr.BK0,
    rosace.ddr.BK1,
    rosace.ddr.BK2,
    rosace.ddr.BK3,
    rosace.ddr.BK4,
    rosace.ddr.BK5,
    rosace.ddr.BK6,
    rosace.ddr.BK7,
    rosace.eth
  )

  private val cluster_inputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.input_port,
    rosace.cg0.cl1.input_port,
    rosace.cg0.cl2.input_port,
    rosace.cg0.cl3.input_port,
    rosace.cg0.input_port,
    rosace.cg1.cl0.input_port,
    rosace.cg1.cl1.input_port,
    rosace.cg1.cl2.input_port,
    rosace.cg1.cl3.input_port,
    rosace.cg1.input_port
  )

  private val cluster_outputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.cl1.output_port,
    rosace.cg0.cl2.output_port,
    rosace.cg0.cl3.output_port,
    rosace.cg0.output_port,
    rosace.cg1.cl0.output_port,
    rosace.cg1.cl1.output_port,
    rosace.cg1.cl2.output_port,
    rosace.cg1.cl3.output_port,
    rosace.cg1.output_port
  )

  private val cores: Seq[Initiator] = Seq(
    rosace.cg0.cl0.C0,
    rosace.cg0.cl1.C0,
    rosace.cg0.cl2.C0,
    rosace.cg0.cl3.C0,
    rosace.cg1.cl0.C0,
    rosace.cg1.cl1.C0,
    rosace.cg1.cl2.C0,
    rosace.cg1.cl3.C0
  )

  private val srams: Seq[Target] = Seq(
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
