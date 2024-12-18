package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait NocC8S2G1B8RoutingConstraints {
  self: NocC8S2G1B8Platform =>

  val dma_targets = Seq(
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

  val cluster_inputs = Seq(
    rosace.cg0.cl0.input_port,
    rosace.cg0.cl1.input_port,
    rosace.cg0.cl2.input_port,
    rosace.cg0.cl3.input_port,
    rosace.cg0.cl4.input_port,
    rosace.cg0.cl5.input_port,
    rosace.cg0.cl6.input_port,
    rosace.cg0.cl7.input_port,
    rosace.cg0.input_port,
  )

  val cluster_outputs = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.cl1.output_port,
    rosace.cg0.cl2.output_port,
    rosace.cg0.cl3.output_port,
    rosace.cg0.cl4.output_port,
    rosace.cg0.cl5.output_port,
    rosace.cg0.cl6.output_port,
    rosace.cg0.cl7.output_port,
    rosace.cg0.output_port,
  )

  val cores = Seq(
    rosace.cg0.cl0.C0,
    rosace.cg0.cl1.C0,
    rosace.cg0.cl2.C0,
    rosace.cg0.cl3.C0,
    rosace.cg0.cl4.C0,
    rosace.cg0.cl5.C0,
    rosace.cg0.cl6.C0,
    rosace.cg0.cl7.C0,
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

}
