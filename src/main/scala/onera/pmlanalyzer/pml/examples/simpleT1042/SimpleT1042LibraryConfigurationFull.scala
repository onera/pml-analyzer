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

package onera.pmlanalyzer.pml.examples.simpleT1042

trait SimpleT1042LibraryConfigurationFull extends SimpleT1042LibraryConfiguration {
  self: SimpleT1042Platform =>

  app4_wr_input_d.used
  app21_rd_input_d.used
  app1_rd_wr_L1.used
  app21_wr_d1.used
  app22_rd_d2.used
  app22_wr_output_d.used
  app22_st_dma_reg.used
  app3_transfer.used
}
