/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package views.interference.examples.simpleKeystone

import pml.examples.simpleKeystone.SimpleKeystoneExport.*
import views.interference.operators.*

import scala.concurrent.duration.*
import scala.language.postfixOps

/**
  * Compute the interference of the SimpleKeystone defined in [[pml.examples.simpleKeystone.SimpleKeystoneExport]]
  */
object SimpleKeystoneInterferenceGeneration extends App {

  for (p <- Set(
    SimpleKeystoneConfiguredFull,
    SimpleKeystoneConfiguredNoL1,
    SimpleKeystoneConfiguredPlanApp21,
    SimpleKeystoneConfiguredPlanApp22)) {

    // Compute only up to 2-ite and 2-free
    p.computeKInterference(2, 2 hours)

    // Compute all ite and itf for benchmarks
    p.computeAllInterference( 2 hours, ignoreExistingAnalysisFiles = true)
  }
}
