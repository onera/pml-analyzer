package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*


trait HbusCl4C2B8Software {
  self: HbusCl4C2B8Platform =>


  val app_rosace_cg0_cl0_C0: Application = Application()
  app_rosace_cg0_cl0_C0 hostedBy rosace.cg0.cl0.C0

  val app_rosace_cg0_cl0_C1: Application = Application()
  app_rosace_cg0_cl0_C1 hostedBy rosace.cg0.cl0.C1

  val app_rosace_cg0_cl1_C0: Application = Application()
  app_rosace_cg0_cl1_C0 hostedBy rosace.cg0.cl1.C0

  val app_rosace_cg0_cl1_C1: Application = Application()
  app_rosace_cg0_cl1_C1 hostedBy rosace.cg0.cl1.C1

  val app_rosace_cg0_cl2_C0: Application = Application()
  app_rosace_cg0_cl2_C0 hostedBy rosace.cg0.cl2.C0

  val app_rosace_cg0_cl2_C1: Application = Application()
  app_rosace_cg0_cl2_C1 hostedBy rosace.cg0.cl2.C1

  val app_rosace_cg0_cl3_C0: Application = Application()
  app_rosace_cg0_cl3_C0 hostedBy rosace.cg0.cl3.C0

  val app_rosace_cg0_cl3_C1: Application = Application()
  app_rosace_cg0_cl3_C1 hostedBy rosace.cg0.cl3.C1

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma


}
