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

package onera.pmlanalyzer.views.interference.examples.riscv.FU740

import onera.pmlanalyzer.pml.examples.riscv.FU740.pml.*
import onera.pmlanalyzer.pml.examples.riscv.FU740.pml.FU740Export.*
import onera.pmlanalyzer.views.interference.operators.*

import scala.concurrent.duration.*
import scala.language.postfixOps

/**
  * Compute the interference of the FU740 defined in [[pml.examples.riscv.FU740.FU740Export]]
  */
object FU740InterferenceGeneration extends App {

  for (
    p <- Seq(
      FU740ConfiguredFull,
      FU740BenchmarkConfiguredInclusiveFull,
      FU740BenchmarkConfiguredFull,
      FU740PartitionedConfiguredFull
    )
  ) {
    // Compute all ite and itf for benchmarks
    p.computeAllInterference(5 hours, ignoreExistingAnalysisFiles = true)
  }
}
