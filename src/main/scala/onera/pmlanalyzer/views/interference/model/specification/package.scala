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

package onera.pmlanalyzer.views.interference.model

/** Package containing the modelling features used to specify the assumptions
  * considered during the interference analysis.
  *
  * @see
  *   [[ApplicativeTableBasedInterferenceSpecification]] provides modelling
  *   features to specify application related assumption Example can be found in
  *   [[views.interference.examples.simpleKeystone.SimpleKeystoneApplicativeTableBasedInterferenceSpecification]]
  * @see
  *   [[PhysicalTableBasedInterferenceSpecification]] provides modelling
  *   features to specify hardware related assumption Example can be found in
  *   [[views.interference.examples.simpleKeystone.SimpleKeystonePhysicalTableBasedInterferenceSpecification]]
  * @see
  *   [[TableBasedInterferenceSpecification]] provides a wide range of modelling
  *   features to specify assumption.
  * @see
  *   [[InterferenceSpecification]] provides the basic modelling features to
  *   specify the assumptions, if possible consider using
  * @see
  *   [[InterferenceSpecification.Default]] provides a default specialization of
  *   [[InterferenceSpecification]]
  * @see
  *   [[TableBasedInterferenceSpecification.Default]] provides a default
  *   specialization of [[TableBasedInterferenceSpecification]]
  */
package object specification
