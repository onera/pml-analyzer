/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.examples.mySys

/** Transaction that are used/ A user transaction is considered during the
  * analyses if identified as so. For instance to indicate that the t11
  * transaction defined in [[MySysTransactionLibrary]] is used {{{t11.used}}}
  * @see
  *   [[pml.operators.Use.Ops]] for operator definition
  */
trait MySysLibraryConfiguration
    extends MySysTransactionLibrary
    with MySysSoftwareAllocation {
  self: MyProcPlatform =>

  t11.used
  t12.used
  t13.used
  t14.used

  t21.used

  t22.used
  t23.used
  t24.used
  t25.used
  t26.used

  t31.used

  t41.used
}
