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

package onera.pmlanalyzer.pml.examples.simpleKeystone

import onera.pmlanalyzer.pml.operators._

/**
  * Routing constraints considered for simple Keystone
  */
trait SimpleRoutingConfiguration {
  self: SimpleKeystonePlatform =>

  //Arm cores, ethernet and dma cannot use the periph_bus from msmc
  ARM0.core cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  ARM1.core cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  eth cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
  dma cannotUseLink MemorySubsystem.msmc to TeraNet.periph_bus
}
