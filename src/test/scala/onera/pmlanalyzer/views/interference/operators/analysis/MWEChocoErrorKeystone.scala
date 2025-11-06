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

package onera.pmlanalyzer.views.interference.operators.analysis

import org.chocosolver.solver.Model
import org.chocosolver.solver.Solver
import org.chocosolver.solver.constraints.{Constraint, Operator}
import org.chocosolver.solver.expression.discrete.relational.ReExpression
import org.chocosolver.solver.variables.BoolVar
import org.chocosolver.solver.variables.UndirectedGraphVar
import org.chocosolver.util.objects.graphs.UndirectedGraph
import org.chocosolver.util.objects.setDataStructures.SetType

object MWEChocoErrorKeystone extends App {
  /**
   * Create transaction boolean variables
   */
  val transactions = Array(
    "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
    "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
    "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
    "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
    "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
    "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
    "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
    "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
    "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
    "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
    "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
    "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
    "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
    "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
    "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
    "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
    "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
    "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
    "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
    "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
    "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0",
    "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
    "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
    "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
    "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
    "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
    "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
    "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
    "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
    "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
    "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
    "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0",
    "ioServer_SPI",
    "spi_CA",
    "spi_CB"
  )
  val exclusiveTr =
    Map(
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0" -> Array(
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "spi_CB" -> Array("ioServer_SPI", "spi_CA"),
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"
      ),
      "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"
      ),
      "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
      ),
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0" -> Array(
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"
      ),
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0" -> Array(
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "ioServer_SPI" -> Array("spi_CA", "spi_CB"),
      "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0" -> Array(
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_isram_store_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_CorePac2_isram_load_0",
        "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0"
      ),
      "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0"
      ),
      "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "spi_CA" -> Array("ioServer_SPI", "spi_CB"),
      "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"
      ),
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0" -> Array(
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0" -> Array(
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0"
      ),
      "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0"
      ),
      "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_isram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_CorePac1_dsram_load_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_dsram_store_0",
        "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_CorePac1_isram_store_0"
      ),
      "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0" -> Array(
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_isram_store_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_isram_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
      ),
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0" -> Array(
        "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
      ),
      "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0" -> Array(
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0"
      ),
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0" -> Array(
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_CorePac3_isram_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_isram_store_0",
        "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0"
      ),
      "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0" -> Array(
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_isram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_CorePac0_dsram_load_0",
        "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_isram_store_0",
        "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_CorePac0_dsram_store_0"
      )
    )
  val nodes = Seq(
    "<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>",
    "<KeystoneWithRosace_TeraNet_load>",
    "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>",
    "<KeystoneWithRosace_TeraNet_store>",
    "<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>",
    "<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>",
    "<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>",
    "<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>",
    "<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>",
    "<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>",
    "<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>"
  ).zipWithIndex.toMap
  val edges = Seq(
    "<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>" -> "<KeystoneWithRosace_TeraNet_load>",
    "<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>" -> "<KeystoneWithRosace_TeraNet_load>",
    "<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>" -> "<KeystoneWithRosace_TeraNet_load>",
    "<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>" -> "<KeystoneWithRosace_TeraNet_load>",
    "<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>" -> "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>" -> "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>" -> "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>" -> "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>" -> "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>",
    "<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>" -> "<KeystoneWithRosace_TeraNet_store>",
    "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>" -> "<KeystoneWithRosace_TeraNet_store>"
  )
  // Association of transactions using a given channel
  val nodeToTr = Map(
    "<KeystoneWithRosace_TeraNet_store>" -> Array(
      "spi_CA",
      "ioServer_SPI",
      "spi_CB",
      "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_EDMARegister_store_0"
    ),
    "<KeystoneWithRosace_TeraNet_load>" -> Array(
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_EDMARegister_load_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
      "spi_CA",
      "ioServer_SPI",
      "spi_CB",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0"
    ),
    "<KeystoneWithRosace_MSMC_load$KeystoneWithRosace_MSMC_store>" -> Array(
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
      "spi_CA",
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "spi_CB",
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
    ),
    "<KeystoneWithRosace_DDR_Bank1_load$KeystoneWithRosace_DDR_Bank1_store>" -> Array(
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0"
    ),
    "<KeystoneWithRosace_CorePac3_dsram_load$KeystoneWithRosace_CorePac3_dsram_store>" -> Array(
      "KeystoneWithRosace_CorePac3_dsp_store_KeystoneWithRosace_CorePac3_dsram_store_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac3_dsram_load_0"
    ),
    "<KeystoneWithRosace_DDR_Bank0_load$KeystoneWithRosace_DDR_Bank0_store>" -> Array(
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
    ),
    "<KeystoneWithRosace_CorePac2_dsram_load$KeystoneWithRosace_CorePac2_dsram_store>" -> Array(
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0",
      "KeystoneWithRosace_CorePac2_dsp_store_KeystoneWithRosace_CorePac2_dsram_store_0"
    ),
    "<KeystoneWithRosace_AXI_load$KeystoneWithRosace_AXI_store>" -> Array(
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_DDR_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_DDR_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_DDR_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0"
    ),
    "<KeystoneWithRosace_CorePac4_dsram_load$KeystoneWithRosace_CorePac4_dsram_store>" -> Array(
      "ioServer_SPI",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac4_dsram_load_0",
      "KeystoneWithRosace_CorePac4_dsp_store_KeystoneWithRosace_CorePac4_dsram_store_0"
    ),
    "<KeystoneWithRosace_MSMC_SRAM_Bank0_load$KeystoneWithRosace_MSMC_SRAM_Bank0_store>" -> Array(
      "spi_CA",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac0_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_CorePac0_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM0_core_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
      "KeystoneWithRosace_ARMPac_ARM1_core_load_KeystoneWithRosace_MSMC_SRAM_Bank0_load_0"
    ),
    "<KeystoneWithRosace_MSMC_SRAM_Bank1_load$KeystoneWithRosace_MSMC_SRAM_Bank1_store>" -> Array(
      "KeystoneWithRosace_ARMPac_ARM2_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM2_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0",
      "KeystoneWithRosace_ARMPac_ARM3_core_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "spi_CB",
      "KeystoneWithRosace_CorePac2_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac1_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac3_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_MSMC_SRAM_Bank1_load_0",
      "KeystoneWithRosace_CorePac1_dsp_store_KeystoneWithRosace_MSMC_SRAM_Bank1_store_0"
    ),
    "<KeystoneWithRosace_SPI_load$KeystoneWithRosace_SPI_store>" -> Array(
      "spi_CA",
      "ioServer_SPI",
      "spi_CB",
      "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_SPI_load_0"
    )
  )
  def buildModelAndSolve(k: Int): Unit = {
    val model: Model = Model()
    val transactionVar =
      (for {
        id <- transactions
      } yield id -> model.boolVar(id)).toMap
    // Add constraint C^1_{\Sys} i.e. transactions should not be exclusive
    for {
      (tr, ex) <- exclusiveTr
      if ex.nonEmpty
    } {
      val notEx = ex.map(tr2 => transactionVar(tr2).not())
      transactionVar(tr).imp(notEx.head.and(notEx.tail: _*)).post()
    }
    // Add constraint C^2_{\Sys} cardinality constraint
    model.sum(transactionVar.values.toArray, Operator.EQ.toString, k).post()
    // define the upper graph bound (envelop), it contains all possible nodes and edges
    val UB =
      UndirectedGraph(model, nodes.size, SetType.BITSET, SetType.BITSET, false)
    // define the lesser graph bound (kernel), it contains the mandatory nodes and edges, here the empty graph
    val LB =
      UndirectedGraph(model, nodes.size, SetType.BITSET, SetType.BITSET, false)
    // Add the nodes to upper graph
    for {
      (_, i) <- nodes
    } {
      UB.addNode(i)
    }
    // Add the edges to upper graph
    for {
      (l, r) <- edges
    } {
      UB.addEdge(nodes(l), nodes(r))
    }
    // Create a graph variable
    val g: UndirectedGraphVar = model.graphVar("q", LB, UB)
    // Fetch the node variables from g
    val nodeVars = nodes.transform((k, _) => model.boolVar(k))
    model
      .nodesChanneling(g, nodeVars.keySet.toArray.sortBy(nodes).map(nodeVars))
      .post()
    // Fetch the edge variables from g
    val edgeVars =
      edges.map((l, r) => (l, r) -> model.boolVar(s"${l}--$r")).toMap
    for {
      ((l, r), v) <- edgeVars
    } {
      model.edgeChanneling(g, v, nodes(l), nodes(r)).post()
    }
    val trToNode =
      (for {
        tr <- transactions
        nodes = nodeToTr.collect({ case (k, v) if v.contains(tr) => k }).toArray
      } yield {
        tr -> nodes
      }).toMap
    // Add constraint C_\Node i.e. node belongs to graph iff at least two transactions using it are activated
    for {
      (nodeId, transactions) <- nodeToTr
    } {
      val sumTransactions =
        model.sum(s"sum$nodeId", transactions.map(transactionVar).toArray: _*)
      nodeVars(nodeId).eq(sumTransactions.ge(2)).post()
    }
    // Retrieve the edges used by transactions
    val edgeToTr = (for {
      (l, r) <- edges
      commonTr = nodeToTr(l).toSet.intersect(nodeToTr(r).toSet)
    } yield {
      (l, r) -> commonTr.map(transactionVar).toArray
    }).toMap
    // Add constraint C_\Edge i.e. edge belongs to graph iff the nodes are in the graph and one transaction using the
    // edge is activated
    for {
      ((l, r), e) <- edgeVars
    } {
      val usedTr = edgeToTr((l, r))
      e.eq(nodeVars(l).and(nodeVars(r).and(usedTr.head.or(usedTr.tail: _*))))
        .post()
    }
    // Defining connectivity constraint
    val isConnected = model.connected(g)
    // Defining empty or non-empty constraint
    val isNonEmpty = model.or(nodeVars.values.toArray: _*)
    val isEmpty: Constraint =
      model.and(nodeVars.values.map(_.not()).toArray: _*)
    // Defining transaction contribution to channel constraint
    val trContribToGraph =
      for {
        (tr, n) <- trToNode.toArray
      } yield {
        if (n.isEmpty)
          transactionVar(tr).not()
        else {
          val nV = n.map(nodeVars)
          transactionVar(tr).imp(nV.head.or(nV.tail: _*))
        }
      }
    val trContribToGraphCst =
      trContribToGraph.head.and(trContribToGraph.tail: _*)
    // Defining the ITF constraint
    val isITF = model.boolVar("isITF")
    isITF
      .eq(isConnected.reify().and(isNonEmpty.reify()).and(trContribToGraphCst))
      .post()
    // Defining the Free constraint
    val isFree = model.boolVar("isFree")
    isFree.eq(isEmpty.reify()).post()
    // Compute only free and itf
    isFree.xor(isITF).post()
    val solver = model.getSolver
    while (solver.solve()) {} // implicitly enumerate solutions
  }
  buildModelAndSolve(4)
}
