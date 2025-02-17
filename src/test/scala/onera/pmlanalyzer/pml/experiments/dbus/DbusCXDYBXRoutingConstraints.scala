package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait DbusCXDYBXRoutingConstraints {
  self: DbusCXDYBXPlatform =>

  //FIXME Add all inputs and outputs to the collections
  private val cluster_inputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.input_port,
    rosace.cg0.input_port,
    rosace.dg0.cl0.input_port,
    rosace.dg0.input_port
  )

  private val cluster_outputs: Seq[Hardware] = Seq(
    rosace.cg0.cl0.output_port,
    rosace.cg0.output_port,
    rosace.dg0.cl0.output_port,
    rosace.dg0.output_port
  )

  private val cores: Seq[Initiator] =
    rosace.cg0.cl0.cores ++ rosace.dg0.cl0.cores

  for {
    core <- cores
    target <- Target.all
    in_port <- cluster_inputs
  } {
    core targeting target blockedBy in_port
  }

  //FIXME The DSP cannot use the bus to access to their SRAM
  for {
    dsp <- rosace.dg0.cl0.cores
    sram <- rosace.dg0.cl0.SRAM
  } {
    dsp targeting sram blockedBy rosace.dg0.cl0.bus
  }

  for {
    target <- Target.all
    out_port <- cluster_outputs
  } {
    rosace.dma targeting target blockedBy out_port
  }

}
