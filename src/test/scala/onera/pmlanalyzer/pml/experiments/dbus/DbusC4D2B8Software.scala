package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*


trait DbusC4D2B8Software {
  self: DbusC4D2B8Platform =>


  val app_rosace_cg0_cl0_C0: Application = Application()
  app_rosace_cg0_cl0_C0 hostedBy rosace.cg0.cl0.C0

  val app_rosace_cg0_cl0_C1: Application = Application()
  app_rosace_cg0_cl0_C1 hostedBy rosace.cg0.cl0.C1

  val app_rosace_cg0_cl0_C2: Application = Application()
  app_rosace_cg0_cl0_C2 hostedBy rosace.cg0.cl0.C2

  val app_rosace_cg0_cl0_C3: Application = Application()
  app_rosace_cg0_cl0_C3 hostedBy rosace.cg0.cl0.C3

  val app_rosace_dg0_cl0_C0: Application = Application()
  app_rosace_dg0_cl0_C0 hostedBy rosace.dg0.cl0.C0

  val app_rosace_dg0_cl0_C1: Application = Application()
  app_rosace_dg0_cl0_C1 hostedBy rosace.dg0.cl0.C1

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma


}
