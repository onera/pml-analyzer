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

package onera.pmlanalyzer.pml

/**
  * Package containing all the extension methods provided by operators
  *
  * @note This package should be imported in all pml models as so
  * {{{
  * import onera.pmlanalyzer.pml.operators._
  * }}}
  * @see [[Link.Ops]] for link (e.g. link/unlink) keywords
  * @see [[Linked.Ops]] for linked (e.g. linked/reverse) keywords
  * @see [[Deactivate.Ops]] for deactivate (e.g. deactivate) keywords
  * @see [[Provided.Ops]] for provide (e.g. services/loads/stores) keywords
  * @see [[Use.Ops]] for use (e.g. use/hostedBy) keywords
  * @see [[Used.Ops]] for used (e.g. used/targetLoads) keywords
  * @see [[Merge.Ops]] for and (e.g. and) keywords
  * @see [[Restrict.Ops]] for restrict (e.g. restrictedTo) keywords
  * @see [[Route.Ops]] for route (e.g. useLink/cannotUseLink) keywords
  */
package object operators extends Link.Ops
  with Linked.Ops
  with Deactivate.Ops
  with Provided.Ops
  with Use.Ops
  with Used.Ops
  with Merge.Ops
  with Restrict.Ops
  with Route.Ops 
  with AsTransaction.Ops
