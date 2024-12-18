package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*


trait NocC4S4G1B8Software {
  self: NocC4S4G1B8Platform =>


  val app_rosace_cg0_cl0_C0: Application = Application()
  app_rosace_cg0_cl0_C0 hostedBy rosace.cg0.cl0.C0

  val app_rosace_cg0_cl1_C0: Application = Application()
  app_rosace_cg0_cl1_C0 hostedBy rosace.cg0.cl1.C0

  val app_rosace_cg0_cl2_C0: Application = Application()
  app_rosace_cg0_cl2_C0 hostedBy rosace.cg0.cl2.C0

  val app_rosace_cg0_cl3_C0: Application = Application()
  app_rosace_cg0_cl3_C0 hostedBy rosace.cg0.cl3.C0

  val app_dma: Application = Application()
  app_dma hostedBy rosace.dma


}
