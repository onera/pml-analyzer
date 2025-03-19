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

trait HbusClXCYBYSoftware {
  self: HbusClXCYBYPlatform =>

  val clusterApplications: Seq[Seq[Application]] =
    for { i <- cg0.clusters.indices } yield {
      for { j <- cg0.clusters(i).cores.indices } yield {
        val app = Application(s"app_Cl${i}_C$j")
        app hostedBy cg0.clusters(i).cores(j)
        app
      }
    }

  val app_dma: Application = Application()
  app_dma hostedBy dma

}
