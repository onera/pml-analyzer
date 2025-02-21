/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.operators.*

trait GenericSoftware {
  self: GenericPlatform =>

  def defineAndHostCoreApplication(
      groups: Seq[Group[ClusterCore]]
  ): Seq[Seq[Seq[Seq[Application]]]] =
    for { gId <- groups.indices } yield for {
      cIdI <- groups(gId).clusters.indices
    } yield for { cIdJ <- groups(gId).clusters(cIdI).indices } yield for {
      coreId <- groups(gId).clusters(cIdI)(cIdJ).cores.indices
    } yield {
      val app = Application(s"app_rosace_cg${gId}_cl${cIdI}_${cIdJ}_C$coreId")
      app hostedBy groups(gId).clusters(cIdI)(cIdJ).cores(coreId)
      app
    }

  def defineAndHostDSPApplication(
      groups: Seq[Group[ClusteDSP]]
  ): Seq[Seq[Seq[Seq[Application]]]] =
    for { gId <- groups.indices } yield for {
      cIdI <- groups(gId).clusters.indices
    } yield for { cIdJ <- groups(gId).clusters(cIdI).indices } yield for {
      coreId <- groups(gId).clusters(cIdI)(cIdJ).cores.indices
    } yield {
      val app = Application(s"app_rosace_dg${gId}_cl${cIdI}_${cIdJ}_C$coreId")
      app hostedBy groups(gId).clusters(cIdI)(cIdJ).cores(coreId)
      app
    }
}
