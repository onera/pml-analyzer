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

package examples.riscv.FU740.pml

/**
  * All transactions are used
  */
trait FU740LibraryConfigurationFull extends FU740LibraryConfiguration {
  self: FU740Platform =>

  t0_0.used
  t0_1.used
  t0_2.used
  t0_3.used
  t0_4.used
  t0_5.used

  t1_0.used
  t1_1.used
  t1_2.used
  t1_3.used
  t1_6.used
  t1_7.used
  t1_4.used
  t1_5.used

  t2_0.used
  t2_1.used
  t2_2.used
  t2_3.used
  t2_4.used
  t2_5.used
  t2_6.used

  t3_0.used
  t3_1.used
  t3_2.used
  t3_3.used
  t3_4.used
  t3_5.used

  t4_0.used
  t4_1.used
  t4_2.used
  t4_3.used
  t4_4.used
  t4_5.used
}
