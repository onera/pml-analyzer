/*******************************************************************************
 * Copyright (c)  2025. ONERA
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

package onera.pmlanalyzer.pml.examples.riscv.FU740.pml

trait FU740BenchmarkConfiguration
    extends FU740LibraryConfiguration
    with FU740BenchmarkLibrary {
  self: FU740Platform =>

  Hart_0_Load_L1D.used
  Hart_0_Store_L1D.used
  Hart_0_Load_L2.used
  Hart_0_Store_L2.used
  Hart_0_Load_DDR_b0.used
  Hart_0_Store_DDR_b0.used
  Hart_1_Load_L1D.used
  Hart_1_Store_L1D.used
  Hart_1_Load_LIM.used
  Hart_1_Store_LIM.used
  Hart_1_Load_L2.used
  Hart_1_Store_L2.used
  Hart_1_Store_DDR_b0.used
  Hart_1_Load_DDR_b0.used
  Hart_2_Load_L1D.used
  Hart_2_Store_L1D.used
  Hart_2_Load_L2.used
  Hart_2_Store_L2.used
  Hart_2_Load_DDR_b0.used
  Hart_2_Store_DDR_b0.used
  Hart_3_Load_L1D.used
  Hart_3_Store_L1D.used
  Hart_3_Load_L2.used
  Hart_3_Store_L2.used
  Hart_3_Load_DDR_b0.used
  Hart_3_Store_DDR_b0.used
  Hart_4_Load_L1D.used
  Hart_4_Store_L1D.used
  Hart_4_Load_L2.used
  Hart_4_Store_L2.used
  Hart_4_Load_DDR_b0.used
  Hart_4_Store_DDR_b0.used
}
