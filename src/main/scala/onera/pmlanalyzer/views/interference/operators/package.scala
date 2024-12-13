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

package onera.pmlanalyzer.views.interference

/** Package containing the operators related to interference computation that
  * can be used on a PML model. Examples are provided in
  * [[views.interference.examples.simpleKeystone.SimpleKeystoneInterferenceGeneration]]
  * @note
  *   This package should be imported in all pml models as so
  *   {{{import onera.pmlanalyzer.views.interference.operators._}}}
  * @see
  *   [[Analyse.Ops]] provides the operators related to interference computation
  *   with Monosat [[https://github.com/sambayless/monosat]]
  * @see
  *   [[PostProcess.Ops]] provides the operators related to the post processing
  *   of the interference computation
  * @see
  *   [[Interfere.Ops]] provides the operators related to interference
  *   assumptions
  * @see
  *   [[Exclusive.Ops]] provides the operators related to exclusive assumptions
  *   (e.g., two [[pml.model.software.Application]] will not execute
  *   simultaneously)
  * @see
  *   [[Transparent.Ops]] proves the operators related to transparency
  *   assumptions (e.g., a
  *   [[pml.model.configuration.TransactionLibrary.Transaction]] is discarded)
  */
package object operators
    extends Analyse.Ops
    with PostProcess.Ops
    with Interfere.Ops
    with Exclusive.Ops
    with Transparent.Ops
    with Equivalent.Ops
