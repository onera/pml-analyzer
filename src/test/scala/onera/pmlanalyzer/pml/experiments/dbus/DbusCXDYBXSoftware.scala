package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

trait DbusCXDYBXSoftware {
  self: DbusCXDYBXPlatform =>

  val standardCoreApplications: Seq[Application] =
    for { i <- rosace.cg0.cl0.cores.indices } yield {
      Application(s"app_rosace_cg0_cl0_C$i")
    }

  for { (app, core) <- standardCoreApplications.zip(rosace.cg0.cl0.cores) }
    app hostedBy core

  val ioCoreApplications: Seq[Application] =
    for { i <- rosace.dg0.cl0.cores.indices } yield {
      Application(s"app_rosace_dg0_cl0_C$i")
    }

  for { (app, core) <- ioCoreApplications.zip(rosace.dg0.cl0.cores) }
    app hostedBy core

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma

}
