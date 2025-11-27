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

import onera.pmlanalyzer.pml.model.hardware.{
  Composite,
  SimpleTransporter,
  Target
}
import onera.pmlanalyzer.pml.model.utils.{Context, ReflexiveInfo}
import onera.pmlanalyzer.pml.operators.*

final class DDR private (
    val id: Int,
    nbDDRBank: Int,
    ddrInfo: ReflexiveInfo,
    ddrContext: Context
) extends Composite(Symbol(s"ddr$id"), ddrInfo, ddrContext) {

  def this(ident: Int, nbDDRB: Int, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(ident, nbDDRB, givenInfo, givenContext)
  }

  val banks: Seq[Target] =
    for { i <- 0 until nbDDRBank } yield Target(s"BK$i")

  val ddr_ctrl: SimpleTransporter = SimpleTransporter()

  val input_port: SimpleTransporter = SimpleTransporter()

  for { bank <- banks }
    ddr_ctrl link bank

  input_port link ddr_ctrl
}
