package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait DbusC2D8B8RoutingConstraints {
  self: DbusC2D8B8Platform =>

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
    rosace.cg0.input_port,
  )

  val cluster_outputs = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.output_port,
  )

  val cores = Seq(
    rosace.cg0.cl0.C0,
    rosace.cg0.cl0.C1,
    rosace.dg0.cl0.C0,
    rosace.dg0.cl0.C1,
    rosace.dg0.cl0.C2,
    rosace.dg0.cl0.C3,
    rosace.dg0.cl0.C4,
    rosace.dg0.cl0.C5,
    rosace.dg0.cl0.C6,
    rosace.dg0.cl0.C7,
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
