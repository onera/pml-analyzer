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

package mySys.views.interference

import mySys.pml.MySysExport.MySys
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.operators.*

import scala.concurrent.duration.*
import scala.language.postfixOps

/** Compute the interference of the SimpleKeystone defined in
  * [[pml.examples.mySys.MySysExport]]
  */
object MySysInterferenceGeneration extends App {

  // Exporting the graph of all pairs of transactions
  for {
    ss <- MySys.purifiedTransactions.keySet.subsets(2)
    if !MySys.finalExclusive(ss.head, ss.last)
  }
    MySys.exportInterferenceGraph(ss)

  // Compute only up to 2-ite and 2-free
  MySys.computeKInterference(2, 2 hours)

  // Compute all ite and itf for benchmarks
  MySys.computeAllInterference(2 hours, ignoreExistingAnalysisFiles = true)
}
