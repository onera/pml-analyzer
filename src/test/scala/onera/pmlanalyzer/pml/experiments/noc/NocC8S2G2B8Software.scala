package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*


trait NocC8S2G2B8Software {
  self: NocC8S2G2B8Platform =>


  val app_rosace_cg0_cl0_C0: Application = Application()
  app_rosace_cg0_cl0_C0 hostedBy rosace.cg0.cl0.C0

  val app_rosace_cg0_cl1_C0: Application = Application()
  app_rosace_cg0_cl1_C0 hostedBy rosace.cg0.cl1.C0

  val app_rosace_cg0_cl2_C0: Application = Application()
  app_rosace_cg0_cl2_C0 hostedBy rosace.cg0.cl2.C0

  val app_rosace_cg0_cl3_C0: Application = Application()
  app_rosace_cg0_cl3_C0 hostedBy rosace.cg0.cl3.C0

  val app_rosace_cg1_cl0_C0: Application = Application()
  app_rosace_cg1_cl0_C0 hostedBy rosace.cg1.cl0.C0

  val app_rosace_cg1_cl1_C0: Application = Application()
  app_rosace_cg1_cl1_C0 hostedBy rosace.cg1.cl1.C0

  val app_rosace_cg1_cl2_C0: Application = Application()
  app_rosace_cg1_cl2_C0 hostedBy rosace.cg1.cl2.C0

  val app_rosace_cg1_cl3_C0: Application = Application()
  app_rosace_cg1_cl3_C0 hostedBy rosace.cg1.cl3.C0

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma


}
