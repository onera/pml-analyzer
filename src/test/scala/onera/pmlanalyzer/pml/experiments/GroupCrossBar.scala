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

import onera.pmlanalyzer.pml.model.hardware.{Composite, SimpleTransporter}
import onera.pmlanalyzer.pml.model.utils.Context
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.operators.*

final class GroupCrossBar private (
    val id: Int,
    nbCluster: Int,
    nbGroup: Int,
    groupCrossBarInfo: ReflexiveInfo,
    groupCrossBarContext: Context
) extends Composite(
      Symbol(s"group_bus$id"),
      groupCrossBarInfo,
      groupCrossBarContext
    ) {

  def this(ident: Int, nbCl: Int, nbGr: Int, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(ident, nbCl, nbGr, givenInfo, givenContext)
  }

  val clusterInput: Seq[Seq[SimpleTransporter]] =
    for {
      i <- 0 until nbCluster
    } yield {
      for {
        j <- 0 until nbGroup
      } yield {
        SimpleTransporter(s"input_port_Cl${i}_$j")
      }
    }

  val clusterOuput: Seq[Seq[SimpleTransporter]] =
    for {
      i <- 0 until nbCluster
    } yield {
      for {
        j <- 0 until nbGroup
      } yield {
        SimpleTransporter(s"output_port_Cl${i}_$j")
      }
    }

  val input_port: SimpleTransporter = SimpleTransporter()
  val output_port: SimpleTransporter = SimpleTransporter()

  for {
    input <- clusterInput.flatten :+ input_port
    output <- clusterOuput.flatten :+ output_port
  } {
    input link output
  }
}
