/** *****************************************************************************
 * Copyright (c) 2023. ONERA This file is part of PML Analyzer
 *
 * PML Analyzer is free software ; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation ; either version 2 of the License, or (at your
 * option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program ; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

import scala.language.postfixOps

trait HbusCl4C4B8Software {
  self: HbusCl4C4B8Platform =>

  val app_Cl0C0: Application = Application()
  app_Cl0C0 hostedBy Cl0.C0
  val app_Cl0C1: Application = Application()
  app_Cl0C1 hostedBy Cl0.C1
  val app_Cl0C2: Application = Application()
  app_Cl0C2 hostedBy Cl0.C2
  val app_Cl0C3: Application = Application()
  app_Cl0C3 hostedBy Cl0.C3
  val app_Cl1C0: Application = Application()
  app_Cl1C0 hostedBy Cl1.C0
  val app_Cl1C1: Application = Application()
  app_Cl1C1 hostedBy Cl1.C1
  val app_Cl1C2: Application = Application()
  app_Cl1C2 hostedBy Cl1.C2
  val app_Cl1C3: Application = Application()
  app_Cl1C3 hostedBy Cl1.C3
  val app_Cl2C0: Application = Application()
  app_Cl2C0 hostedBy Cl2.C0
  val app_Cl2C1: Application = Application()
  app_Cl2C1 hostedBy Cl2.C1
  val app_Cl2C2: Application = Application()
  app_Cl2C2 hostedBy Cl2.C2
  val app_Cl2C3: Application = Application()
  app_Cl2C3 hostedBy Cl2.C3
  val app_Cl3C0: Application = Application()
  app_Cl3C0 hostedBy Cl3.C0
  val app_Cl3C1: Application = Application()
  app_Cl3C1 hostedBy Cl3.C1
  val app_Cl3C2: Application = Application()
  app_Cl3C2 hostedBy Cl3.C2
  val app_Cl3C3: Application = Application()
  app_Cl3C3 hostedBy Cl3.C3
  val app_dma: Application = Application()
  app_dma hostedBy DMA

}