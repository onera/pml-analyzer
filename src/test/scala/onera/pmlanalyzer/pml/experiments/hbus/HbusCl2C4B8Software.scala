package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

trait HbusCl2C4B8Software {
  self: HbusCl2C4B8Platform =>

  val app_rosace_cg0_cl0_C0: Application = Application()
  app_rosace_cg0_cl0_C0 hostedBy rosace.cg0.cl0.C0

  val app_rosace_cg0_cl0_C1: Application = Application()
  app_rosace_cg0_cl0_C1 hostedBy rosace.cg0.cl0.C1

  val app_rosace_cg0_cl0_C2: Application = Application()
  app_rosace_cg0_cl0_C2 hostedBy rosace.cg0.cl0.C2

  val app_rosace_cg0_cl0_C3: Application = Application()
  app_rosace_cg0_cl0_C3 hostedBy rosace.cg0.cl0.C3

  val app_rosace_cg0_cl1_C0: Application = Application()
  app_rosace_cg0_cl1_C0 hostedBy rosace.cg0.cl1.C0

  val app_rosace_cg0_cl1_C1: Application = Application()
  app_rosace_cg0_cl1_C1 hostedBy rosace.cg0.cl1.C1

  val app_rosace_cg0_cl1_C2: Application = Application()
  app_rosace_cg0_cl1_C2 hostedBy rosace.cg0.cl1.C2

  val app_rosace_cg0_cl1_C3: Application = Application()
  app_rosace_cg0_cl1_C3 hostedBy rosace.cg0.cl1.C3

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma

}
