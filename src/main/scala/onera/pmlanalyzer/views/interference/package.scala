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

package onera.pmlanalyzer.views

/** Package containing all interference modelling and analysis features of PML
  *
  * @see
  *   [[views.interference.model]] for basic classes used to model the
  *   interference analysis assumption
  * @see
  *   [[views.interference.operators]] for operators used to manipulate and
  *   perform analyses
  * @see
  *   [[views.interference.exporters]] for exporters
  * @see
  *   [[views.interference.examples]] for examples on operator and class usage
  *   and the related documentation in src/main/doc-resources/pml/examples
  * @groupname transform_operator Transform operators
  * @groupprio transform_operator 4
  * @groupname service_relation Service relations
  * @groupprio service_relation 3
  * @groupname transaction_relation Physical transaction relations
  * @groupprio transaction_relation 3
  * @groupname scenario_relation Physical scenario relations
  * @groupprio scenario_relation 3
  * @groupname equivalence_relation Equivalence relations
  * @groupprio equivalence_relation 3
  * @groupname utilFun Utility functions
  * @groupprio utilFun 5
  * @groupname exclusive_relation Exclusive relations
  * @groupprio exclusive_relation 3
  * @groupname interfere_relation Interfere relations
  * @groupprio interfere_relation 3
  * @groupname transparent_relation Transparent relations
  * @groupprio transparent_relation 3
  * @groupname exclusive_predicate Exclusive predicates
  * @groupprio exclusive_predicate 2
  * @groupname interfere_predicate Interfere predicates
  * @groupprio interfere_predicate 2
  * @groupname equivalence_predicate Equivalence predicates
  * @groupprio equivalence_predicate 2
  * @groupname transparent_predicate Transparent predicates
  * @groupprio transparent_predicate 2
  */
package object interference
